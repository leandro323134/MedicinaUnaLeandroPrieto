package py.una.medstudy

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

data class ExamQuestion(
    val subject: String, val text: String, val options: List<String>,
    val correct: Int, val explanation: String, val source: String = "",
    val id: String = "", val topic: String = "",
)
data class PracticeExam(val id: String, val title: String, val minutes: Int, val questions: List<ExamQuestion>)

fun parseQuestions(text: String): List<ExamQuestion> {
    val array = JSONArray(text)
    return List(array.length()) { index ->
        val entry = array.getJSONObject(index)
        val options = entry.getJSONArray("options")
        val question = ExamQuestion(
            normalizedSubject(entry.getString("subject")), entry.getString("question"),
            List(options.length()) { options.getString(it) }, entry.getInt("correctIndex"),
            entry.getString("explanation"), entry.optString("source"), entry.optString("id"), entry.optString("topic"),
        )
        require(question.text.isNotBlank() && question.options.size in 2..6 &&
            question.options.distinct().size == question.options.size &&
            question.correct in question.options.indices)
        question
    }
}
fun questionsJson(questions: List<ExamQuestion>): String = JSONArray(questions.map {
    JSONObject().put("subject", it.subject).put("question", it.text).put("options", JSONArray(it.options))
        .put("id", it.id).put("topic", it.topic).put("correctIndex", it.correct).put("explanation", it.explanation).put("source", it.source)
}).toString()

fun parseAnswers(text: String): List<Int> {
    val array = JSONArray(text)
    return List(array.length()) { array.getInt(it) }
}
fun ExamRun.score(): Pair<Int, Int> {
    val questions = parseQuestions(questionsJson)
    val answers = parseAnswers(answersJson)
    return questions.indices.count { answers[it] == questions[it].correct } to questions.size
}

fun loadQuestionBank(context: Context): List<ExamQuestion> =
    context.assets.open("preguntas.json").bufferedReader().use {
        parseQuestions(JSONObject(it.readText()).getJSONArray("questions").toString())
    }

fun loadExams(context: Context, bank: List<ExamQuestion> = loadQuestionBank(context)): List<PracticeExam> {
    fun read(name: String) = context.assets.open(name).bufferedReader().use { JSONObject(it.readText()) }
    val questions = bank.associateBy { it.id }
    val exams = read("simulacros.json").getJSONArray("exams")
    return List(exams.length()) { index ->
        val entry = exams.getJSONObject(index)
        val ids = entry.getJSONArray("questionIds")
        PracticeExam(entry.getString("id"), entry.getString("title"), entry.getInt("minutes"),
            List(ids.length()) { questions.getValue(ids.getString(it)) })
    }
}

