package py.una.medstudy

import java.security.MessageDigest
import kotlin.random.Random

private val whitespace = Regex("\\s+")
private fun normalizedQuestion(text: String) =
    text.trim().replace(whitespace, " ").lowercase(java.util.Locale.ROOT)

private fun questionKey(subject: String, text: String) =
    normalizedSubject(subject) + "\n" + normalizedQuestion(text)

fun questionPool(bank: List<ExamQuestion>, subject: String?): List<ExamQuestion> =
    bank.filter { subject == null || it.subject == subject }
        .distinctBy { normalizedQuestion(it.text) }
        .distinctBy { it.id.ifBlank { normalizedQuestion(it.text) } }

fun selectQuestions(bank: List<ExamQuestion>, subject: String?, count: Int, random: Random = Random.Default): List<ExamQuestion> {
    val eligible = questionPool(bank, subject)
    // Cycle through topics within each subject, then through subjects. Empty groups never block selection.
    val subjects = eligible.groupBy { it.subject }.values.shuffled(random).map { questions ->
        val topics = questions.groupBy { it.topic }.values.shuffled(random)
            .map { ArrayDeque(it.shuffled(random)) }
        val ordered = ArrayDeque<ExamQuestion>()
        while (topics.any { it.isNotEmpty() }) topics.forEach { it.removeFirstOrNull()?.let(ordered::addLast) }
        ordered
    }
    val result = mutableListOf<ExamQuestion>()
    while (result.size < count && subjects.any { it.isNotEmpty() }) {
        for (queue in subjects) {
            if (result.size == count) break
            queue.removeFirstOrNull()?.let(result::add)
        }
    }
    return result
}

data class ExamOutcome(val question: ExamQuestion, val answer: Int, val at: Long)
data class ErrorItem(val question: ExamQuestion, val consecutive: Int, val missedAt: Long) {
    val status: String get() = when (consecutive) { 0 -> "Pendiente"; 1 -> "En práctica"; else -> "Dominada" }
}

fun examOutcomes(runs: List<ExamRun>, bank: List<ExamQuestion>): List<ExamOutcome> {
    val byId = bank.associateBy { it.id }
    val byText = bank.groupBy { questionKey(it.subject, it.text) }
    return runs.filter { it.finishedAt > 0 }.flatMap { run ->
        val answers = parseAnswers(run.answersJson)
        parseQuestions(run.questionsJson).mapIndexed { i, saved ->
            val key = questionKey(saved.subject, saved.text)
            val current = byId[saved.id] ?: byText[key]?.filter {
                it.options[it.correct] == saved.options[saved.correct]
            }?.singleOrNull()
            val resolved = if (saved.id.isBlank()) {
                val id = current?.id ?: "legacy-" + MessageDigest.getInstance("SHA-256")
                    .digest((key + "\n" + saved.options[saved.correct]).toByteArray()).joinToString("") { "%02x".format(it.toInt() and 255) }
                saved.copy(id = id, topic = current?.topic ?: "Sin tema identificado")
            } else saved.copy(topic = saved.topic.ifBlank { current?.topic ?: "Sin tema identificado" })
            ExamOutcome(resolved, answers.getOrElse(i) { -1 }, run.finishedAt)
        }
    }
}

fun errorItems(outcomes: List<ExamOutcome>, reviews: List<ErrorReview>): List<ErrorItem> {
    val reviewsByQuestion = reviews.groupBy { it.questionId }
    return outcomes.filter { it.answer != it.question.correct }.groupBy { it.question.id }.values.map { misses ->
        val last = misses.maxBy { it.at }
        var streak = 0
        reviewsByQuestion[last.question.id].orEmpty().filter { it.at > last.at }
            .sortedWith(compareBy<ErrorReview> { it.at }.thenBy { it.sessionId })
            .distinctBy { it.sessionId }
            .forEach { streak = if (it.correct) (streak + 1).coerceAtMost(2) else 0 }
        ErrorItem(last.question, streak, last.at)
    }.sortedByDescending { it.missedAt }
}

data class TopicResult(
    val subject: String, val topic: String, val examAnswers: Int, val examCorrect: Int,
    val reviewAnswers: Int, val reviewCorrect: Int, val pending: Int,
)

fun topicResults(outcomes: List<ExamOutcome>, reviews: List<ErrorReview>, errors: List<ErrorItem>): List<TopicResult> {
    val exams = outcomes.groupBy { it.question.subject to it.question.topic }
    val byId = errors.associateBy { it.question.id }
    val practice = reviews.mapNotNull { review ->
        byId[review.questionId]?.let { (it.question.subject to it.question.topic) to review }
    }.groupBy({ it.first }, { it.second })
    val pending = errors.filter { it.consecutive < 2 }.groupingBy { it.question.subject to it.question.topic }.eachCount()
    return (exams.keys + practice.keys).map { key ->
        val attempts = exams[key].orEmpty().filter { it.answer >= 0 }
        val retried = practice[key].orEmpty()
        TopicResult(key.first, key.second, attempts.size, attempts.count { it.answer == it.question.correct },
            retried.size, retried.count { it.correct }, pending[key] ?: 0)
    }.sortedWith(compareBy<TopicResult> { it.subject }.thenByDescending { it.pending }.thenBy { it.topic })
}
