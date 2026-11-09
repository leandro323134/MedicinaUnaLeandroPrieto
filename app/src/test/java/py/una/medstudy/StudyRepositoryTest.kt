package py.una.medstudy

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class StudyRepositoryTest {
    private lateinit var context: Context
    private lateinit var db: StudyDatabase
    private lateinit var repository: StudyRepository
    private val card = Card("old-card", "Pregunta", "Respuesta", "Materia", "Unidad", "Tema", "Fuente")
    @Before fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, StudyDatabase::class.java).allowMainThreadQueries().build()
        repository = StudyRepository(db)
    }
    @After fun close() { db.close() }

    @Test fun migrationPreservesVersionOneCardsHistoryAndSchedule() = runBlocking {
        val name = "migration-test.db"
        context.deleteDatabase(name)
        val file = context.getDatabasePath(name)
        file.parentFile!!.mkdirs()
        SQLiteDatabase.openOrCreateDatabase(file, null).use { old ->
            old.execSQL("CREATE TABLE cards (id TEXT NOT NULL PRIMARY KEY, question TEXT NOT NULL, answer TEXT NOT NULL, subject TEXT NOT NULL, unit TEXT NOT NULL, topic TEXT NOT NULL, source TEXT NOT NULL, page TEXT NOT NULL)")
            old.execSQL("CREATE INDEX index_cards_subject_unit_topic ON cards(subject, unit, topic)")
            old.execSQL("CREATE TABLE progress (cardId TEXT NOT NULL PRIMARY KEY, stability REAL NOT NULL, difficulty REAL NOT NULL, lastReview INTEGER NOT NULL, due INTEGER NOT NULL, reviews INTEGER NOT NULL, FOREIGN KEY(cardId) REFERENCES cards(id) ON DELETE CASCADE)")
            old.execSQL("CREATE TABLE review_log (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, cardId TEXT NOT NULL, ratedAt INTEGER NOT NULL, rating INTEGER NOT NULL, stability REAL NOT NULL, difficulty REAL NOT NULL, due INTEGER NOT NULL)")
            old.execSQL("CREATE INDEX index_review_log_cardId ON review_log(cardId)")
            old.execSQL("INSERT INTO cards VALUES ('legacy','Pregunta','Respuesta','Biología','Unidad','Tema','Fuente','7')")
            old.execSQL("INSERT INTO progress VALUES ('legacy',28.5,4.2,1000,9999999,7)")
            old.execSQL("INSERT INTO review_log VALUES (1,'legacy',1000,3,28.5,4.2,9999999)")
            old.version = 1
        }
        val migrated = StudyDatabase.open(context, name)
        try {
            val dao = migrated.dao()
            assertEquals("Respuesta", dao.allCards().single().answer)
            assertEquals("", dao.allCards().single().explanation)
            assertEquals(1, dao.allCards().single().contentDifficulty)
            assertEquals(7, dao.getProgress("legacy")!!.reviews)
            assertEquals(9999999L, dao.getProgress("legacy")!!.due)
            assertEquals(28.5, dao.getProgress("legacy")!!.stability, 0.0)
            assertEquals(1, dao.allLogs().size)
            dao.addTime(StudyTime("test", 100, 5000))
            assertEquals(5000, dao.allTimes().single().millis.toInt())
        } finally { migrated.close(); context.deleteDatabase(name) }
    }

    @Test fun doubleRatingIsIdempotentAndImportPreservesProgress() = runBlocking {
        repository.dao.add(listOf(card))
        assertTrue(repository.rate(card.id, 3, 0, 100000))
        assertFalse(repository.rate(card.id, 4, 0, 100001))
        assertEquals(1, repository.dao.allLogs().size)
        val json = """{"formatVersion":1,"cards":[{"id":"old-card","question":"Editada","answer":"Respuesta","subject":"Materia","unit":"Unidad","topic":"Tema","explanation":"Explicación","difficulty":3}]}"""
        assertEquals(0, repository.importCards(json))
        assertEquals("Pregunta", repository.dao.allCards().single().question)
        repository.importCards(json, updateBundledContent = true)
        assertEquals("Explicación", repository.dao.allCards().single().explanation)
        assertEquals(1, repository.dao.getProgress(card.id)!!.reviews)
        assertEquals(1, repository.dao.allLogs().size)
    }

    @Test fun invalidImportWritesNothing() = runBlocking {
        val json = """{"formatVersion":1,"cards":[{"id":"ok","question":"Q","answer":"A","subject":"S","unit":"U","topic":"T"},{"id":"bad","question":"","answer":"A","subject":"S","unit":"U","topic":"T"}]}"""
        assertTrue(runCatching { repository.importCards(json) }.isFailure)
        assertTrue(repository.dao.allCards().isEmpty())
    }

    @Test fun examSurvivesReopenAndDeadlineRejectsLateAnswers() = runBlocking {
        val name = "exam-test.db"
        context.deleteDatabase(name)
        var stored = StudyDatabase.open(context, name)
        var repo = StudyRepository(stored)
        val exam = PracticeExam("one", "Práctica", 1,
            listOf(ExamQuestion("Química", "Pregunta", listOf("A", "B"), 1, "Porque B")))
        val id = repo.startExam(exam, 1000)
        repo.answer(id, 0, 1, 2000)
        assertEquals(id, repo.startExam(exam, 3000))
        stored.close()
        stored = StudyDatabase.open(context, name)
        repo = StudyRepository(stored)
        try {
            assertEquals(listOf(1), parseAnswers(repo.dao.exam(id)!!.answersJson))
            repo.answer(id, 0, 0, 61001)
            repo.finish(id, 70000)
            assertEquals(61000L, repo.dao.exam(id)!!.finishedAt)
            assertEquals(1 to 1, repo.dao.exam(id)!!.score())
            assertEquals(1, repo.dao.allExams().size)
        } finally { stored.close(); context.deleteDatabase(name) }
    }

    @Test fun backupRoundTripAndRepeatedRestoreKeepOneHistory() = runBlocking {
        repository.dao.add(listOf(card))
        repository.rate(card.id, 3, 0, 5000)
        repository.dao.addTime(StudyTime("time", 5000, 10000))
        val backup = repository.exportBackup()
        val destination = Room.inMemoryDatabaseBuilder(context, StudyDatabase::class.java).allowMainThreadQueries().build()
        try {
            val restored = StudyRepository(destination)
            restored.restoreBackup(backup)
            restored.restoreBackup(backup)
            assertEquals(1, restored.dao.allCards().size)
            assertEquals(1, restored.dao.allLogs().size)
            assertEquals(1, restored.dao.allTimes().size)
            assertEquals(repository.dao.allProgress(), restored.dao.allProgress())
        } finally { destination.close() }
    }

    @Test fun allBundledContentAndExamReferencesLoad() = runBlocking {
        for (name in listOf("starter.json", "tarjetas.json", "programa_2027.json")) {
            context.assets.open(name).bufferedReader().use { repository.importCards(it.readText(), true) }
        }
        val bundledCards = repository.dao.allCards()
        assertEquals(1285, bundledCards.size)
        val programCards = bundledCards.filter { it.id.startsWith("programa27-") }
        assertEquals(1052, programCards.size)
        val chemistryCards = programCards.filter { it.subject == "Química" }
        val chemistryAudit = chemistryCards.filter { it.id.startsWith("programa27-quimica-auditoria-") }
        assertEquals(451, chemistryCards.size)
        assertEquals(113, chemistryAudit.size)
        assertEquals(17, chemistryAudit.map { it.unit }.toSet().size)
        assertTrue(chemistryAudit.any { it.topic == "Gráficos de orden de reacción" })
        assertTrue(chemistryAudit.any { it.topic == "Temperatura y Arrhenius" })
        assertTrue(chemistryAudit.any { it.topic == "pH y pOH" })
        val languageCards = programCards.filter { it.subject == "Lenguaje" }
        assertEquals(181, languageCards.size)
        assertEquals(83, languageCards.count { it.id.startsWith("programa27-lenguaje-auditoria-") })
        assertTrue(languageCards.any { it.topic == "Textos instrumentales" })
        assertTrue(languageCards.any { it.topic == "Funciones de los relativos" })
        val additions = programCards.filter { it.id.startsWith("programa27-refuerzo-") }
        assertEquals(mapOf("Biología" to 37, "Lenguaje" to 6, "Química" to 12),
            additions.groupingBy { it.subject }.eachCount())
        assertEquals(setOf("Biología", "Física", "Lenguaje", "Matemática", "Química"),
            programCards.map { it.subject }.toSet())
        assertTrue(programCards.all { it.source.isNotBlank() && it.explanation.isNotBlank() })
        assertTrue(programCards.any { it.question.contains("¬(p ∧ q)") })
        assertTrue(programCards.any { it.question.contains("¬(p ∨ q)") })
        assertTrue(repository.dao.allCards().any { it.explanation.isNotBlank() && it.contentDifficulty == 3 })
        val exams = loadExams(context)
        assertEquals(7, exams.size)
        assertTrue(exams.all { it.questions.isNotEmpty() })
    }

    @Test fun largeImportKeepsIdsAndAvoidsDuplicates() = runBlocking {
        val cards = org.json.JSONArray()
        repeat(1200) { i ->
            cards.put(org.json.JSONObject().put("id", "large-$i").put("question", "Pregunta $i")
                .put("answer", "Respuesta $i").put("subject", "Materia")
                .put("unit", "Unidad ${i / 100}").put("topic", "Tema ${i / 10}"))
        }
        val text = org.json.JSONObject().put("formatVersion", 1).put("cards", cards).toString()
        assertEquals(1200, repository.importCards(text))
        assertEquals(0, repository.importCards(text))
        assertEquals(1200, repository.dao.allCards().size)
    }

    @Test fun streakHandlesYesterdayAndMissingDays() {
        val today = LocalDate.of(2026, 11, 8)
        assertEquals(2, streak(setOf(today.minusDays(1), today.minusDays(2)), today))
        assertEquals(1, streak(setOf(today, today.minusDays(2)), today))
        assertEquals(0, streak(emptySet(), today))
    }
}
