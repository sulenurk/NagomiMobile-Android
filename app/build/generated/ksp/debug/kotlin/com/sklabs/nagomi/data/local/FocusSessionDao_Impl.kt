package com.sklabs.nagomi.`data`.local

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class FocusSessionDao_Impl(
  __db: RoomDatabase,
) : FocusSessionDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfFocusSessionEntity: EntityInsertAdapter<FocusSessionEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfFocusSessionEntity = object : EntityInsertAdapter<FocusSessionEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `focus_sessions` (`id`,`taskId`,`taskTitle`,`subjectId`,`subjectName`,`mode`,`source`,`durationSeconds`,`awaySeconds`,`startedAtMillis`,`completedAtMillis`,`completed`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: FocusSessionEntity) {
        statement.bindText(1, entity.id)
        val _tmpTaskId: String? = entity.taskId
        if (_tmpTaskId == null) {
          statement.bindNull(2)
        } else {
          statement.bindText(2, _tmpTaskId)
        }
        statement.bindText(3, entity.taskTitle)
        statement.bindText(4, entity.subjectId)
        statement.bindText(5, entity.subjectName)
        statement.bindText(6, entity.mode)
        statement.bindText(7, entity.source)
        statement.bindLong(8, entity.durationSeconds.toLong())
        statement.bindLong(9, entity.awaySeconds.toLong())
        val _tmpStartedAtMillis: Long? = entity.startedAtMillis
        if (_tmpStartedAtMillis == null) {
          statement.bindNull(10)
        } else {
          statement.bindLong(10, _tmpStartedAtMillis)
        }
        statement.bindLong(11, entity.completedAtMillis)
        val _tmp: Int = if (entity.completed) 1 else 0
        statement.bindLong(12, _tmp.toLong())
      }
    }
  }

  public override suspend fun insert(session: FocusSessionEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfFocusSessionEntity.insert(_connection, session)
  }

  public override fun observeCompleted(): Flow<List<FocusSessionEntity>> {
    val _sql: String = "SELECT * FROM focus_sessions WHERE completed = 1 ORDER BY completedAtMillis DESC"
    return createFlow(__db, false, arrayOf("focus_sessions")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTaskId: Int = getColumnIndexOrThrow(_stmt, "taskId")
        val _columnIndexOfTaskTitle: Int = getColumnIndexOrThrow(_stmt, "taskTitle")
        val _columnIndexOfSubjectId: Int = getColumnIndexOrThrow(_stmt, "subjectId")
        val _columnIndexOfSubjectName: Int = getColumnIndexOrThrow(_stmt, "subjectName")
        val _columnIndexOfMode: Int = getColumnIndexOrThrow(_stmt, "mode")
        val _columnIndexOfSource: Int = getColumnIndexOrThrow(_stmt, "source")
        val _columnIndexOfDurationSeconds: Int = getColumnIndexOrThrow(_stmt, "durationSeconds")
        val _columnIndexOfAwaySeconds: Int = getColumnIndexOrThrow(_stmt, "awaySeconds")
        val _columnIndexOfStartedAtMillis: Int = getColumnIndexOrThrow(_stmt, "startedAtMillis")
        val _columnIndexOfCompletedAtMillis: Int = getColumnIndexOrThrow(_stmt, "completedAtMillis")
        val _columnIndexOfCompleted: Int = getColumnIndexOrThrow(_stmt, "completed")
        val _result: MutableList<FocusSessionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: FocusSessionEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpTaskId: String?
          if (_stmt.isNull(_columnIndexOfTaskId)) {
            _tmpTaskId = null
          } else {
            _tmpTaskId = _stmt.getText(_columnIndexOfTaskId)
          }
          val _tmpTaskTitle: String
          _tmpTaskTitle = _stmt.getText(_columnIndexOfTaskTitle)
          val _tmpSubjectId: String
          _tmpSubjectId = _stmt.getText(_columnIndexOfSubjectId)
          val _tmpSubjectName: String
          _tmpSubjectName = _stmt.getText(_columnIndexOfSubjectName)
          val _tmpMode: String
          _tmpMode = _stmt.getText(_columnIndexOfMode)
          val _tmpSource: String
          _tmpSource = _stmt.getText(_columnIndexOfSource)
          val _tmpDurationSeconds: Int
          _tmpDurationSeconds = _stmt.getLong(_columnIndexOfDurationSeconds).toInt()
          val _tmpAwaySeconds: Int
          _tmpAwaySeconds = _stmt.getLong(_columnIndexOfAwaySeconds).toInt()
          val _tmpStartedAtMillis: Long?
          if (_stmt.isNull(_columnIndexOfStartedAtMillis)) {
            _tmpStartedAtMillis = null
          } else {
            _tmpStartedAtMillis = _stmt.getLong(_columnIndexOfStartedAtMillis)
          }
          val _tmpCompletedAtMillis: Long
          _tmpCompletedAtMillis = _stmt.getLong(_columnIndexOfCompletedAtMillis)
          val _tmpCompleted: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfCompleted).toInt()
          _tmpCompleted = _tmp != 0
          _item = FocusSessionEntity(_tmpId,_tmpTaskId,_tmpTaskTitle,_tmpSubjectId,_tmpSubjectName,_tmpMode,_tmpSource,_tmpDurationSeconds,_tmpAwaySeconds,_tmpStartedAtMillis,_tmpCompletedAtMillis,_tmpCompleted)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun reassignSubject(deletedSubjectId: String) {
    val _sql: String = "UPDATE focus_sessions SET subjectId = 'subject_other', subjectName = 'Other' WHERE subjectId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, deletedSubjectId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteStudyPlanSessions(taskId: String) {
    val _sql: String = "DELETE FROM focus_sessions WHERE taskId = ? AND source = 'study_plan'"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, taskId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteAll() {
    val _sql: String = "DELETE FROM focus_sessions"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
