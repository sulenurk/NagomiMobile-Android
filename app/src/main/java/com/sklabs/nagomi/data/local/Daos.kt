package com.sklabs.nagomi.data.local

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class TaskWithSubject(
    @Embedded val task: StudyTaskEntity,
    @ColumnInfo(name = "resolved_subject_name") val subjectName: String?,
    @ColumnInfo(name = "resolved_subject_name_key") val subjectNameKey: String?,
    @ColumnInfo(name = "resolved_subject_color") val subjectColor: String?,
    @ColumnInfo(name = "resolved_subject_default") val subjectIsDefault: Boolean?,
) {
    val displaySubjectName: String
        get() = if (subjectIsDefault == true || task.subjectId == "subject_other") {
            "Other"
        } else {
            subjectName ?: "Other"
        }
}

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects ORDER BY position, id")
    fun observeAll(): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects ORDER BY position, id")
    suspend fun getAll(): List<SubjectEntity>

    @Query("SELECT * FROM subjects WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): SubjectEntity?

    @Query("SELECT MAX(position) FROM subjects")
    suspend fun maxPosition(): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subject: SubjectEntity)

    @Query("UPDATE subjects SET color = :color WHERE id = :id")
    suspend fun updateColor(id: String, color: String)

    @Query("DELETE FROM subjects WHERE id = :id AND isDefault = 0")
    suspend fun deleteCustom(id: String)
}

@Dao
interface StudyTaskDao {
    @Query(
        """
        SELECT study_tasks.*,
               subjects.name AS resolved_subject_name,
               subjects.nameKey AS resolved_subject_name_key,
               subjects.color AS resolved_subject_color,
               subjects.isDefault AS resolved_subject_default
        FROM study_tasks
        LEFT JOIN subjects ON subjects.id = study_tasks.subjectId
        ORDER BY study_tasks.position, study_tasks.id
        """,
    )
    fun observeAllWithSubjects(): Flow<List<TaskWithSubject>>

    @Query(
        """
        SELECT study_tasks.*,
               subjects.name AS resolved_subject_name,
               subjects.nameKey AS resolved_subject_name_key,
               subjects.color AS resolved_subject_color,
               subjects.isDefault AS resolved_subject_default
        FROM study_tasks
        LEFT JOIN subjects ON subjects.id = study_tasks.subjectId
        WHERE study_tasks.status = 'active'
        LIMIT 1
        """,
    )
    fun observeActiveWithSubject(): Flow<TaskWithSubject?>

    @Query(
        """
        SELECT study_tasks.*,
               subjects.name AS resolved_subject_name,
               subjects.nameKey AS resolved_subject_name_key,
               subjects.color AS resolved_subject_color,
               subjects.isDefault AS resolved_subject_default
        FROM study_tasks
        LEFT JOIN subjects ON subjects.id = study_tasks.subjectId
        WHERE study_tasks.queuePosition IS NOT NULL
          AND study_tasks.status != 'completed'
        ORDER BY study_tasks.queuePosition, study_tasks.position
        """,
    )
    fun observeQueueWithSubjects(): Flow<List<TaskWithSubject>>

    @Query("SELECT * FROM study_tasks ORDER BY position, id")
    suspend fun getAll(): List<StudyTaskEntity>

    @Query("SELECT * FROM study_tasks WHERE status != 'completed' ORDER BY position, id")
    suspend fun getPendingOrdered(): List<StudyTaskEntity>

    @Query("SELECT * FROM study_tasks WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): StudyTaskEntity?

    @Query("SELECT MAX(position) FROM study_tasks")
    suspend fun maxPosition(): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: StudyTaskEntity)

    @Update
    suspend fun update(task: StudyTaskEntity)

    @Query("DELETE FROM study_tasks WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM study_tasks WHERE status = 'completed'")
    suspend fun deleteCompleted()

    @Query("DELETE FROM study_tasks")
    suspend fun deleteAll()

    @Query("UPDATE study_tasks SET subjectId = 'subject_other' WHERE subjectId = :deletedSubjectId")
    suspend fun reassignSubject(deletedSubjectId: String)

    @Query("UPDATE study_tasks SET position = position + 1 WHERE position > :afterPosition")
    suspend fun shiftPositionsAfter(afterPosition: Int)

    @Query("UPDATE study_tasks SET position = :position WHERE id = :id")
    suspend fun updatePosition(id: String, position: Int)

    @Query("UPDATE study_tasks SET status = 'pending' WHERE status = 'active'")
    suspend fun resetActiveTasks()

    @Query("UPDATE study_tasks SET queuePosition = NULL")
    suspend fun clearQueue()

    @Query("UPDATE study_tasks SET queuePosition = :queuePosition WHERE id = :id")
    suspend fun setQueuePosition(id: String, queuePosition: Int?)

    @Query(
        """
        SELECT * FROM study_tasks
        WHERE queuePosition IS NOT NULL
          AND queuePosition > :currentPosition
          AND status != 'completed'
        ORDER BY queuePosition
        LIMIT 1
        """,
    )
    suspend fun getNextQueued(currentPosition: Int): StudyTaskEntity?
}

@Dao
interface FocusSessionDao {
    @Query("SELECT * FROM focus_sessions WHERE completed = 1 ORDER BY completedAtMillis DESC")
    fun observeCompleted(): Flow<List<FocusSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: FocusSessionEntity)

    @Query("UPDATE focus_sessions SET subjectId = 'subject_other', subjectName = 'Other' WHERE subjectId = :deletedSubjectId")
    suspend fun reassignSubject(deletedSubjectId: String)

    @Query("DELETE FROM focus_sessions WHERE taskId = :taskId AND source = 'study_plan'")
    suspend fun deleteStudyPlanSessions(taskId: String)

    @Query("DELETE FROM focus_sessions")
    suspend fun deleteAll()
}

@Dao
interface FocusTimerStateDao {
    @Query("SELECT * FROM focus_timer_state WHERE id = 1 LIMIT 1")
    suspend fun get(): FocusTimerStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(state: FocusTimerStateEntity)

    @Query("DELETE FROM focus_timer_state")
    suspend fun clear()
}
