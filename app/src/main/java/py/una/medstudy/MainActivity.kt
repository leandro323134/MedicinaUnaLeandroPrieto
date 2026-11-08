package py.una.medstudy

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.math.*

@Entity(tableName="cards", indices=[Index(value=["subject","unit","topic"])])
data class Card(@PrimaryKey val id:String, val question:String, val answer:String, val subject:String, val unit:String, val topic:String, val source:String, val page:String="")
@Entity(tableName="progress", foreignKeys=[ForeignKey(entity=Card::class,parentColumns=["id"],childColumns=["cardId"],onDelete=ForeignKey.CASCADE)])
data class Progress(@PrimaryKey val cardId:String,val stability:Double=0.0,val difficulty:Double=0.0,val lastReview:Long=0L,val due:Long=0L,val reviews:Int=0)
@Entity(tableName="review_log",indices=[Index("cardId")])
data class ReviewLog(@PrimaryKey(autoGenerate=true) val id:Long=0,val cardId:String,val ratedAt:Long,val rating:Int,val stability:Double,val difficulty:Double,val due:Long)
@Dao interface CardsDao {
    @Query("SELECT * FROM cards ORDER BY subject,unit,topic,id") fun cards():Flow<List<Card>>
    @Query("SELECT * FROM progress") fun progress():Flow<List<Progress>>
    @Query("SELECT * FROM progress WHERE cardId=:id") suspend fun getProgress(id:String):Progress?
    @Insert(onConflict=OnConflictStrategy.IGNORE) suspend fun add(cards:List<Card>):List<Long>
    @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun save(progress:Progress)
    @Insert suspend fun log(review:ReviewLog)
}
@Database(entities=[Card::class,Progress::class,ReviewLog::class],version=1,exportSchema=false)
abstract class StudyDatabase:RoomDatabase(){ abstract fun dao():CardsDao }

