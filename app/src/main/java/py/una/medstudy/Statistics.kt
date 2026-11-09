package py.una.medstudy

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.Locale

fun localDate(time: Long, zone: ZoneId = ZoneId.systemDefault()): LocalDate =
    Instant.ofEpochMilli(time).atZone(zone).toLocalDate()
fun dateLabel(time: Long): String = localDate(time).format(DateTimeFormatter.ofPattern("dd/MM"))
fun streak(dates: Set<LocalDate>, today: LocalDate): Int {
    var day = if (today in dates) today else today.minusDays(1)
    var count = 0
    while (day in dates) { count++; day = day.minusDays(1) }
    return count
}
fun durationLabel(millis: Long): String {
    val minutes = millis / 60000
    return if (minutes >= 60) "${minutes / 60} h ${minutes % 60} min" else "$minutes min"
}
fun percent(correct: Int, total: Int): String = if (total == 0) "—" else "${correct * 100 / total} %"

@Composable
fun StatisticsScreen(cards: List<Card>, progress: List<Progress>, logs: List<ReviewLog>,
                     times: List<StudyTime>, runs: List<ExamRun>, topics: List<TopicResult>, now: Long, openExam: (String) -> Unit) {
    val finished = runs.filter { it.finishedAt > 0 }
    val scores = finished.map { it.score() }
    var selectedSubject by rememberSaveable { mutableStateOf<String?>(null) }
    val today = localDate(now)
    val activeDays = (logs.map { localDate(it.ratedAt) } + finished.map { localDate(it.finishedAt) }).toSet()
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Tu evolución", style = MaterialTheme.typography.headlineMedium)
        Metric("Tiempo de estudio", durationLabel(times.sumOf { it.millis }))
        Text("Cuenta el tiempo con la pantalla de estudio abierta y activa. Empieza a registrarse en esta versión.",
            style = MaterialTheme.typography.bodySmall)
        Metric("Racha", "${streak(activeDays, today)} días")
        Metric("Tarjetas repasadas", "${progress.count { it.reviews > 0 }} / ${cards.size}")
        Metric("Tarjetas aprendidas", "${progress.count { it.reviews > 0 && it.stability >= 21.0 }}")
        Text("Aprendida indica una estabilidad estimada de al menos 21 días.", style = MaterialTheme.typography.bodySmall)
        Metric("Aciertos en simulacros", percent(scores.sumOf { it.first }, scores.sumOf { it.second }))
        Metric("Recuerdo en tarjetas", percent(logs.count { it.rating >= 3 }, logs.size))
        Text("Recuerdo es tu valoración Bien o Fácil. Los aciertos se calculan con las respuestas de los simulacros.",
            style = MaterialTheme.typography.bodySmall)
        Text("Últimos 7 días", style = MaterialTheme.typography.titleLarge)
        for (offset in 6 downTo 0) {
            val day = today.minusDays(offset.toLong())
            val dayTime = times.filter { localDate(it.at) == day }.sumOf { it.millis }
            val dayLogs = logs.count { localDate(it.ratedAt) == day }
            Text("${day.format(DateTimeFormatter.ofPattern("EEE dd", Locale("es")))} · ${durationLabel(dayTime)} · $dayLogs repasos")
            LinearProgressIndicator(progress = (dayTime / 3600000f).coerceIn(0f, 1f), modifier = Modifier.fillMaxWidth())
        }
        Text("Rendimiento por materia", style = MaterialTheme.typography.titleLarge)
        val questions = finished.flatMap { run ->
            val answers = parseAnswers(run.answersJson)
            parseQuestions(run.questionsJson).mapIndexed { i, q -> q.subject to (answers[i] == q.correct) }
        }
        for ((subject, results) in questions.groupBy { it.first }) {
            Metric(subject, percent(results.count { it.second }, results.size))
        }
        if (finished.isEmpty()) Text("Tus primeros resultados aparecerán al terminar un simulacro.")
        Text("Rendimiento por tema", style = MaterialTheme.typography.titleLarge)
        Text("Los aciertos por tema se calculan sobre respuestas contestadas. Las preguntas en blanco pueden quedar pendientes de repaso.")
        val subjects = topics.map { it.subject }.distinct()
        if (subjects.isEmpty()) {
            Text("Todavía no hay intentos finalizados.")
        } else {
            val subject = selectedSubject?.takeIf { it in subjects } ?: subjects.first()
            ChoiceMenu("Materia", subjects, subject) { selectedSubject = it }
            topics.filter { it.subject == subject }.forEach {
                Text(it.topic, style = MaterialTheme.typography.titleMedium)
                Text("Simulacros · ${it.examAnswers} respuestas · ${percent(it.examCorrect, it.examAnswers)} de aciertos")
                Text("Repaso · ${it.reviewAnswers} respuestas · ${percent(it.reviewCorrect, it.reviewAnswers)} de aciertos")
                Text("${it.pending} errores por repasar")
                if (it.examAnswers < 5) Text("Pocos datos de simulacro para evaluar este tema.", style = MaterialTheme.typography.bodySmall)
                HorizontalDivider()
            }
        }
        Text("Historial de simulacros", style = MaterialTheme.typography.titleLarge)
        finished.take(30).forEach { run ->
            val score = run.score()
            TextButton(onClick = { openExam(run.id) }) {
                Text("${dateLabel(run.finishedAt)} · ${run.title} · ${percent(score.first, score.second)}")
            }
        }
    }
}

@Composable
fun Metric(label: String, value: String) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, modifier = Modifier.weight(1f))
            Text(value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}
