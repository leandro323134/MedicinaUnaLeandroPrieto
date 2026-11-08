package py.una.medstudy

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "cards", indices = [Index(value = ["subject", "unit", "topic"])])
data class Card(
    @PrimaryKey val id: String,
    val question: String,
    val answer: String,
    val subject: String,
    val unit: String,
    val topic: String,
    val source: String,
    val page: String = "",
    @ColumnInfo(defaultValue = "''") val explanation: String = "",
    @ColumnInfo(defaultValue = "1") val contentDifficulty: Int = 1,
)

@Entity(tableName = "progress", foreignKeys = [ForeignKey(
    entity = Card::class, parentColumns = ["id"], childColumns = ["cardId"], onDelete = ForeignKey.CASCADE
)])
data class Progress(
    @PrimaryKey val cardId: String, val stability: Double = 0.0, val difficulty: Double = 0.0,
    val lastReview: Long = 0L, val due: Long = 0L, val reviews: Int = 0,
)

@Entity(tableName = "review_log", indices = [Index("cardId")])
data class ReviewLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0, val cardId: String, val ratedAt: Long,
    val rating: Int, val stability: Double, val difficulty: Double, val due: Long,
)

@Entity(tableName = "study_time")
data class StudyTime(@PrimaryKey val id: String, val at: Long, val millis: Long)

@Entity(tableName = "exam_runs")
data class ExamRun(
    @PrimaryKey val id: String, val title: String, val startedAt: Long, val deadline: Long,
    val finishedAt: Long = 0L, val position: Int = 0, val questionsJson: String, val answersJson: String,
)

@Dao
interface CardsDao {
    @Query("SELECT * FROM cards ORDER BY subject,unit,topic,id") fun cards(): Flow<List<Card>>
    @Query("SELECT * FROM cards") suspend fun allCards(): List<Card>
    @Query("SELECT * FROM progress") fun progress(): Flow<List<Progress>>
    @Query("SELECT * FROM progress") suspend fun allProgress(): List<Progress>
    @Query("SELECT * FROM review_log ORDER BY ratedAt") fun logs(): Flow<List<ReviewLog>>
    @Query("SELECT * FROM review_log ORDER BY ratedAt") suspend fun allLogs(): List<ReviewLog>
    @Query("SELECT * FROM study_time ORDER BY at") fun times(): Flow<List<StudyTime>>
    @Query("SELECT * FROM study_time") suspend fun allTimes(): List<StudyTime>
    @Query("SELECT * FROM exam_runs ORDER BY startedAt DESC") fun exams(): Flow<List<ExamRun>>
    @Query("SELECT * FROM exam_runs ORDER BY startedAt DESC") suspend fun allExams(): List<ExamRun>
    @Query("SELECT * FROM exam_runs WHERE id=:id") suspend fun exam(id: String): ExamRun?
    @Query("SELECT * FROM progress WHERE cardId=:id") suspend fun getProgress(id: String): Progress?
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun add(cards: List<Card>): List<Long>
    @Update suspend fun updateCards(cards: List<Card>)
    @Upsert suspend fun save(progress: Progress)
    @Insert suspend fun log(review: ReviewLog)
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun addTime(time: StudyTime)
    @Upsert suspend fun saveExam(exam: ExamRun)
}

@Database(
    entities = [Card::class, Progress::class, ReviewLog::class, StudyTime::class, ExamRun::class],
    version = 2, exportSchema = false,
)
abstract class StudyDatabase : RoomDatabase() {
    abstract fun dao(): CardsDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE cards ADD COLUMN explanation TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE cards ADD COLUMN contentDifficulty INTEGER NOT NULL DEFAULT 1")
                db.execSQL("CREATE TABLE IF NOT EXISTS study_time (id TEXT NOT NULL PRIMARY KEY, at INTEGER NOT NULL, millis INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS exam_runs (id TEXT NOT NULL PRIMARY KEY, title TEXT NOT NULL, startedAt INTEGER NOT NULL, deadline INTEGER NOT NULL, finishedAt INTEGER NOT NULL, position INTEGER NOT NULL, questionsJson TEXT NOT NULL, answersJson TEXT NOT NULL)")
            }
        }
        fun open(context: Context, name: String = "study.db"): StudyDatabase =
            Room.databaseBuilder(context, StudyDatabase::class.java, name)
                .addMigrations(MIGRATION_1_2).build()
    }
}
