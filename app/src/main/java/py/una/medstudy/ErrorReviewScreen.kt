package py.una.medstudy

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.UUID

@Composable
fun ErrorReviewScreen(model: StudyModel, runId: String?, initialSubject: String?, initialTopic: String?) {
    val errors by model.errors.collectAsState()
    val reviews by model.errorReviews.collectAsState()
    val runs by model.runs.collectAsState()
    val focused = remember(runId, runs, model.bank) {
        if (runId == null) null else examOutcomes(runs.filter { it.id == runId }, model.bank)
            .filter { it.answer != it.question.correct }.map { it.question.id }.toSet()
    }
    var subject by rememberSaveable { mutableStateOf(initialSubject ?: "Todas") }
    var topic by rememberSaveable { mutableStateOf(initialTopic ?: "Todos") }
    var session by rememberSaveable { mutableStateOf<String?>(null) }
    var queue by rememberSaveable { mutableStateOf(arrayListOf<String>()) }
    var index by rememberSaveable { mutableIntStateOf(0) }
    var selected by rememberSaveable { mutableIntStateOf(-1) }
    val visible = errors.filter { focused == null || it.question.id in focused }
    val filtered = visible.filter {
        (subject == "Todas" || it.question.subject == subject) && (topic == "Todos" || it.question.topic == topic)
    }
    val pending = filtered.filter { it.consecutive < 2 }
    val currentId = queue.getOrNull(index)
    val question = errors.firstOrNull { it.question.id == currentId }?.question
    val saved = reviews.firstOrNull { it.sessionId == session && it.questionId == currentId }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Repaso de errores", style = MaterialTheme.typography.headlineMedium)
        if (session == null) {
            if (runId != null) Text("Errores de este simulacro")
            ChoiceMenu("Materia", (listOf("Todas", subject) + visible.map { it.question.subject }).distinct(), subject) {
                subject = it; topic = "Todos"
            }
            ChoiceMenu("Tema", (listOf("Todos", topic) + visible.filter {
                subject == "Todas" || it.question.subject == subject
            }.map { it.question.topic }).distinct(), topic) { topic = it }
            Text("${filtered.count { it.consecutive == 0 }} pendientes · ${filtered.count { it.consecutive == 1 }} en práctica · ${filtered.count { it.consecutive == 2 }} dominadas")
            Text("Dos aciertos seguidos en sesiones distintas marcan una pregunta como dominada. Un error reinicia el conteo.")
            Button(onClick = {
                queue = ArrayList(pending.map { it.question.id })
                index = 0
                selected = -1
                session = UUID.randomUUID().toString()
            }, enabled = pending.isNotEmpty() && !model.busy) { Text("Empezar repaso") }
            if (filtered.isEmpty()) Text("Todavía no hay errores para esta selección.")
            val visibleIds = filtered.map { it.question.id }.toSet()
            val history = reviews.filter { it.questionId in visibleIds }.groupBy { it.sessionId }.values
                .sortedByDescending { attempt -> attempt.maxOf { it.at } }.take(5)
            if (history.isNotEmpty()) {
                Text("Sesiones recientes", style = MaterialTheme.typography.titleMedium)
                history.forEach { attempt ->
                    Text("${dateLabel(attempt.maxOf { it.at })} · ${attempt.count { it.correct }}/${attempt.size} aciertos")
                }
            }
            filtered.forEach { item ->
                Text("${item.status} · ${item.question.subject} · ${item.question.topic}", style = MaterialTheme.typography.labelLarge)
                Text(item.question.text)
                HorizontalDivider()
            }
        } else if (question != null) {
            Text("${index + 1} de ${queue.size} · ${question.subject} · ${question.topic}")
            Text(question.text, style = MaterialTheme.typography.titleLarge)
            question.options.forEachIndexed { option, text ->
                FilterChip(selected = selected == option,
                    onClick = { selected = option }, enabled = saved == null && !model.busy,
                    label = { Text(text) }, modifier = Modifier.fillMaxWidth())
            }
            if (saved == null) {
                Button(onClick = { session?.let { model.practiceError(it, question, selected) } },
                    enabled = selected >= 0 && !model.busy) { Text("Comprobar respuesta") }
            } else {
                Text(if (saved.correct) "Correcta" else "Incorrecta")
                Text("Respuesta correcta · ${question.options[question.correct]}")
                Text(question.explanation)
                if (question.source.isNotBlank()) Text(question.source, style = MaterialTheme.typography.bodySmall)
                Button(onClick = { index++; selected = -1 }) { Text("Siguiente") }
            }
        } else if (index >= queue.size) {
            val completed = reviews.filter { it.sessionId == session }
            Text("Repaso terminado · ${completed.count { it.correct }}/${completed.size} aciertos")
            Button(onClick = { session = null; queue = arrayListOf(); index = 0 }) { Text("Volver a los errores") }
        } else {
            Text("Cargando el historial de errores…")
        }
    }
}
