package com.sklabs.nagomi.`data`.local

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
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
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class FocusTimerStateDao_Impl(
  __db: RoomDatabase,
) : FocusTimerStateDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfFocusTimerStateEntity: EntityInsertAdapter<FocusTimerStateEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfFocusTimerStateEntity = object : EntityInsertAdapter<FocusTimerStateEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `focus_timer_state` (`id`,`taskId`,`phase`,`remainingSeconds`,`totalSeconds`,`isRunning`,`endTimestampMillis`,`focusStartedAtMillis`,`awaySeconds`,`backgroundStartedAtMillis`,`updatedAtMillis`) VALUES (?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: FocusTimerStateEntity) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.taskId)
        statement.bindText(3, entity.phase)
        statement.bindLong(4, entity.remainingSeconds.toLong())
        statement.bindLong(5, entity.totalSeconds.toLong())
        val _tmp: Int = if (entity.isRunning) 1 else 0
        statement.bindLong(6, _tmp.toLong())
        val _tmpEndTimestampMillis: Long? = entity.endTimestampMillis
        if (_tmpEndTimestampMillis == null) {
          statement.bindNull(7)
        } else {
          statement.bindLong(7, _tmpEndTimestampMillis)
        }
        val _tmpFocusStartedAtMillis: Long? = entity.focusStartedAtMillis
        if (_tmpFocusStartedAtMillis == null) {
          statement.bindNull(8)
        } else {
          statement.bindLong(8, _tmpFocusStartedAtMillis)
        }
        statement.bindLong(9, entity.awaySeconds.toLong())
        val _tmpBackgroundStartedAtMillis: Long? = entity.backgroundStartedAtMillis
        if (_tmpBackgroundStartedAtMillis == null) {
          statement.bindNull(10)
        } else {
          statement.bindLong(10, _tmpBackgroundStartedAtMillis)
        }
        statement.bindLong(11, entity.updatedAtMillis)
      }
    }
  }

  public override suspend fun save(state: FocusTimerStateEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfFocusTimerStateEntity.insert(_connection, state)
  }

  public override suspend fun `get`(): FocusTimerStateEntity? {
    val _sql: String = "SELECT * FROM focus_timer_state WHERE id = 1 LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTaskId: Int = getColumnIndexOrThrow(_stmt, "taskId")
        val _columnIndexOfPhase: Int = getColumnIndexOrThrow(_stmt, "phase")
        val _columnIndexOfRemainingSeconds: Int = getColumnIndexOrThrow(_stmt, "remainingSeconds")
        val _columnIndexOfTotalSeconds: Int = getColumnIndexOrThrow(_stmt, "totalSeconds")
        val _columnIndexOfIsRunning: Int = getColumnIndexOrThrow(_stmt, "isRunning")
        val _columnIndexOfEndTimestampMillis: Int = getColumnIndexOrThrow(_stmt, "endTimestampMillis")
        val _columnIndexOfFocusStartedAtMillis: Int = getColumnIndexOrThrow(_stmt, "focusStartedAtMillis")
        val _columnIndexOfAwaySeconds: Int = getColumnIndexOrThrow(_stmt, "awaySeconds")
        val _columnIndexOfBackgroundStartedAtMillis: Int = getColumnIndexOrThrow(_stmt, "backgroundStartedAtMillis")
        val _columnIndexOfUpdatedAtMillis: Int = getColumnIndexOrThrow(_stmt, "updatedAtMillis")
        val _result: FocusTimerStateEntity?
        if (_stmt.step()) {
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpTaskId: String
          _tmpTaskId = _stmt.getText(_columnIndexOfTaskId)
          val _tmpPhase: String
          _tmpPhase = _stmt.getText(_columnIndexOfPhase)
          val _tmpRemainingSeconds: Int
          _tmpRemainingSeconds = _stmt.getLong(_columnIndexOfRemainingSeconds).toInt()
          val _tmpTotalSeconds: Int
          _tmpTotalSeconds = _stmt.getLong(_columnIndexOfTotalSeconds).toInt()
          val _tmpIsRunning: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsRunning).toInt()
          _tmpIsRunning = _tmp != 0
          val _tmpEndTimestampMillis: Long?
          if (_stmt.isNull(_columnIndexOfEndTimestampMillis)) {
            _tmpEndTimestampMillis = null
          } else {
            _tmpEndTimestampMillis = _stmt.getLong(_columnIndexOfEndTimestampMillis)
          }
          val _tmpFocusStartedAtMillis: Long?
          if (_stmt.isNull(_columnIndexOfFocusStartedAtMillis)) {
            _tmpFocusStartedAtMillis = null
          } else {
            _tmpFocusStartedAtMillis = _stmt.getLong(_columnIndexOfFocusStartedAtMillis)
          }
          val _tmpAwaySeconds: Int
          _tmpAwaySeconds = _stmt.getLong(_columnIndexOfAwaySeconds).toInt()
          val _tmpBackgroundStartedAtMillis: Long?
          if (_stmt.isNull(_columnIndexOfBackgroundStartedAtMillis)) {
            _tmpBackgroundStartedAtMillis = null
          } else {
            _tmpBackgroundStartedAtMillis = _stmt.getLong(_columnIndexOfBackgroundStartedAtMillis)
          }
          val _tmpUpdatedAtMillis: Long
          _tmpUpdatedAtMillis = _stmt.getLong(_columnIndexOfUpdatedAtMillis)
          _result = FocusTimerStateEntity(_tmpId,_tmpTaskId,_tmpPhase,_tmpRemainingSeconds,_tmpTotalSeconds,_tmpIsRunning,_tmpEndTimestampMillis,_tmpFocusStartedAtMillis,_tmpAwaySeconds,_tmpBackgroundStartedAtMillis,_tmpUpdatedAtMillis)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clear() {
    val _sql: String = "DELETE FROM focus_timer_state"
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
