package py.una.medstudy

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.json.JSONObject
import java.util.Locale

internal data class ExamQuestion(
    val subject: String,
    val text: String,
    val options: List<String>,
    val correct: Int,
    val explanation: String,
)

internal data class PracticeExam(
    val id: String,
    val title: String,
    val minutes: Int,
    val questions: List<ExamQuestion>,
)

internal fun loadExams(context: Context): List<PracticeExam> {
    fun read(name: String) = context.assets.open(name).bufferedReader().use { JSONObject(it.readText()) }
    val bank = read("preguntas.json").getJSONArray("questions")
    val questions = (0 until bank.length()).associate { index ->
        val entry = bank.getJSONObject(index)
        val options = entry.getJSONArray("options")
        entry.getString("id") to ExamQuestion(
            entry.getString("subject"),
            entry.getString("question"),
            List(options.length()) { options.getString(it) },
            entry.getInt("correctIndex"),
            entry.getString("explanation"),
        )
    }
    val exams = read("simulacros.json").getJSONArray("exams")
    return List(exams.length()) { index ->
        val entry = exams.getJSONObject(index)
        val ids = entry.getJSONArray("questionIds")
        PracticeExam(
            entry.getString("id"), entry.getString("title"), entry.getInt("minutes"),
            List(ids.length()) { questions.getValue(ids.getString(it)) },
        )
    }
}

@Composable
internal fun ExamScreen(
    exams: List<PracticeExam>,
    onBack: () -> Unit,
) {
    var examId by rememberSaveable { mutableStateOf<String?>(null) }
    var answers by rememberSaveable { mutableStateOf(IntArray(0)) }
    var position by rememberSaveable { mutableIntStateOf(0) }
    var deadline by rememberSaveable { mutableLongStateOf(0L) }
    var finished by rememberSaveable { mutableStateOf(false) }
    var confirm by rememberSaveable { mutableStateOf(false) }
    var remaining by remember { mutableLongStateOf(0L) }
    val exam = exams.firstOrNull { it.id == examId }
    LaunchedEffect(deadline, finished) {
        if (deadline > 0L && !finished) {
            while (true) {
                remaining = ((deadline - System.currentTimeMillis() + 999L) / 1000L).coerceAtLeast(0L)
                if (remaining == 0L) {
                    confirm = false
                    finished = true
                    break
                }
                delay(250L)
            }
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        when {
            exam == null -> {
                Text("Simulacros de práctica", style = MaterialTheme.typography.headlineSmall)
                Text("Elige un examen y revisa la corrección al terminar.")
                for (option in exams) {
                    OutlinedButton(onClick = {
                        position = 0
                        answers = IntArray(option.questions.size) { -1 }
                        remaining = option.minutes * 60L
                        deadline = System.currentTimeMillis() + remaining * 1000L
                        finished = false
                        examId = option.id
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text("${option.title} · ${option.minutes} min")
                    }
                }
                TextButton(onClick = onBack) { Text("Volver a estudiar") }
            }
            finished -> {
                val total = exam.questions.indices.count { answers[it] == exam.questions[it].correct }
                Text("Resultado: $total / ${exam.questions.size}", style = MaterialTheme.typography.headlineSmall)
                for ((subject, indexes) in exam.questions.indices.groupBy { exam.questions[it].subject }) {
                    val score = indexes.count { answers[it] == exam.questions[it].correct }
                    Text("$subject · $score / ${indexes.size} · ${score * 100 / indexes.size} %")
                }
                OutlinedButton(onClick = { examId = null; deadline = 0L }) { Text("Elegir otro simulacro") }
                exam.questions.forEachIndexed { index, question ->
                    HorizontalDivider()
                    Text("${index + 1}. ${question.text}", style = MaterialTheme.typography.titleMedium)
                    Text("Tu respuesta: ${question.options.getOrNull(answers[index]) ?: "Sin responder"}")
                    Text("Correcta: ${question.options[question.correct]}")
                    Text(question.explanation)
                }
            }
            else -> {
                val question = exam.questions[position]
                val time = String.format(Locale.ROOT, "%02d:%02d", remaining / 60L, remaining % 60L)
                Text("$time · Pregunta ${position + 1} de ${exam.questions.size}")
                Text(question.text, style = MaterialTheme.typography.headlineSmall)
                question.options.forEachIndexed { index, option ->
                    FilterChip(selected = answers[position] == index, onClick = {
                        if (System.currentTimeMillis() < deadline) {
                            answers = answers.copyOf().also { it[position] = index }
                        }
                    }, label = { Text(option) }, modifier = Modifier.fillMaxWidth())
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { position-- }, enabled = position > 0) { Text("Anterior") }
                    OutlinedButton(onClick = { position++ }, enabled = position < exam.questions.lastIndex) { Text("Siguiente") }
                }
                Button(onClick = { confirm = true }) { Text("Entregar") }
            }
        }
    }
    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text("Entregar simulacro") },
            text = { Text("${answers.count { it < 0 }} preguntas sin responder.") },
            confirmButton = { TextButton(onClick = { confirm = false; finished = true }) { Text("Entregar") } },
            dismissButton = { TextButton(onClick = { confirm = false }) { Text("Seguir") } },
        )
    }
}