// FSRS-5 default model at desired retention 0.90, with short learning steps.
object Fsrs {
    private val w=doubleArrayOf(0.40255,1.18385,3.173,15.69105,7.1949,0.5345,1.4604,0.0046,1.54575,0.1192,1.01925,1.9395,0.11,0.29605,2.2698,0.2315,2.9898,0.51655,0.6621)
    private const val day=86400000L
    private fun d0(g:Int)=(w[4]-exp(w[5]*(g-1))+1).coerceIn(1.0,10.0)
    fun next(p:Progress?,g:Int,now:Long):Progress {
        require(g in 1..4)
        val old=p ?: Progress("")
        val s:Double; val d:Double
        if(old.reviews==0){ s=w[g-1]; d=d0(g) }
        else {
            val previousS=old.stability.coerceAtLeast(0.01)
            val elapsed=((now-old.lastReview).coerceAtLeast(0L)/day.toDouble())
            val r=(1+19.0/81.0*elapsed/previousS).pow(-0.5)
            val delta=-w[6]*(g-3)
            d=(w[7]*d0(4)+(1-w[7])*(old.difficulty+delta*(10-old.difficulty)/9)).coerceIn(1.0,10.0)
            s=when {
                elapsed < 1.0 -> {
                    val candidate=previousS*exp(w[17]*(g-3+w[18]))
                    if(g==1) candidate else max(previousS,candidate)
                }
                g==1 -> w[11]*d.pow(-w[12])*((previousS+1).pow(w[13])-1)*exp(w[14]*(1-r))
                else -> previousS*(1+exp(w[8])*(11-d)*previousS.pow(-w[9])*(exp(w[10]*(1-r))-1)*(if(g==2) w[15] else 1.0)*(if(g==4) w[16] else 1.0))
            }
        }
        val stability=s.coerceIn(0.01,36500.0)
        val interval=(stability/(19.0/81.0)*(0.9.pow(-2.0)-1)).roundToLong().coerceIn(1,36500)
        val due=when(g){1->now+TimeUnit.MINUTES.toMillis(1);2->if(old.reviews==0) now+TimeUnit.MINUTES.toMillis(10) else now+interval*day;else->now+interval*day}
        return old.copy(stability=stability,difficulty=d,lastReview=now,due=due,reviews=old.reviews+1)
    }
}
class StudyModel(app:android.app.Application):AndroidViewModel(app){
    private val dao=Room.databaseBuilder(app,StudyDatabase::class.java,"study.db").build().dao()
    val cards=dao.cards().stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList())
    val progress=dao.progress().stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList())
    var notice by mutableStateOf(""); private set
    init {
        viewModelScope.launch {
            runCatching {
                for (name in listOf("starter.json", "tarjetas.json")) {
                    app.assets.open(name).bufferedReader().use { importJson(it.readText()) }
                }
            }.onFailure { notice = "No se pudo cargar el banco de estudio: ${it.message}" }
        }
    }
    private suspend fun importJson(text:String):Int {
        val root=JSONObject(text); require(root.optInt("formatVersion")==1){"Formato no compatible"}
        val array=root.getJSONArray("cards"); require(array.length()<=50000){"Paquete demasiado grande"}
        val entries=(0 until array.length()).map { i-> val o=array.getJSONObject(i); Card(o.getString("id").trim(),o.getString("question").trim(),o.getString("answer").trim(),o.getString("subject").trim(),o.getString("unit").trim(),o.getString("topic").trim(),o.optString("source"),o.optString("page")) }
        require(entries.all { it.id.isNotBlank()&&it.question.isNotBlank()&&it.answer.isNotBlank()&&it.subject.isNotBlank() }){"Tarjeta incompleta"}
        require(entries.map { it.id }.distinct().size==entries.size){"IDs repetidos en el paquete"}
        return dao.add(entries).count { it!=-1L }
    }
    fun importUri(context:Context,uri:Uri){ viewModelScope.launch { runCatching { val text=context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: error("Archivo vacío"); importJson(text) }.onSuccess { notice="$it tarjetas nuevas importadas. El progreso anterior se conservó." }.onFailure { notice="Error al importar: ${it.message}" } } }
    fun rate(card:Card,g:Int){ viewModelScope.launch { val now=System.currentTimeMillis();val p=Fsrs.next(dao.getProgress(card.id)?.copy(cardId=card.id) ?: Progress(card.id),g,now);dao.save(p);dao.log(ReviewLog(cardId=card.id,ratedAt=now,rating=g,stability=p.stability,difficulty=p.difficulty,due=p.due)) } }
}
class MainActivity:ComponentActivity(){override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent { StudyApp() }}}
private enum class StudyTab(val label: String, val icon: String) {
    Home("Inicio", "⌂"), Subjects("Materias", "▤"), Review("Estudiar", "▣"),
    Stats("Estadísticas", "▥"), Settings("Ajustes", "⚙");
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyApp(model: StudyModel = viewModel()) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val preferences = remember(context) { context.getSharedPreferences("appearance", Context.MODE_PRIVATE) }
    var palette by remember {
        mutableStateOf(StudyPalette.entries.firstOrNull { it.name == preferences.getString("palette", null) } ?: StudyPalette.Forest)
    }
    var appearance by remember {
        mutableStateOf(StudyAppearance.entries.firstOrNull { it.name == preferences.getString("mode", null) } ?: StudyAppearance.System)
    }
    val exams = remember(context) { runCatching { loadExams(context) }.getOrDefault(emptyList()) }
    var showExams by rememberSaveable { mutableStateOf(false) }
    var tab by rememberSaveable { mutableStateOf(StudyTab.Home) }
    var subject by rememberSaveable { mutableStateOf<String?>(null) }
    var unit by rememberSaveable { mutableStateOf<String?>(null) }
    var topic by rememberSaveable { mutableStateOf<String?>(null) }
    var revealedId by rememberSaveable { mutableStateOf<String?>(null) }
    val cards by model.cards.collectAsState()
    val states by model.progress.collectAsState()
    val state = remember(states) { states.associateBy { it.cardId } }
    val subset = remember(cards, subject, unit, topic) {
        cards.filter { (subject == null || it.subject == subject) &&
            (unit == null || it.unit == unit) && (topic == null || it.topic == topic) }
    }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            kotlinx.coroutines.delay(1000)
        }
    }
    val due = remember(subset, state, now) {
        subset.filter { (state[it.id]?.due ?: 0L) <= now }
            .sortedBy { state[it.id]?.due ?: Long.MAX_VALUE }
    }
    val current = due.firstOrNull()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
        if (it != null) model.importUri(context, it)
    }

    StudyTheme(palette, appearance) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (tab == StudyTab.Home) "Medicina UNA" else tab.label) },
                    navigationIcon = {
                        if (tab == StudyTab.Subjects && subject != null) {
                            TextButton(onClick = {
                                when {
                                    topic != null -> topic = null
                                    unit != null -> unit = null
                                    else -> subject = null
                                }
                            }) { Text("Atrás") }
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar {
                    StudyTab.entries.forEach { destination ->
                        NavigationBarItem(
                            selected = destination == tab,
                            onClick = { if (!showExams || destination == StudyTab.Review) tab = destination },
                            enabled = !showExams || destination == StudyTab.Review,
                            icon = { Text(destination.icon) },
                            label = { Text(destination.label, maxLines = 1) },
                            alwaysShowLabel = false,
                        )
                    }
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                when (tab) {
                    StudyTab.Home -> item {
                        val totalDue = cards.count { (state[it.id]?.due ?: 0L) <= now }
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                            Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Tu repaso de hoy", style = MaterialTheme.typography.headlineSmall)
                                Text("$totalDue pendientes · ${cards.size} tarjetas en el banco")
                                Button(onClick = {
                                    subject = null
                                    unit = null
                                    topic = null
                                    tab = StudyTab.Review
                                }, enabled = totalDue > 0) { Text("Repasar ahora") }
                            }
                        }
                    }
                    StudyTab.Subjects -> {
                        val groups = when {
                            subject == null -> subset.groupingBy { it.subject }.eachCount()
                            unit == null -> subset.groupingBy { it.unit }.eachCount()
                            topic == null -> subset.groupingBy { it.topic }.eachCount()
                            else -> emptyMap()
                        }
                        item {
                            Text(topic ?: unit ?: subject ?: "Elige una materia", style = MaterialTheme.typography.headlineSmall)
                            if (subject != null) {
                                Text("${subset.size} tarjetas · ${due.size} pendientes")
                                Button(onClick = { tab = StudyTab.Review }, enabled = due.isNotEmpty()) {
                                    Text("Repasar esta selección")
                                }
                            }
                        }
                        items(groups.entries.toList(), key = { it.key }) { (name, count) ->
                            OutlinedButton(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    when {
                                        subject == null -> subject = name
                                        unit == null -> unit = name
                                        else -> topic = name
                                    }
                                }
                            ) { Text("$name · $count") }
                        }
                        if (topic != null) items(subset, key = { it.id }) { Text(it.question) }
                    }
                    StudyTab.Review -> if (showExams) {
                        item { ExamScreen(exams, onBack = { showExams = false }) }
                    } else item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (exams.isNotEmpty()) OutlinedButton(onClick = { showExams = true }) {
                                Text("Simulacros")
                            }
                            Text(topic ?: unit ?: subject ?: "Todas las materias")
                            if (subject != null) TextButton(onClick = {
                                subject = null
                                unit = null
                                topic = null
                            }) { Text("Repasar todas") }
                            if (current == null) {
                                Text("No hay tarjetas pendientes en esta selección.")
                            } else {
                                Text("${current.subject} · ${current.topic}", style = MaterialTheme.typography.labelLarge)
                                Text(current.question, style = MaterialTheme.typography.headlineSmall)
                                if (revealedId == current.id) {
                                    HorizontalDivider()
                                    Text(current.answer, style = MaterialTheme.typography.bodyLarge)
                                    Text("Fuente: ${current.source} ${current.page}", style = MaterialTheme.typography.bodySmall)
                                    listOf("Otra vez", "Difícil", "Bien", "Fácil").forEachIndexed { index, label ->
                                        Button(onClick = {
                                            revealedId = null
                                            model.rate(current, index + 1)
                                        }, modifier = Modifier.fillMaxWidth()) { Text(label) }
                                    }
                                } else Button(onClick = { revealedId = current.id }) { Text("Mostrar respuesta") }
                            }
                        }
                    }
                    StudyTab.Stats -> item {
                        Text("Tu progreso", style = MaterialTheme.typography.headlineSmall)
                        Text("${states.count { it.reviews > 0 }} tarjetas repasadas")
                        Text("${states.sumOf { it.reviews }} repasos registrados")
                    }
                    StudyTab.Settings -> {
                        item { Text("Colores", style = MaterialTheme.typography.headlineSmall) }
                        items(StudyPalette.entries) { option ->
                            FilterChip(selected = palette == option, onClick = {
                                palette = option
                                preferences.edit().putString("palette", option.name).apply()
                            }, label = { Text(option.label) })
                        }
                        item { Text("Apariencia", style = MaterialTheme.typography.titleLarge) }
                        items(StudyAppearance.entries) { option ->
                            FilterChip(selected = appearance == option, onClick = {
                                appearance = option
                                preferences.edit().putString("mode", option.name).apply()
                            }, label = { Text(option.label) })
                        }
                        item {
                            OutlinedButton(onClick = { launcher.launch(arrayOf("application/json", "text/plain", "*/*")) }) {
                                Text("Importar tarjetas JSON")
                            }
                            if (model.notice.isNotBlank()) Text(model.notice)
                        }
                    }
                }
            }
        }
    }
}
