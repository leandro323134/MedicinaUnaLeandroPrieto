package py.una.medstudy

import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { StudyApp() }
    }
}
private enum class StudyTab(val label: String, val icon: ImageVector) {
    Home("Inicio", Icons.Outlined.Home), Subjects("Materias", Icons.Outlined.MenuBook),
    Review("Estudiar", Icons.Outlined.School), Stats("Progreso", Icons.Outlined.BarChart),
    Settings("Ajustes", Icons.Outlined.Settings);
}

@Composable
fun ChoiceMenu(label: String, options: List<String>, value: String, choose: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) { Text("$label · $value") }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = { choose(option); expanded = false })
            }
        }
    }
}
@Composable
fun Stepper(label: String, value: Int, min: Int, max: Int, step: Int, change: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("$label · $value", modifier = Modifier.weight(1f).padding(top = 12.dp))
        TextButton(onClick = { change((value - step).coerceAtLeast(min)) }, enabled = value > min) { Text("−") }
        TextButton(onClick = { change((value + step).coerceAtMost(max)) }, enabled = value < max) { Text("+") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyApp(model: StudyModel = viewModel()) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("appearance", Context.MODE_PRIVATE) }
    var palette by remember { mutableStateOf(StudyPalette.entries.firstOrNull { it.name == prefs.getString("palette", "") } ?: StudyPalette.Forest) }
    var appearance by remember { mutableStateOf(StudyAppearance.entries.firstOrNull { it.name == prefs.getString("mode", "") } ?: StudyAppearance.System) }
    var dailyGoal by remember { mutableIntStateOf(prefs.getInt("dailyGoal", 20).coerceIn(5, 200)) }
    var newLimit by remember { mutableIntStateOf(prefs.getInt("newLimit", 20).coerceIn(5, 200)) }
    var tab by rememberSaveable { mutableStateOf(StudyTab.Home) }
    var subject by rememberSaveable { mutableStateOf("Todas") }
    var unit by rememberSaveable { mutableStateOf("Todas") }
    var topic by rememberSaveable { mutableStateOf("Todos") }
    var query by rememberSaveable { mutableStateOf("") }
    var difficulty by rememberSaveable { mutableStateOf("Todos") }
    var mode by rememberSaveable { mutableStateOf("Tarjetas") }
    var errorRun by rememberSaveable { mutableStateOf<String?>(null) }
    var errorSubject by rememberSaveable { mutableStateOf<String?>(null) }
    var errorTopic by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedExam by rememberSaveable { mutableStateOf<String?>(null) }
    fun openErrors(runId: String? = null, subjectFilter: String? = null, topicFilter: String? = null) {
        errorRun = runId; errorSubject = subjectFilter; errorTopic = topicFilter
        selectedExam = null; mode = "Errores"; tab = StudyTab.Review
    }
    var selectedCard by rememberSaveable { mutableStateOf<String?>(null) }
    var revealedId by rememberSaveable { mutableStateOf<String?>(null) }
    var previewId by rememberSaveable { mutableStateOf<String?>(null) }
    var ratingPending by remember { mutableStateOf<String?>(null) }

    val cards by model.cards.collectAsState()
    val progress by model.progress.collectAsState()
    val logs by model.logs.collectAsState()
    val times by model.times.collectAsState()
    val runs by model.runs.collectAsState()
    val topicResults by model.resultsByTopic.collectAsState()
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { now = System.currentTimeMillis(); delay(1000) } }
    val states = remember(progress) { progress.associateBy { it.cardId } }
    val today = localDate(now)
    val tomorrow = today.plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    val scheduledToday = remember(progress, tomorrow, now) {
        progress.count { it.reviews > 0 && it.due in (now + 1) until tomorrow }
    }
    val expired = runs.firstOrNull { it.finishedAt == 0L && it.deadline <= now }
    LaunchedEffect(expired?.id, model.busy) {
        if (expired != null && !model.busy) model.expireExams(now)
    }
    val focus = remember(topicResults) {
        topicResults.filter { it.pending > 0 }.sortedWith(compareByDescending<TopicResult> { it.pending }
            .thenBy { if (it.examAnswers == 0) 1.0 else it.examCorrect.toDouble() / it.examAnswers }).take(3)
    }
    val reviewedToday = logs.filter { localDate(it.ratedAt) == today }
    val firstReviews = remember(logs) { logs.groupBy { it.cardId }.mapValues { (_, v) -> v.minOf { it.ratedAt } } }
    val newToday = firstReviews.count { localDate(it.value) == today }
    val filtered = remember(cards, subject, unit, topic, query, difficulty) {
        cards.filter {
            (subject == "Todas" || normalizedSubject(it.subject) == subject) &&
            (unit == "Todas" || it.unit == unit) && (topic == "Todos" || it.topic == topic) &&
            (difficulty == "Todos" || difficultyLabel(it.contentDifficulty) == difficulty) &&
            (query.isBlank() || listOf(it.question, it.answer, it.topic).any { text -> text.contains(query, ignoreCase = true) })
        }
    }
    val due = remember(filtered, states, now, newToday, newLimit) {
        filtered.filter {
            val p = states[it.id]
            (p?.due ?: 0L) <= now && ((p?.reviews ?: 0) > 0 || newToday < newLimit)
        }.sortedWith(compareBy<Card> { if ((states[it.id]?.reviews ?: 0) > 0) 0 else 1 }.thenBy { states[it.id]?.due ?: 0L })
    }
    // Keep a revealed card stable when another card becomes due in the background.
    LaunchedEffect(due.map { it.id }, ratingPending, model.busy) {
        if (ratingPending != null && !model.busy) { selectedCard = null; ratingPending = null }
        if (selectedCard !in due.map { it.id }) { selectedCard = due.firstOrNull()?.id; revealedId = null }
    }
    val current = cards.firstOrNull { it.id == selectedCard }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { it?.let { model.importUri(context, it) } }
    val export = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { it?.let { model.export(context, it) } }
    val restore = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { it?.let { model.restore(context, it) } }

    val contentState = remember(tab, mode) { LazyListState() }
    val keyboard = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
    LaunchedEffect(tab) { keyboard?.hide() }
    val owner = LocalLifecycleOwner.current
    val isStudying = tab == StudyTab.Review && !model.loading &&
        (mode == "Guía" || mode == "Errores" || (mode == "Tarjetas" && current != null) ||
        (mode == "Simulacros" && runs.any { it.id == selectedExam && it.finishedAt == 0L }))
    LaunchedEffect(isStudying, owner) {
        if (isStudying) owner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            var last = SystemClock.elapsedRealtime()
            try {
                while (true) {
                    delay(10000)
                    val tick = SystemClock.elapsedRealtime()
                    model.recordTime(System.currentTimeMillis(), tick - last)
                    last = tick
                }
            } finally {
                model.recordTime(System.currentTimeMillis(), SystemClock.elapsedRealtime() - last)
            }
        }
    }
    BackHandler(tab != StudyTab.Home || selectedExam != null) {
        when {
            tab == StudyTab.Review && selectedExam != null -> selectedExam = null
            tab == StudyTab.Review && mode != "Tarjetas" -> mode = "Tarjetas"
            else -> tab = StudyTab.Home
        }
    }

    StudyTheme(palette, appearance) {
        Scaffold(
            topBar = { TopAppBar(title = {
                Column {
                    Text(if (tab == StudyTab.Home) "Medicina UNA" else tab.label)
                    if (tab == StudyTab.Home) Text("Un paso más hacia tu ingreso", style = MaterialTheme.typography.labelMedium)
                }
            }) },
            bottomBar = {
                NavigationBar {
                    StudyTab.entries.forEach { destination ->
                        NavigationBarItem(
                            selected = tab == destination, onClick = { tab = destination },
                            icon = { Icon(destination.icon, contentDescription = destination.label) },
                            label = { Text(destination.label, maxLines = 1) },
                        )
                    }
                }
            }
        ) { padding ->
            LazyColumn(
                state = contentState,
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                if (model.loading) item { LinearProgressIndicator(Modifier.fillMaxWidth()); Text("Preparando tu banco de estudio…") }
                if (model.notice.isNotBlank()) item { Text(model.notice, style = MaterialTheme.typography.bodySmall) }
                when (tab) {
                    StudyTab.Home -> {
                        item {
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("Tu sesión de hoy", style = MaterialTheme.typography.headlineMedium)
                                    Text("${cards.count { (states[it.id]?.due ?: 0L) <= now && (states[it.id]?.reviews ?: 0) > 0 }} repasos pendientes")
                                    Text("$scheduledToday repasos programados para más tarde hoy")
                                    Text("${minOf((newLimit - newToday).coerceAtLeast(0), cards.count { (states[it.id]?.reviews ?: 0) == 0 })} tarjetas nuevas disponibles hoy")
                                    Button(onClick = {
                                        subject = "Todas"; unit = "Todas"; topic = "Todos"; query = ""; difficulty = "Todos"
                                        mode = "Tarjetas"; tab = StudyTab.Review
                                    }, enabled = !model.loading && cards.isNotEmpty()) { Text("Empezar a estudiar") }
                                }
                            }
                        }
                        item {
                            Metric("Meta de hoy", "${reviewedToday.size} / $dailyGoal repasos")
                            Text(if (reviewedToday.size >= dailyGoal) "Meta cumplida" else "Te faltan ${dailyGoal - reviewedToday.size} repasos de tarjetas.")
                            Stepper("Ajustar meta diaria", dailyGoal, 5, 200, 5) {
                                dailyGoal = it; prefs.edit().putInt("dailyGoal", it).apply()
                            }
                            LinearProgressIndicator(progress = (reviewedToday.size.toFloat() / dailyGoal).coerceAtMost(1f), modifier = Modifier.fillMaxWidth())
                        }
                        item {
                            Text("Temas para reforzar", style = MaterialTheme.typography.titleLarge)
                            if (focus.isEmpty()) Text("Acá aparecerán los temas con errores de tus simulacros.")
                            focus.forEach {
                                TextButton(onClick = { openErrors(subjectFilter = it.subject, topicFilter = it.topic) }) {
                                    Text("${it.subject} · ${it.topic} · ${it.pending} por repasar")
                                }
                            }
                            OutlinedButton(onClick = { openErrors() }) { Text("Repasar errores") }
                        }
                        item { Metric("Racha", "${streak((logs.map { localDate(it.ratedAt) } + runs.filter { it.finishedAt > 0 }.map { localDate(it.finishedAt) }).toSet(), today)} días") }
                        item { Metric("Tu banco", "${cards.size} tarjetas") }
                        item {
                            OutlinedButton(onClick = { mode = "Simulacros"; selectedExam = null; tab = StudyTab.Review }, modifier = Modifier.fillMaxWidth()) { Text("Hacer un simulacro") }
                            OutlinedButton(onClick = { mode = "Guía"; tab = StudyTab.Review }, modifier = Modifier.fillMaxWidth()) { Text("Abrir guía de repaso") }
                        }
                    }
                    StudyTab.Subjects -> {
                        item {
                            Text("Explora tu banco", style = MaterialTheme.typography.headlineMedium)
                            OutlinedTextField(value = query, onValueChange = { query = it }, label = { Text("Buscar pregunta, respuesta o tema") }, modifier = Modifier.fillMaxWidth())
                            ChoiceMenu("Materia", listOf("Todas") + cards.map { normalizedSubject(it.subject) }.distinct(), subject) {
                                subject = it; unit = "Todas"; topic = "Todos"
                            }
                            ChoiceMenu("Unidad", listOf("Todas") + cards.filter { subject == "Todas" || normalizedSubject(it.subject) == subject }.map { it.unit }.distinct(), unit) {
                                unit = it; topic = "Todos"
                            }
                            ChoiceMenu("Tema", listOf("Todos") + cards.filter { (subject == "Todas" || normalizedSubject(it.subject) == subject) && (unit == "Todas" || it.unit == unit) }.map { it.topic }.distinct(), topic) { topic = it }
                            ChoiceMenu("Dificultad", listOf("Todos", "Básica", "Intermedia", "Avanzada"), difficulty) { difficulty = it }
                            Text("${filtered.size} tarjetas · ${due.size} disponibles")
                            Button(onClick = { mode = "Tarjetas"; tab = StudyTab.Review }, enabled = due.isNotEmpty() && !model.loading) { Text("Repasar selección") }
                        }
                        items(filtered, key = { it.id }) { card ->
                            OutlinedCard(onClick = { previewId = card.id }, modifier = Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(card.topic, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                    Text(card.question, style = MaterialTheme.typography.titleMedium)
                                    Text(difficultyLabel(card.contentDifficulty), style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                    StudyTab.Review -> {
                        item { ChoiceMenu("Modo", listOf("Tarjetas", "Simulacros", "Errores", "Guía"), mode) {
                            if (it == "Errores") openErrors() else mode = it
                        } }
                        when (mode) {
                            "Simulacros" -> item { ExamScreen(model, selectedExam, { selectedExam = it }, now) { openErrors(it) } }
                            "Errores" -> item {
                                ErrorReviewScreen(model, errorRun, errorSubject, errorTopic)
                            }
                            "Guía" -> {
                                items(model.guide.split("\n").filter { it.isNotBlank() }) { line ->
                                    when {
                                        line.startsWith("# ") -> Text(line.removePrefix("# "), style = MaterialTheme.typography.headlineMedium)
                                        line.startsWith("## ") -> Text(line.removePrefix("## "), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                                        else -> Text(line.replace("**", "").removePrefix("- "))
                                    }
                                }
                            }
                            else -> item {
                                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                    Text(if (subject == "Todas") "Repaso diario" else subject, style = MaterialTheme.typography.headlineMedium)
                                    if (subject != "Todas" || query.isNotBlank() || difficulty != "Todos") {
                                        TextButton(onClick = { subject = "Todas"; unit = "Todas"; topic = "Todos"; query = ""; difficulty = "Todos" }) { Text("Quitar filtros") }
                                    }
                                    if (current == null) {
                                        Text("Completaste los repasos disponibles de esta selección.")
                                        val next = filtered.mapNotNull { states[it.id]?.due }.filter { it > now }.minOrNull()
                                        if (next != null) Text("Próximo repaso en ${intervalLabel(next - now)}.")
                                        if (newToday >= newLimit) Text("Alcanzaste tu límite de tarjetas nuevas de hoy.")
                                    } else {
                                        Text("${current.unit} · ${current.topic}", style = MaterialTheme.typography.labelLarge)
                                        Text(difficultyLabel(current.contentDifficulty), color = MaterialTheme.colorScheme.primary)
                                        Card {
                                            Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                                Text(current.question, style = MaterialTheme.typography.headlineSmall)
                                                if (revealedId == current.id) {
                                                    HorizontalDivider()
                                                    Text(current.answer, style = MaterialTheme.typography.titleLarge)
                                                    if (current.explanation.isNotBlank()) Text(current.explanation)
                                                    Text("Fuente · ${current.source} ${current.page}", style = MaterialTheme.typography.bodySmall)
                                                }
                                            }
                                        }
                                        if (revealedId == current.id) {
                                            Text("¿Cómo te fue al recordarla?", style = MaterialTheme.typography.titleMedium)
                                            listOf("Otra vez", "Difícil", "Bien", "Fácil").forEachIndexed { index, label ->
                                                val next = Fsrs.next(states[current.id], index + 1, now)
                                                OutlinedButton(
                                                    onClick = {
                                                        ratingPending = current.id
                                                        revealedId = null
                                                        model.rate(current, index + 1, states[current.id]?.reviews ?: 0)
                                                    },
                                                    enabled = !model.busy && ratingPending == null,
                                                    modifier = Modifier.fillMaxWidth(),
                                                ) { Text("$label · ${intervalLabel(next.due - now)}") }
                                            }
                                        } else Button(onClick = { revealedId = current.id }, enabled = !model.busy && ratingPending == null, modifier = Modifier.fillMaxWidth()) { Text("Mostrar respuesta") }
                                        Text("${due.size} tarjetas disponibles en esta selección", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                    StudyTab.Stats -> item { StatisticsScreen(cards, progress, logs, times, runs, topicResults, now) {
                        selectedExam = it; mode = "Simulacros"; tab = StudyTab.Review
                    } }
                    StudyTab.Settings -> {
                        item { Text("Hazla tuya", style = MaterialTheme.typography.headlineMedium) }
                        item {
                            ChoiceMenu("Colores", StudyPalette.entries.map { it.label }, palette.label) { label ->
                                palette = StudyPalette.entries.first { it.label == label }
                                prefs.edit().putString("palette", palette.name).apply()
                            }
                            ChoiceMenu("Apariencia", StudyAppearance.entries.map { it.label }, appearance.label) { label ->
                                appearance = StudyAppearance.entries.first { it.label == label }
                                prefs.edit().putString("mode", appearance.name).apply()
                            }
                            Stepper("Meta de repasos", dailyGoal, 5, 200, 5) { dailyGoal = it; prefs.edit().putInt("dailyGoal", it).apply() }
                            Stepper("Nuevas por día", newLimit, 5, 200, 5) { newLimit = it; prefs.edit().putInt("newLimit", it).apply() }
                            Text("Los repasos pendientes siempre se incluyen. El límite se aplica a las tarjetas vistas por primera vez.",
                                style = MaterialTheme.typography.bodySmall)
                        }
                        item {
                            Text("Contenido y copias", style = MaterialTheme.typography.titleLarge)
                            OutlinedButton(onClick = { launcher.launch(arrayOf("application/json", "text/plain", "*/*")) }, enabled = !model.busy && !model.loading) { Text("Importar tarjetas") }
                            Text("Acepta archivos JSON de hasta 50.000 tarjetas, organizadas por materia, unidad y tema. Los IDs existentes se omiten.",
                                style = MaterialTheme.typography.bodySmall)
                            OutlinedButton(onClick = { export.launch("MedicinaUNA-copia.json") }, enabled = !model.busy && !model.loading) { Text("Guardar copia del progreso") }
                            OutlinedButton(onClick = { restore.launch(arrayOf("application/json", "text/plain", "*/*")) }, enabled = !model.busy && !model.loading) { Text("Recuperar una copia") }
                            Text("Versión 0.2.0 · Estudio sin conexión", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
        cards.firstOrNull { it.id == previewId }?.let { card ->
            AlertDialog(onDismissRequest = { previewId = null },
                title = { Text(card.question) },
                text = {
                    androidx.compose.foundation.lazy.LazyColumn {
                        item { Text(card.answer, style = MaterialTheme.typography.titleMedium) }
                        item { Text(card.explanation) }
                        item { Text("${card.source} ${card.page}", style = MaterialTheme.typography.bodySmall) }
                    }
                },
                confirmButton = { TextButton(onClick = { previewId = null }) { Text("Cerrar") } })
        }
    }
}
fun difficultyLabel(value: Int): String = when (value) { 3 -> "Avanzada"; 2 -> "Intermedia"; else -> "Básica" }
fun intervalLabel(millis: Long): String = when {
    millis < 3600000 -> "${(millis / 60000).coerceAtLeast(1)} min"
    millis < 86400000 -> "${millis / 3600000} h"
    else -> "${millis / 86400000} días"
}
