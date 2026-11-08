package py.una.medstudy

import androidx.room.withTransaction
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

fun normalizedSubject(subject: String): String =
    if (subject == "Lenguaje y Comunicación") "Lenguaje" else subject

class StudyRepository(val db: StudyDatabase) {
    val dao = db.dao()

    // Validation completes before the first write, including for large imports.
    fun parseCards(text: String): List<Card> {
        val root = JSONObject(text)
        require(root.optInt("formatVersion") == 1) { "Formato no compatible" }
        val array = root.getJSONArray("cards")
        require(array.length() in 1..50000) { "El banco debe tener entre 1 y 50.000 tarjetas" }
        val cards = List(array.length()) { i ->
            val o = array.getJSONObject(i)
            Card(
                o.getString("id").trim(), o.getString("question").trim(), o.getString("answer").trim(),
                normalizedSubject(o.getString("subject").trim()), o.getString("unit").trim(),
                o.getString("topic").trim(), o.optString("source"), o.optString("page"),
                o.optString("explanation"), o.optInt("difficulty", 1),
            )
        }
        require(cards.all { it.id.isNotBlank() && it.question.isNotBlank() &&
            it.answer.isNotBlank() && it.subject.isNotBlank() && it.unit.isNotBlank() &&
            it.topic.isNotBlank() && it.contentDifficulty in 1..3 }) { "Tarjeta incompleta o dificultad inválida" }
        require(cards.map { it.id }.toSet().size == cards.size) { "El archivo contiene IDs repetidos" }
        return cards
    }

    suspend fun importCards(text: String, updateBundledContent: Boolean = false): Int {
        val cards = parseCards(text)
        return db.withTransaction {
            val inserted = dao.add(cards).count { it != -1L }
            if (updateBundledContent) dao.updateCards(cards)
            inserted
        }
    }

    suspend fun rate(cardId: String, rating: Int, expectedReviews: Int, now: Long): Boolean =
        db.withTransaction {
            val previous = dao.getProgress(cardId) ?: Progress(cardId)
            // A repeated tap or retry from the same visible card must not add a second review.
            if (previous.reviews != expectedReviews) return@withTransaction false
            val next = Fsrs.next(previous, rating, now)
            dao.save(next)
            dao.log(ReviewLog(cardId = cardId, ratedAt = now, rating = rating,
                stability = next.stability, difficulty = next.difficulty, due = next.due))
            true
        }

    suspend fun startExam(exam: PracticeExam, now: Long): String = db.withTransaction {
        val active = dao.allExams().firstOrNull { it.finishedAt == 0L }
        if (active != null) return@withTransaction active.id
        val id = UUID.randomUUID().toString()
        dao.saveExam(ExamRun(id, exam.title, now, now + exam.minutes * 60000L,
            questionsJson = questionsJson(exam.questions),
            answersJson = JSONArray(List(exam.questions.size) { -1 }).toString()))
        id
    }

    suspend fun answer(id: String, index: Int, option: Int, now: Long) = db.withTransaction {
        val run = dao.exam(id) ?: return@withTransaction
        if (run.finishedAt != 0L) return@withTransaction
        if (now >= run.deadline) {
            dao.saveExam(run.copy(finishedAt = run.deadline))
            return@withTransaction
        }
        val questions = parseQuestions(run.questionsJson)
        require(index in questions.indices && option in questions[index].options.indices)
        val answers = JSONArray(run.answersJson).put(index, option)
        dao.saveExam(run.copy(answersJson = answers.toString()))
    }

    suspend fun position(id: String, position: Int) = db.withTransaction {
        val run = dao.exam(id) ?: return@withTransaction
        if (run.finishedAt == 0L && position in parseQuestions(run.questionsJson).indices) {
            dao.saveExam(run.copy(position = position))
        }
    }

    suspend fun finish(id: String, now: Long) = db.withTransaction {
        val run = dao.exam(id) ?: return@withTransaction
        if (run.finishedAt == 0L) {
            dao.saveExam(run.copy(finishedAt = now.coerceIn(run.startedAt, run.deadline)))
        }
    }

    suspend fun exportBackup(): String = db.withTransaction {
        val gson = com.google.gson.Gson()
        JSONObject().put("backupVersion", 1)
            .put("cards", JSONArray(gson.toJson(dao.allCards())))
            .put("progress", JSONArray(gson.toJson(dao.allProgress())))
            .put("logs", JSONArray(gson.toJson(dao.allLogs())))
            .put("times", JSONArray(gson.toJson(dao.allTimes())))
            .put("exams", JSONArray(gson.toJson(dao.allExams()))).toString()
    }

    suspend fun restoreBackup(text: String) {
        val root = JSONObject(text)
        require(root.optInt("backupVersion") == 1) { "Copia no compatible" }
        val gson = com.google.gson.Gson()
        val cards = gson.fromJson(root.getJSONArray("cards").toString(), Array<Card>::class.java).toList()
        val progress = gson.fromJson(root.getJSONArray("progress").toString(), Array<Progress>::class.java).toList()
        val logs = gson.fromJson(root.getJSONArray("logs").toString(), Array<ReviewLog>::class.java).toList()
        val times = gson.fromJson(root.getJSONArray("times").toString(), Array<StudyTime>::class.java).toList()
        val exams = gson.fromJson(root.getJSONArray("exams").toString(), Array<ExamRun>::class.java).toList()
        require(cards.size <= 50000 && cards.map { it.id }.toSet().size == cards.size)
        require(cards.all { it.id.isNotBlank() && it.question.isNotBlank() && it.answer.isNotBlank() &&
            it.subject.isNotBlank() && it.unit.isNotBlank() && it.topic.isNotBlank() && it.contentDifficulty in 1..3 })
        val ids = cards.map { it.id }.toSet()
        require(progress.all { it.cardId in ids && it.reviews >= 0 && it.stability.isFinite() &&
            it.difficulty.isFinite() && it.stability >= 0 && it.due >= 0 })
        require(logs.all { it.cardId in ids && it.rating in 1..4 })
        require(times.all { it.millis in 0..60000 && it.at >= 0 })
        require(exams.count { it.finishedAt == 0L } <= 1)
        for (run in exams) {
            val questions = parseQuestions(run.questionsJson)
            val answers = parseAnswers(run.answersJson)
            require(questions.isNotEmpty() && answers.size == questions.size &&
                run.position in questions.indices && run.deadline >= run.startedAt)
            require(answers.indices.all { answers[it] == -1 || answers[it] in questions[it].options.indices })
        }
        db.withTransaction {
            // Merge cards, retain newer progress and avoid duplicating logs or completed exams.
            dao.add(cards)
            for (p in progress) {
                val existing = dao.getProgress(p.cardId)
                if (existing == null || p.lastReview > existing.lastReview) dao.save(p)
            }
            val known = dao.allLogs().map { Triple(it.cardId, it.ratedAt, it.rating) }.toMutableSet()
            for (log in logs) if (known.add(Triple(log.cardId, log.ratedAt, log.rating))) dao.log(log.copy(id = 0))
            times.forEach { dao.addTime(it) }
            var activeExists = dao.allExams().any { it.finishedAt == 0L }
            for (run in exams) {
                if (dao.exam(run.id) == null) {
                    if (run.finishedAt != 0L || !activeExists) {
                        dao.saveExam(run)
                        if (run.finishedAt == 0L) activeExists = true
                    }
                }
            }
        }
    }
}
