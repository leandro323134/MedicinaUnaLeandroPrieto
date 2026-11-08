package py.una.medstudy

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.UUID

class StudyModel(app: Application) : AndroidViewModel(app) {
    private val db = StudyDatabase.open(app)
    val repository = StudyRepository(db)
    private val dao = repository.dao
    val cards = dao.cards().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val progress = dao.progress().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val logs = dao.logs().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val times = dao.times().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val runs = dao.exams().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    var exams by mutableStateOf<List<PracticeExam>>(emptyList()); private set
    var guide by mutableStateOf(""); private set
    var notice by mutableStateOf(""); private set
    var loading by mutableStateOf(true); private set
    var busy by mutableStateOf(false); private set

    init {
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    for (name in listOf("starter.json", "tarjetas.json")) {
                        app.assets.open(name).bufferedReader().use {
                            repository.importCards(it.readText(), updateBundledContent = true)
                        }
                    }
                    val loadedExams = loadExams(app)
                    val loadedGuide = app.assets.open("guia.md").bufferedReader().use { it.readText() }
                    loadedExams to loadedGuide
                }
            }.onSuccess { (loadedExams, loadedGuide) ->
                exams = loadedExams
                guide = loadedGuide
            }.onFailure { notice = "No se pudo cargar el contenido. ${it.message}" }
            loading = false
        }
    }

    private fun operation(block: suspend () -> Unit) {
        if (busy) return
        busy = true
        viewModelScope.launch {
            try { block() }
            catch (e: Exception) { notice = e.message ?: "No se pudo completar la operación" }
            finally { busy = false }
        }
    }

    fun rate(card: Card, rating: Int, expectedReviews: Int) = operation {
        repository.rate(card.id, rating, expectedReviews, System.currentTimeMillis())
    }

    private suspend fun read(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(uri)?.bufferedReader()?.use {
            val result = StringBuilder()
            val buffer = CharArray(8192)
            var read = it.read(buffer)
            while (read >= 0) {
                result.append(buffer, 0, read)
                require(result.length <= 30_000_000) { "El archivo supera el tamaño admitido" }
                read = it.read(buffer)
            }
            result.toString()
        } ?: error("No se pudo abrir el archivo")
    }

    fun importUri(context: Context, uri: Uri) = operation {
        val text = read(context, uri)
        val added = withContext(Dispatchers.IO) { repository.importCards(text) }
        notice = "$added tarjetas nuevas importadas."
    }

    fun export(context: Context, uri: Uri) = operation {
        val text = withContext(Dispatchers.IO) { repository.exportBackup() }
        withContext(Dispatchers.IO) {
            context.contentResolver.openOutputStream(uri, "wt")?.bufferedWriter()?.use { it.write(text) }
                ?: error("No se pudo guardar la copia")
        }
        notice = "Copia guardada con tarjetas, repasos, tiempo y simulacros."
    }

    fun restore(context: Context, uri: Uri) = operation {
        val text = read(context, uri)
        withContext(Dispatchers.IO) { repository.restoreBackup(text) }
        notice = "Copia incorporada. Se conserva el progreso más reciente."
    }

    fun start(exam: PracticeExam, select: (String) -> Unit) = operation {
        select(repository.startExam(exam, System.currentTimeMillis()))
    }
    fun answer(id: String, index: Int, option: Int) = operation {
        repository.answer(id, index, option, System.currentTimeMillis())
    }
    fun move(id: String, index: Int) = operation { repository.position(id, index) }
    fun finish(id: String) = operation { repository.finish(id, System.currentTimeMillis()) }
    fun recordTime(at: Long, millis: Long) {
        if (millis <= 0) return
        viewModelScope.launch {
            runCatching { dao.addTime(StudyTime(UUID.randomUUID().toString(), at, millis.coerceAtMost(60000))) }
                .onFailure { notice = "No se pudo guardar el tiempo de estudio." }
        }
    }
}
