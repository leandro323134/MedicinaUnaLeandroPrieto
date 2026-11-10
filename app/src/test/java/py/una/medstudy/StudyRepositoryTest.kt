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

    @Test fun bundledUpdatePreservesProgressSettingsAndInFlightExam() = runBlocking {
        // Representa contenido anterior con un ID estable que recibe una corrección académica.
        val original = card.copy(id = "len-0027", question = "Pregunta anterior", answer = "Respuesta anterior")
        repository.dao.add(listOf(original))
        repository.rate(original.id, 3, 0, 1000)
        val prefs = context.getSharedPreferences("appearance", Context.MODE_PRIVATE)
        prefs.edit().putString("palette", "Forest").putString("mode", "Dark")
            .putInt("dailyGoal", 35).putInt("newLimit", 10).commit()
        val question = ExamQuestion("Lenguaje", "Enunciado anterior", listOf("A", "B"), 1,
            "Explicación anterior", id = "saved-question", topic = "Tema anterior")
        val examId = repository.startExam(PracticeExam("upgrade", "En curso", 10,
            listOf(question, question.copy(id = "saved-question-2", text = "Otra pregunta"))), 2000)
        repository.answer(examId, 0, 1, 3000)
        repository.position(examId, 1)
        repository.reviewError("previous-session", question, 1, 3500)
        val previousProgress = repository.dao.allProgress()
        val previousLogs = repository.dao.allLogs()
        val previousExam = repository.dao.exam(examId)
        val previousReviews = repository.dao.allErrorReviews()
        repeat(2) {
            for (name in listOf("starter.json", "tarjetas.json", "programa_2027.json")) {
                context.assets.open(name).bufferedReader().use { repository.importCards(it.readText(), true) }
            }
        }
        assertEquals(1449, repository.dao.allCards().size)
        assertTrue(repository.dao.allCards().single { it.id == original.id }.question.contains("capacidad"))
        assertEquals(previousProgress, repository.dao.allProgress())
        assertEquals(previousLogs, repository.dao.allLogs())
        assertEquals(previousExam, repository.dao.exam(examId))
        assertEquals(previousReviews, repository.dao.allErrorReviews())
        assertEquals("Forest", prefs.getString("palette", null))
        assertEquals("Dark", prefs.getString("mode", null))
        assertEquals(35, prefs.getInt("dailyGoal", 0))
        assertEquals(10, prefs.getInt("newLimit", 0))
        repository.expireExams(previousExam!!.deadline)
        assertEquals(previousExam.copy(finishedAt = previousExam.deadline), repository.dao.exam(examId))
        prefs.edit().clear().commit()
        Unit
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
        assertEquals(1449, bundledCards.size)
        val programCards = bundledCards.filter { it.id.startsWith("programa27-") }
        assertEquals(1216, programCards.size)
        val mathCards = programCards.filter { it.subject == "Matemática" }
        val mathAudit = mathCards.filter { it.id.startsWith("programa27-matematica-auditoria-") }
        assertEquals(186, mathCards.size)
        assertEquals(85, mathAudit.size)
        assertEquals(10, mathAudit.map { it.unit }.toSet().size)
        assertTrue(mathAudit.any { it.topic == "Cuantificadores anidados" })
        assertTrue(mathAudit.any { it.topic == "Porcentajes sucesivos" })
        assertTrue(mathAudit.any { it.topic == "Capacidad de un recipiente" })
        val physicsCards = programCards.filter { it.subject == "Física" }
        val physicsAudit = physicsCards.filter { it.id.startsWith("programa27-fisica-auditoria-") }
        assertEquals(185, physicsCards.size)
        assertEquals(79, physicsAudit.size)
        assertEquals(10, physicsAudit.map { it.unit }.toSet().size)
        assertTrue(physicsAudit.any { it.topic == "Ley de Gauss y simetría" })
        assertTrue(physicsAudit.any { it.topic == "Doppler con fuente móvil" })
        assertTrue(physicsAudit.any { it.topic == "Imagen virtual en lente convergente" })
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

    @Test fun balancedSelectionAvoidsDuplicatesAndFillsFromAvailableQuestions() {
        val bank = loadQuestionBank(context)
        assertEquals(311, bank.size)
        assertEquals(bank.size, bank.map { it.id }.toSet().size)
        val selection = selectQuestions(bank + bank.take(8), null, 25, kotlin.random.Random(1))
        assertEquals(25, selection.size)
        assertEquals(setOf(5), selection.groupingBy { it.subject }.eachCount().values.toSet())
        assertEquals(25, selection.map { it.id }.distinct().size)
        val math = selectQuestions(bank, "Matemática", 120, kotlin.random.Random(2))
        assertEquals(38, math.size)
        assertTrue(math.all { it.subject == "Matemática" })
        assertEquals(bank.size, selectQuestions(bank, null, 500).size)
    }

    @Test fun errorPracticeRequiresSeparateAttemptsAndDoesNotChangeExam() = runBlocking {
        val q = ExamQuestion("Química", "Pregunta", listOf("A", "B"), 1, "Porque B", id = "q", topic = "Tema")
        val exam = PracticeExam("one", "Práctica", 1, listOf(q))
        val id = repository.startExam(exam, 1000)
        repository.answer(id, 0, 0, 2000)
        repository.finish(id, 3000)
        val original = daoRun(id)
        suspend fun streak() = errorItems(examOutcomes(repository.dao.allExams(), listOf(q)),
            repository.dao.allErrorReviews()).single().consecutive
        assertEquals(0, streak())
        assertTrue(repository.reviewError("s1", q, 1, 4000))
        assertFalse(repository.reviewError("s1", q, 1, 4001))
        assertEquals(1, streak())
        assertTrue(repository.reviewError("s2", q, 0, 5000))
        assertEquals(0, streak())
        repository.reviewError("s3", q, 1, 6000)
        repository.reviewError("s4", q, 1, 7000)
        assertEquals(2, streak())
        assertEquals(original, daoRun(id))
        val data = repository.exportBackup()
        repository.restoreBackup(data)
        assertEquals(4, repository.dao.allErrorReviews().size)
        val stats = topicResults(examOutcomes(repository.dao.allExams(), listOf(q)),
            repository.dao.allErrorReviews(), errorItems(examOutcomes(listOf(original), listOf(q)),
                repository.dao.allErrorReviews())).single()
        assertEquals(TopicResult("Química", "Tema", 1, 0, 4, 3, 0), stats)
        val retry = repository.startExam(exam, 8000)
        repository.finish(retry, 9000)
        assertEquals(0, streak())
    }

    private suspend fun daoRun(id: String) = checkNotNull(repository.dao.exam(id))

    @Test fun legacyHistoryMatchesOnlyWhenTheCorrectAnswerAgrees() {
        val old = ExamQuestion("Lenguaje y Comunicación", "Pregunta", listOf("A", "B"), 0, "Explicación")
        val run = ExamRun("old", "Anterior", 1, 100, 2, questionsJson = questionsJson(listOf(old)), answersJson = "[-1]")
        val bank = listOf(old.copy(subject = "Lenguaje", id = "current", topic = "Texto"))
        val match = examOutcomes(listOf(run), bank).single()
        assertEquals("current", match.question.id)
        assertEquals("Texto", match.question.topic)
        val conflict = examOutcomes(listOf(run), bank.map { it.copy(correct = 1) }).single()
        assertTrue(conflict.question.id.startsWith("legacy-"))
        assertEquals("Sin tema identificado", conflict.question.topic)
        assertEquals(0, topicResults(listOf(match), emptyList(), errorItems(listOf(match), emptyList())).single().examAnswers)
    }

    @Test fun migrationFromVersionTwoPreservesSavedExamAndProgress() = runBlocking {
        val name = "migration-v2.db"
        context.deleteDatabase(name)
        val first = StudyDatabase.open(context, name)
        val previous = StudyRepository(first)
        previous.dao.add(listOf(card))
        previous.rate(card.id, 3, 0, 1000)
        previous.dao.addTime(StudyTime("previous", 1000, 5000))
        val q = ExamQuestion("Química", "Pregunta", listOf("A", "B"), 1, "Explicación")
        val id = previous.startExam(PracticeExam("previous", "Anterior", 1, listOf(q)), 2000)
        previous.answer(id, 0, 1, 3000)
        val expected = previous.dao.exam(id)
        first.close()
        // Recreate the exact old table set while keeping the saved data.
        SQLiteDatabase.openDatabase(context.getDatabasePath(name).path, null, SQLiteDatabase.OPEN_READWRITE).use {
            it.execSQL("DROP TABLE error_reviews")
            it.version = 2
        }
        val migrated = StudyDatabase.open(context, name)
        try {
            assertEquals(expected, migrated.dao().exam(id))
            assertEquals(1, migrated.dao().allLogs().size)
            assertEquals(1, migrated.dao().getProgress(card.id)?.reviews)
            assertEquals(5000L, migrated.dao().allTimes().single().millis)
            assertTrue(migrated.dao().allErrorReviews().isEmpty())
            val repo = StudyRepository(migrated)
            repo.expireExams(100000)
            assertEquals(expected!!.deadline, migrated.dao().exam(id)!!.finishedAt)
            assertEquals(listOf(1), parseAnswers(migrated.dao().exam(id)!!.answersJson))
        } finally { migrated.close(); context.deleteDatabase(name) }
    }

    @Test fun streakHandlesYesterdayAndMissingDays() {
        val today = LocalDate.of(2026, 11, 8)
        assertEquals(2, streak(setOf(today.minusDays(1), today.minusDays(2)), today))
        assertEquals(1, streak(setOf(today, today.minusDays(2)), today))
        assertEquals(0, streak(emptySet(), today))
    }
}