@Composable
fun ExamScreen(model: StudyModel, selectedId: String?, select: (String?) -> Unit, now: Long, openErrors: (String) -> Unit) {
    val runs by model.runs.collectAsState()
    val run = runs.firstOrNull { it.id == selectedId }
    var confirm by rememberSaveable { mutableStateOf(false) }
    var questionCount by rememberSaveable { mutableIntStateOf(20) }
    var minutes by rememberSaveable { mutableIntStateOf(30) }
    var subject by rememberSaveable { mutableStateOf("Todas") }
    val completeBank = model.bank
    val active = runs.firstOrNull { it.finishedAt == 0L && it.deadline > now }
    val available = remember(completeBank, subject) { questionPool(completeBank, subject.takeUnless { it == "Todas" }).size }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (run == null) {
            Text("Simulacros", style = MaterialTheme.typography.headlineMedium)
            Text("Practica con tiempo y revisa la explicación de cada respuesta al terminar.")
            active?.let {
                Button(onClick = { select(it.id) }) { Text("Continuar ${it.title}") }
            }
            model.exams.forEach { exam ->
                OutlinedButton(onClick = { model.start(exam) { select(it) } },
                    enabled = !model.busy && active == null, modifier = Modifier.fillMaxWidth()) {
                    Text("${exam.title} · ${exam.questions.size} preguntas · ${exam.minutes} min")
                }
            }
            HorizontalDivider()
            Text("Crear una práctica", style = MaterialTheme.typography.titleLarge)
            ChoiceMenu("Materia", listOf("Todas") + completeBank.map { it.subject }.distinct(), subject) { subject = it }
            Stepper("Preguntas", questionCount, 5, 120, 5) { questionCount = it }
            Stepper("Minutos", minutes, 5, 120, 5) { minutes = it }
            Button(onClick = {
                val selection = selectQuestions(completeBank, subject.takeUnless { it == "Todas" }, questionCount)
                model.start(PracticeExam("custom", "Práctica · $subject", minutes, selection)) { select(it) }
            }, enabled = !model.busy && active == null && available > 0) { Text("Comenzar práctica") }
            Text("${minOf(questionCount, available)} preguntas disponibles para este intento. En general se alternan las materias y se reparten los temas disponibles. El tiempo sigue corriendo al salir.",
                style = MaterialTheme.typography.bodySmall)
            if (runs.any { it.finishedAt > 0 }) Text("Resultados anteriores", style = MaterialTheme.typography.titleLarge)
            runs.filter { it.finishedAt > 0 }.take(20).forEach { previous ->
                val score = previous.score()
                TextButton(onClick = { select(previous.id) }) {
                    Text("${previous.title} · ${score.first}/${score.second} · ${dateLabel(previous.finishedAt)}")
                }
            }
        } else {
            val questions = remember(run.questionsJson) { parseQuestions(run.questionsJson) }
            val answers = remember(run.answersJson) { parseAnswers(run.answersJson) }
            TextButton(onClick = { select(null) }) { Text("Volver a simulacros") }
            if (run.finishedAt > 0) {
                val correct = questions.indices.count { answers[it] == questions[it].correct }
                Text("$correct de ${questions.size} · ${correct * 100 / questions.size} %",
                    style = MaterialTheme.typography.headlineMedium)
                for ((name, indexes) in questions.indices.groupBy { questions[it].subject }) {
                    val score = indexes.count { answers[it] == questions[it].correct }
                    Text("$name · $score/${indexes.size} · ${score * 100 / indexes.size} %")
                }
                if (correct < questions.size) {
                    Button(onClick = { openErrors(run.id) }) { Text("Repasar errores") }
                    Text("Respuestas incorrectas o en blanco", style = MaterialTheme.typography.titleLarge)
                } else Text("Todas las respuestas fueron correctas.")
                questions.forEachIndexed { index, question ->
                    if (answers[index] == question.correct) return@forEachIndexed
                    HorizontalDivider()
                    Text("${index + 1}. ${question.text}", style = MaterialTheme.typography.titleMedium)
                    Text("Tu respuesta · ${question.options.getOrNull(answers[index]) ?: "Sin responder"}")
                    Text("Correcta · ${question.options[question.correct]}", color = MaterialTheme.colorScheme.primary)
                    Text(question.explanation)
                    Text(question.source, style = MaterialTheme.typography.bodySmall)
                }
            } else {
                val remaining = ((run.deadline - now + 999) / 1000).coerceAtLeast(0)
                val position = run.position
                val question = questions[position]
                Text(String.format(Locale.ROOT, "%02d:%02d", remaining / 60, remaining % 60),
                    style = MaterialTheme.typography.headlineMedium)
                LinearProgressIndicator(progress = (position + 1f) / questions.size, modifier = Modifier.fillMaxWidth())
                Text("Pregunta ${position + 1} de ${questions.size} · ${question.subject}")
                Text(question.text, style = MaterialTheme.typography.titleLarge)
                question.options.forEachIndexed { option, text ->
                    FilterChip(selected = answers[position] == option,
                        onClick = { model.answer(run.id, position, option) },
                        enabled = !model.busy && remaining > 0,
                        label = { Text("${'A' + option}. $text") }, modifier = Modifier.fillMaxWidth())
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { model.move(run.id, position - 1) }, enabled = position > 0 && !model.busy) { Text("Anterior") }
                    OutlinedButton(onClick = { model.move(run.id, position + 1) }, enabled = position < questions.lastIndex && !model.busy) { Text("Siguiente") }
                }
                Text("${answers.count { it >= 0 }} respondidas")
                Button(onClick = { confirm = true }, enabled = !model.busy) { Text("Entregar") }
            }
        }
    }
    if (confirm && run != null) AlertDialog(
        onDismissRequest = { confirm = false },
        title = { Text("Entregar simulacro") },
        text = { Text("${parseAnswers(run.answersJson).count { it < 0 }} preguntas sin responder.") },
        confirmButton = { TextButton(onClick = { confirm = false; model.finish(run.id) }) { Text("Entregar") } },
        dismissButton = { TextButton(onClick = { confirm = false }) { Text("Seguir") } },
    )
}
