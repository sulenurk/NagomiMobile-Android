package com.sklabs.nagomi.`data`.local

import androidx.room.EntityDeleteOrUpdateAdapter
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
public class StudyTaskDao_Impl(
  __db: RoomDatabase,
) : StudyTaskDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfStudyTaskEntity: EntityInsertAdapter<StudyTaskEntity>

  private val __updateAdapterOfStudyTaskEntity: EntityDeleteOrUpdateAdapter<StudyTaskEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfStudyTaskEntity = object : EntityInsertAdapter<StudyTaskEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `study_tasks` (`id`,`subjectId`,`title`,`focusDurationMinutes`,`breakMinutes`,`priority`,`status`,`position`,`queuePosition`,`completedAtMillis`,`hiddenFromPlan`,`hiddenFromCompleted`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: StudyTaskEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.subjectId)
        statement.bindText(3, entity.title)
        statement.bindLong(4, entity.focusDurationMinutes.toLong())
        statement.bindLong(5, entity.breakMinutes.toLong())
        statement.bindText(6, entity.priority)
        statement.bindText(7, entity.status)
        statement.bindLong(8, entity.position.toLong())
        val _tmpQueuePosition: Int? = entity.queuePosition
        if (_tmpQueuePosition == null) {
          statement.bindNull(9)
        } else {
          statement.bindLong(9, _tmpQueuePosition.toLong())
        }
        val _tmpCompletedAtMillis: Long? = entity.completedAtMillis
        if (_tmpCompletedAtMillis == null) {
          statement.bindNull(10)
        } else {
          statement.bindLong(10, _tmpCompletedAtMillis)
        }
        val _tmp: Int = if (entity.hiddenFromPlan) 1 else 0
        statement.bindLong(11, _tmp.toLong())
        val _tmp_1: Int = if (entity.hiddenFromCompleted) 1 else 0
        statement.bindLong(12, _tmp_1.toLong())
      }
    }
    this.__updateAdapterOfStudyTaskEntity = object : EntityDeleteOrUpdateAdapter<StudyTaskEntity>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `study_tasks` SET `id` = ?,`subjectId` = ?,`title` = ?,`focusDurationMinutes` = ?,`breakMinutes` = ?,`priority` = ?,`status` = ?,`position` = ?,`queuePosition` = ?,`completedAtMillis` = ?,`hiddenFromPlan` = ?,`hiddenFromCompleted` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: StudyTaskEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.subjectId)
        statement.bindText(3, entity.title)
        statement.bindLong(4, entity.focusDurationMinutes.toLong())
        statement.bindLong(5, entity.breakMinutes.toLong())
        statement.bindText(6, entity.priority)
        statement.bindText(7, entity.status)
        statement.bindLong(8, entity.position.toLong())
        val _tmpQueuePosition: Int? = entity.queuePosition
        if (_tmpQueuePosition == null) {
          statement.bindNull(9)
        } else {
          statement.bindLong(9, _tmpQueuePosition.toLong())
        }
        val _tmpCompletedAtMillis: Long? = entity.completedAtMillis
        if (_tmpCompletedAtMillis == null) {
          statement.bindNull(10)
        } else {
          statement.bindLong(10, _tmpCompletedAtMillis)
        }
        val _tmp: Int = if (entity.hiddenFromPlan) 1 else 0
        statement.bindLong(11, _tmp.toLong())
        val _tmp_1: Int = if (entity.hiddenFromCompleted) 1 else 0
        statement.bindLong(12, _tmp_1.toLong())
        statement.bindText(13, entity.id)
      }
    }
  }

  public override suspend fun insert(task: StudyTaskEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfStudyTaskEntity.insert(_connection, task)
  }

  public override suspend fun update(task: StudyTaskEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfStudyTaskEntity.handle(_connection, task)
  }

  public override fun observeAllWithSubjects(): Flow<List<TaskWithSubject>> {
    val _sql: String = """
        |
        |        SELECT study_tasks.*,
        |               subjects.name AS resolved_subject_name,
        |               subjects.nameKey AS resolved_subject_name_key,
        |               subjects.color AS resolved_subject_color,
        |               subjects.isDefault AS resolved_subject_default
        |        FROM study_tasks
        |        LEFT JOIN subjects ON subjects.id = study_tasks.subjectId
        |        ORDER BY study_tasks.position, study_tasks.id
        |        
        """.trimMargin()
    return createFlow(__db, false, arrayOf("study_tasks", "subjects")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSubjectId: Int = getColumnIndexOrThrow(_stmt, "subjectId")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfFocusDurationMinutes: Int = getColumnIndexOrThrow(_stmt, "focusDurationMinutes")
        val _columnIndexOfBreakMinutes: Int = getColumnIndexOrThrow(_stmt, "breakMinutes")
        val _columnIndexOfPriority: Int = getColumnIndexOrThrow(_stmt, "priority")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfPosition: Int = getColumnIndexOrThrow(_stmt, "position")
        val _columnIndexOfQueuePosition: Int = getColumnIndexOrThrow(_stmt, "queuePosition")
        val _columnIndexOfCompletedAtMillis: Int = getColumnIndexOrThrow(_stmt, "completedAtMillis")
        val _columnIndexOfHiddenFromPlan: Int = getColumnIndexOrThrow(_stmt, "hiddenFromPlan")
        val _columnIndexOfHiddenFromCompleted: Int = getColumnIndexOrThrow(_stmt, "hiddenFromCompleted")
        val _columnIndexOfSubjectName: Int = getColumnIndexOrThrow(_stmt, "resolved_subject_name")
        val _columnIndexOfSubjectNameKey: Int = getColumnIndexOrThrow(_stmt, "resolved_subject_name_key")
        val _columnIndexOfSubjectColor: Int = getColumnIndexOrThrow(_stmt, "resolved_subject_color")
        val _columnIndexOfSubjectIsDefault: Int = getColumnIndexOrThrow(_stmt, "resolved_subject_default")
        val _result: MutableList<TaskWithSubject> = mutableListOf()
        while (_stmt.step()) {
          val _item: TaskWithSubject
          val _tmpSubjectName: String?
          if (_stmt.isNull(_columnIndexOfSubjectName)) {
            _tmpSubjectName = null
          } else {
            _tmpSubjectName = _stmt.getText(_columnIndexOfSubjectName)
          }
          val _tmpSubjectNameKey: String?
          if (_stmt.isNull(_columnIndexOfSubjectNameKey)) {
            _tmpSubjectNameKey = null
          } else {
            _tmpSubjectNameKey = _stmt.getText(_columnIndexOfSubjectNameKey)
          }
          val _tmpSubjectColor: String?
          if (_stmt.isNull(_columnIndexOfSubjectColor)) {
            _tmpSubjectColor = null
          } else {
            _tmpSubjectColor = _stmt.getText(_columnIndexOfSubjectColor)
          }
          val _tmpSubjectIsDefault: Boolean?
          val _tmp: Int?
          if (_stmt.isNull(_columnIndexOfSubjectIsDefault)) {
            _tmp = null
          } else {
            _tmp = _stmt.getLong(_columnIndexOfSubjectIsDefault).toInt()
          }
          _tmpSubjectIsDefault = _tmp?.let { it != 0 }
          val _tmpTask: StudyTaskEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpSubjectId: String
          _tmpSubjectId = _stmt.getText(_columnIndexOfSubjectId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpFocusDurationMinutes: Int
          _tmpFocusDurationMinutes = _stmt.getLong(_columnIndexOfFocusDurationMinutes).toInt()
          val _tmpBreakMinutes: Int
          _tmpBreakMinutes = _stmt.getLong(_columnIndexOfBreakMinutes).toInt()
          val _tmpPriority: String
          _tmpPriority = _stmt.getText(_columnIndexOfPriority)
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpPosition: Int
          _tmpPosition = _stmt.getLong(_columnIndexOfPosition).toInt()
          val _tmpQueuePosition: Int?
          if (_stmt.isNull(_columnIndexOfQueuePosition)) {
            _tmpQueuePosition = null
          } else {
            _tmpQueuePosition = _stmt.getLong(_columnIndexOfQueuePosition).toInt()
          }
          val _tmpCompletedAtMillis: Long?
          if (_stmt.isNull(_columnIndexOfCompletedAtMillis)) {
            _tmpCompletedAtMillis = null
          } else {
            _tmpCompletedAtMillis = _stmt.getLong(_columnIndexOfCompletedAtMillis)
          }
          val _tmpHiddenFromPlan: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfHiddenFromPlan).toInt()
          _tmpHiddenFromPlan = _tmp_1 != 0
          val _tmpHiddenFromCompleted: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfHiddenFromCompleted).toInt()
          _tmpHiddenFromCompleted = _tmp_2 != 0
          _tmpTask = StudyTaskEntity(_tmpId,_tmpSubjectId,_tmpTitle,_tmpFocusDurationMinutes,_tmpBreakMinutes,_tmpPriority,_tmpStatus,_tmpPosition,_tmpQueuePosition,_tmpCompletedAtMillis,_tmpHiddenFromPlan,_tmpHiddenFromCompleted)
          _item = TaskWithSubject(_tmpTask,_tmpSubjectName,_tmpSubjectNameKey,_tmpSubjectColor,_tmpSubjectIsDefault)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeActiveWithSubject(): Flow<TaskWithSubject?> {
    val _sql: String = """
        |
        |        SELECT study_tasks.*,
        |               subjects.name AS resolved_subject_name,
        |               subjects.nameKey AS resolved_subject_name_key,
        |               subjects.color AS resolved_subject_color,
        |               subjects.isDefault AS resolved_subject_default
        |        FROM study_tasks
        |        LEFT JOIN subjects ON subjects.id = study_tasks.subjectId
        |        WHERE study_tasks.status = 'active'
        |        LIMIT 1
        |        
        """.trimMargin()
    return createFlow(__db, false, arrayOf("study_tasks", "subjects")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSubjectId: Int = getColumnIndexOrThrow(_stmt, "subjectId")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfFocusDurationMinutes: Int = getColumnIndexOrThrow(_stmt, "focusDurationMinutes")
        val _columnIndexOfBreakMinutes: Int = getColumnIndexOrThrow(_stmt, "breakMinutes")
        val _columnIndexOfPriority: Int = getColumnIndexOrThrow(_stmt, "priority")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfPosition: Int = getColumnIndexOrThrow(_stmt, "position")
        val _columnIndexOfQueuePosition: Int = getColumnIndexOrThrow(_stmt, "queuePosition")
        val _columnIndexOfCompletedAtMillis: Int = getColumnIndexOrThrow(_stmt, "completedAtMillis")
        val _columnIndexOfHiddenFromPlan: Int = getColumnIndexOrThrow(_stmt, "hiddenFromPlan")
        val _columnIndexOfHiddenFromCompleted: Int = getColumnIndexOrThrow(_stmt, "hiddenFromCompleted")
        val _columnIndexOfSubjectName: Int = getColumnIndexOrThrow(_stmt, "resolved_subject_name")
        val _columnIndexOfSubjectNameKey: Int = getColumnIndexOrThrow(_stmt, "resolved_subject_name_key")
        val _columnIndexOfSubjectColor: Int = getColumnIndexOrThrow(_stmt, "resolved_subject_color")
        val _columnIndexOfSubjectIsDefault: Int = getColumnIndexOrThrow(_stmt, "resolved_subject_default")
        val _result: TaskWithSubject?
        if (_stmt.step()) {
          val _tmpSubjectName: String?
          if (_stmt.isNull(_columnIndexOfSubjectName)) {
            _tmpSubjectName = null
          } else {
            _tmpSubjectName = _stmt.getText(_columnIndexOfSubjectName)
          }
          val _tmpSubjectNameKey: String?
          if (_stmt.isNull(_columnIndexOfSubjectNameKey)) {
            _tmpSubjectNameKey = null
          } else {
            _tmpSubjectNameKey = _stmt.getText(_columnIndexOfSubjectNameKey)
          }
          val _tmpSubjectColor: String?
          if (_stmt.isNull(_columnIndexOfSubjectColor)) {
            _tmpSubjectColor = null
          } else {
            _tmpSubjectColor = _stmt.getText(_columnIndexOfSubjectColor)
          }
          val _tmpSubjectIsDefault: Boolean?
          val _tmp: Int?
          if (_stmt.isNull(_columnIndexOfSubjectIsDefault)) {
            _tmp = null
          } else {
            _tmp = _stmt.getLong(_columnIndexOfSubjectIsDefault).toInt()
          }
          _tmpSubjectIsDefault = _tmp?.let { it != 0 }
          val _tmpTask: StudyTaskEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpSubjectId: String
          _tmpSubjectId = _stmt.getText(_columnIndexOfSubjectId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpFocusDurationMinutes: Int
          _tmpFocusDurationMinutes = _stmt.getLong(_columnIndexOfFocusDurationMinutes).toInt()
          val _tmpBreakMinutes: Int
          _tmpBreakMinutes = _stmt.getLong(_columnIndexOfBreakMinutes).toInt()
          val _tmpPriority: String
          _tmpPriority = _stmt.getText(_columnIndexOfPriority)
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpPosition: Int
          _tmpPosition = _stmt.getLong(_columnIndexOfPosition).toInt()
          val _tmpQueuePosition: Int?
          if (_stmt.isNull(_columnIndexOfQueuePosition)) {
            _tmpQueuePosition = null
          } else {
            _tmpQueuePosition = _stmt.getLong(_columnIndexOfQueuePosition).toInt()
          }
          val _tmpCompletedAtMillis: Long?
          if (_stmt.isNull(_columnIndexOfCompletedAtMillis)) {
            _tmpCompletedAtMillis = null
          } else {
            _tmpCompletedAtMillis = _stmt.getLong(_columnIndexOfCompletedAtMillis)
          }
          val _tmpHiddenFromPlan: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfHiddenFromPlan).toInt()
          _tmpHiddenFromPlan = _tmp_1 != 0
          val _tmpHiddenFromCompleted: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfHiddenFromCompleted).toInt()
          _tmpHiddenFromCompleted = _tmp_2 != 0
          _tmpTask = StudyTaskEntity(_tmpId,_tmpSubjectId,_tmpTitle,_tmpFocusDurationMinutes,_tmpBreakMinutes,_tmpPriority,_tmpStatus,_tmpPosition,_tmpQueuePosition,_tmpCompletedAtMillis,_tmpHiddenFromPlan,_tmpHiddenFromCompleted)
          _result = TaskWithSubject(_tmpTask,_tmpSubjectName,_tmpSubjectNameKey,_tmpSubjectColor,_tmpSubjectIsDefault)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeQueueWithSubjects(): Flow<List<TaskWithSubject>> {
    val _sql: String = """
        |
        |        SELECT study_tasks.*,
        |               subjects.name AS resolved_subject_name,
        |               subjects.nameKey AS resolved_subject_name_key,
        |               subjects.color AS resolved_subject_color,
        |               subjects.isDefault AS resolved_subject_default
        |        FROM study_tasks
        |        LEFT JOIN subjects ON subjects.id = study_tasks.subjectId
        |        WHERE study_tasks.queuePosition IS NOT NULL
        |          AND study_tasks.status != 'completed'
        |        ORDER BY study_tasks.queuePosition, study_tasks.position
        |        
        """.trimMargin()
    return createFlow(__db, false, arrayOf("study_tasks", "subjects")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSubjectId: Int = getColumnIndexOrThrow(_stmt, "subjectId")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfFocusDurationMinutes: Int = getColumnIndexOrThrow(_stmt, "focusDurationMinutes")
        val _columnIndexOfBreakMinutes: Int = getColumnIndexOrThrow(_stmt, "breakMinutes")
        val _columnIndexOfPriority: Int = getColumnIndexOrThrow(_stmt, "priority")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfPosition: Int = getColumnIndexOrThrow(_stmt, "position")
        val _columnIndexOfQueuePosition: Int = getColumnIndexOrThrow(_stmt, "queuePosition")
        val _columnIndexOfCompletedAtMillis: Int = getColumnIndexOrThrow(_stmt, "completedAtMillis")
        val _columnIndexOfHiddenFromPlan: Int = getColumnIndexOrThrow(_stmt, "hiddenFromPlan")
        val _columnIndexOfHiddenFromCompleted: Int = getColumnIndexOrThrow(_stmt, "hiddenFromCompleted")
        val _columnIndexOfSubjectName: Int = getColumnIndexOrThrow(_stmt, "resolved_subject_name")
        val _columnIndexOfSubjectNameKey: Int = getColumnIndexOrThrow(_stmt, "resolved_subject_name_key")
        val _columnIndexOfSubjectColor: Int = getColumnIndexOrThrow(_stmt, "resolved_subject_color")
        val _columnIndexOfSubjectIsDefault: Int = getColumnIndexOrThrow(_stmt, "resolved_subject_default")
        val _result: MutableList<TaskWithSubject> = mutableListOf()
        while (_stmt.step()) {
          val _item: TaskWithSubject
          val _tmpSubjectName: String?
          if (_stmt.isNull(_columnIndexOfSubjectName)) {
            _tmpSubjectName = null
          } else {
            _tmpSubjectName = _stmt.getText(_columnIndexOfSubjectName)
          }
          val _tmpSubjectNameKey: String?
          if (_stmt.isNull(_columnIndexOfSubjectNameKey)) {
            _tmpSubjectNameKey = null
          } else {
            _tmpSubjectNameKey = _stmt.getText(_columnIndexOfSubjectNameKey)
          }
          val _tmpSubjectColor: String?
          if (_stmt.isNull(_columnIndexOfSubjectColor)) {
            _tmpSubjectColor = null
          } else {
            _tmpSubjectColor = _stmt.getText(_columnIndexOfSubjectColor)
          }
          val _tmpSubjectIsDefault: Boolean?
          val _tmp: Int?
          if (_stmt.isNull(_columnIndexOfSubjectIsDefault)) {
            _tmp = null
          } else {
            _tmp = _stmt.getLong(_columnIndexOfSubjectIsDefault).toInt()
          }
          _tmpSubjectIsDefault = _tmp?.let { it != 0 }
          val _tmpTask: StudyTaskEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpSubjectId: String
          _tmpSubjectId = _stmt.getText(_columnIndexOfSubjectId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpFocusDurationMinutes: Int
          _tmpFocusDurationMinutes = _stmt.getLong(_columnIndexOfFocusDurationMinutes).toInt()
          val _tmpBreakMinutes: Int
          _tmpBreakMinutes = _stmt.getLong(_columnIndexOfBreakMinutes).toInt()
          val _tmpPriority: String
          _tmpPriority = _stmt.getText(_columnIndexOfPriority)
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpPosition: Int
          _tmpPosition = _stmt.getLong(_columnIndexOfPosition).toInt()
          val _tmpQueuePosition: Int?
          if (_stmt.isNull(_columnIndexOfQueuePosition)) {
            _tmpQueuePosition = null
          } else {
            _tmpQueuePosition = _stmt.getLong(_columnIndexOfQueuePosition).toInt()
          }
          val _tmpCompletedAtMillis: Long?
          if (_stmt.isNull(_columnIndexOfCompletedAtMillis)) {
            _tmpCompletedAtMillis = null
          } else {
            _tmpCompletedAtMillis = _stmt.getLong(_columnIndexOfCompletedAtMillis)
          }
          val _tmpHiddenFromPlan: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfHiddenFromPlan).toInt()
          _tmpHiddenFromPlan = _tmp_1 != 0
          val _tmpHiddenFromCompleted: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfHiddenFromCompleted).toInt()
          _tmpHiddenFromCompleted = _tmp_2 != 0
          _tmpTask = StudyTaskEntity(_tmpId,_tmpSubjectId,_tmpTitle,_tmpFocusDurationMinutes,_tmpBreakMinutes,_tmpPriority,_tmpStatus,_tmpPosition,_tmpQueuePosition,_tmpCompletedAtMillis,_tmpHiddenFromPlan,_tmpHiddenFromCompleted)
          _item = TaskWithSubject(_tmpTask,_tmpSubjectName,_tmpSubjectNameKey,_tmpSubjectColor,_tmpSubjectIsDefault)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAll(): List<StudyTaskEntity> {
    val _sql: String = "SELECT * FROM study_tasks ORDER BY position, id"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSubjectId: Int = getColumnIndexOrThrow(_stmt, "subjectId")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfFocusDurationMinutes: Int = getColumnIndexOrThrow(_stmt, "focusDurationMinutes")
        val _columnIndexOfBreakMinutes: Int = getColumnIndexOrThrow(_stmt, "breakMinutes")
        val _columnIndexOfPriority: Int = getColumnIndexOrThrow(_stmt, "priority")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfPosition: Int = getColumnIndexOrThrow(_stmt, "position")
        val _columnIndexOfQueuePosition: Int = getColumnIndexOrThrow(_stmt, "queuePosition")
        val _columnIndexOfCompletedAtMillis: Int = getColumnIndexOrThrow(_stmt, "completedAtMillis")
        val _columnIndexOfHiddenFromPlan: Int = getColumnIndexOrThrow(_stmt, "hiddenFromPlan")
        val _columnIndexOfHiddenFromCompleted: Int = getColumnIndexOrThrow(_stmt, "hiddenFromCompleted")
        val _result: MutableList<StudyTaskEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: StudyTaskEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpSubjectId: String
          _tmpSubjectId = _stmt.getText(_columnIndexOfSubjectId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpFocusDurationMinutes: Int
          _tmpFocusDurationMinutes = _stmt.getLong(_columnIndexOfFocusDurationMinutes).toInt()
          val _tmpBreakMinutes: Int
          _tmpBreakMinutes = _stmt.getLong(_columnIndexOfBreakMinutes).toInt()
          val _tmpPriority: String
          _tmpPriority = _stmt.getText(_columnIndexOfPriority)
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpPosition: Int
          _tmpPosition = _stmt.getLong(_columnIndexOfPosition).toInt()
          val _tmpQueuePosition: Int?
          if (_stmt.isNull(_columnIndexOfQueuePosition)) {
            _tmpQueuePosition = null
          } else {
            _tmpQueuePosition = _stmt.getLong(_columnIndexOfQueuePosition).toInt()
          }
          val _tmpCompletedAtMillis: Long?
          if (_stmt.isNull(_columnIndexOfCompletedAtMillis)) {
            _tmpCompletedAtMillis = null
          } else {
            _tmpCompletedAtMillis = _stmt.getLong(_columnIndexOfCompletedAtMillis)
          }
          val _tmpHiddenFromPlan: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfHiddenFromPlan).toInt()
          _tmpHiddenFromPlan = _tmp != 0
          val _tmpHiddenFromCompleted: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfHiddenFromCompleted).toInt()
          _tmpHiddenFromCompleted = _tmp_1 != 0
          _item = StudyTaskEntity(_tmpId,_tmpSubjectId,_tmpTitle,_tmpFocusDurationMinutes,_tmpBreakMinutes,_tmpPriority,_tmpStatus,_tmpPosition,_tmpQueuePosition,_tmpCompletedAtMillis,_tmpHiddenFromPlan,_tmpHiddenFromCompleted)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getPendingOrdered(): List<StudyTaskEntity> {
    val _sql: String = "SELECT * FROM study_tasks WHERE status != 'completed' ORDER BY position, id"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSubjectId: Int = getColumnIndexOrThrow(_stmt, "subjectId")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfFocusDurationMinutes: Int = getColumnIndexOrThrow(_stmt, "focusDurationMinutes")
        val _columnIndexOfBreakMinutes: Int = getColumnIndexOrThrow(_stmt, "breakMinutes")
        val _columnIndexOfPriority: Int = getColumnIndexOrThrow(_stmt, "priority")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfPosition: Int = getColumnIndexOrThrow(_stmt, "position")
        val _columnIndexOfQueuePosition: Int = getColumnIndexOrThrow(_stmt, "queuePosition")
        val _columnIndexOfCompletedAtMillis: Int = getColumnIndexOrThrow(_stmt, "completedAtMillis")
        val _columnIndexOfHiddenFromPlan: Int = getColumnIndexOrThrow(_stmt, "hiddenFromPlan")
        val _columnIndexOfHiddenFromCompleted: Int = getColumnIndexOrThrow(_stmt, "hiddenFromCompleted")
        val _result: MutableList<StudyTaskEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: StudyTaskEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpSubjectId: String
          _tmpSubjectId = _stmt.getText(_columnIndexOfSubjectId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpFocusDurationMinutes: Int
          _tmpFocusDurationMinutes = _stmt.getLong(_columnIndexOfFocusDurationMinutes).toInt()
          val _tmpBreakMinutes: Int
          _tmpBreakMinutes = _stmt.getLong(_columnIndexOfBreakMinutes).toInt()
          val _tmpPriority: String
          _tmpPriority = _stmt.getText(_columnIndexOfPriority)
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpPosition: Int
          _tmpPosition = _stmt.getLong(_columnIndexOfPosition).toInt()
          val _tmpQueuePosition: Int?
          if (_stmt.isNull(_columnIndexOfQueuePosition)) {
            _tmpQueuePosition = null
          } else {
            _tmpQueuePosition = _stmt.getLong(_columnIndexOfQueuePosition).toInt()
          }
          val _tmpCompletedAtMillis: Long?
          if (_stmt.isNull(_columnIndexOfCompletedAtMillis)) {
            _tmpCompletedAtMillis = null
          } else {
            _tmpCompletedAtMillis = _stmt.getLong(_columnIndexOfCompletedAtMillis)
          }
          val _tmpHiddenFromPlan: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfHiddenFromPlan).toInt()
          _tmpHiddenFromPlan = _tmp != 0
          val _tmpHiddenFromCompleted: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfHiddenFromCompleted).toInt()
          _tmpHiddenFromCompleted = _tmp_1 != 0
          _item = StudyTaskEntity(_tmpId,_tmpSubjectId,_tmpTitle,_tmpFocusDurationMinutes,_tmpBreakMinutes,_tmpPriority,_tmpStatus,_tmpPosition,_tmpQueuePosition,_tmpCompletedAtMillis,_tmpHiddenFromPlan,_tmpHiddenFromCompleted)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getById(id: String): StudyTaskEntity? {
    val _sql: String = "SELECT * FROM study_tasks WHERE id = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSubjectId: Int = getColumnIndexOrThrow(_stmt, "subjectId")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfFocusDurationMinutes: Int = getColumnIndexOrThrow(_stmt, "focusDurationMinutes")
        val _columnIndexOfBreakMinutes: Int = getColumnIndexOrThrow(_stmt, "breakMinutes")
        val _columnIndexOfPriority: Int = getColumnIndexOrThrow(_stmt, "priority")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfPosition: Int = getColumnIndexOrThrow(_stmt, "position")
        val _columnIndexOfQueuePosition: Int = getColumnIndexOrThrow(_stmt, "queuePosition")
        val _columnIndexOfCompletedAtMillis: Int = getColumnIndexOrThrow(_stmt, "completedAtMillis")
        val _columnIndexOfHiddenFromPlan: Int = getColumnIndexOrThrow(_stmt, "hiddenFromPlan")
        val _columnIndexOfHiddenFromCompleted: Int = getColumnIndexOrThrow(_stmt, "hiddenFromCompleted")
        val _result: StudyTaskEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpSubjectId: String
          _tmpSubjectId = _stmt.getText(_columnIndexOfSubjectId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpFocusDurationMinutes: Int
          _tmpFocusDurationMinutes = _stmt.getLong(_columnIndexOfFocusDurationMinutes).toInt()
          val _tmpBreakMinutes: Int
          _tmpBreakMinutes = _stmt.getLong(_columnIndexOfBreakMinutes).toInt()
          val _tmpPriority: String
          _tmpPriority = _stmt.getText(_columnIndexOfPriority)
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpPosition: Int
          _tmpPosition = _stmt.getLong(_columnIndexOfPosition).toInt()
          val _tmpQueuePosition: Int?
          if (_stmt.isNull(_columnIndexOfQueuePosition)) {
            _tmpQueuePosition = null
          } else {
            _tmpQueuePosition = _stmt.getLong(_columnIndexOfQueuePosition).toInt()
          }
          val _tmpCompletedAtMillis: Long?
          if (_stmt.isNull(_columnIndexOfCompletedAtMillis)) {
            _tmpCompletedAtMillis = null
          } else {
            _tmpCompletedAtMillis = _stmt.getLong(_columnIndexOfCompletedAtMillis)
          }
          val _tmpHiddenFromPlan: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfHiddenFromPlan).toInt()
          _tmpHiddenFromPlan = _tmp != 0
          val _tmpHiddenFromCompleted: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfHiddenFromCompleted).toInt()
          _tmpHiddenFromCompleted = _tmp_1 != 0
          _result = StudyTaskEntity(_tmpId,_tmpSubjectId,_tmpTitle,_tmpFocusDurationMinutes,_tmpBreakMinutes,_tmpPriority,_tmpStatus,_tmpPosition,_tmpQueuePosition,_tmpCompletedAtMillis,_tmpHiddenFromPlan,_tmpHiddenFromCompleted)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun maxPosition(): Int? {
    val _sql: String = "SELECT MAX(position) FROM study_tasks"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: Int?
        if (_stmt.step()) {
          val _tmp: Int?
          if (_stmt.isNull(0)) {
            _tmp = null
          } else {
            _tmp = _stmt.getLong(0).toInt()
          }
          _result = _tmp
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getNextQueued(currentPosition: Int): StudyTaskEntity? {
    val _sql: String = """
        |
        |        SELECT * FROM study_tasks
        |        WHERE queuePosition IS NOT NULL
        |          AND queuePosition > ?
        |          AND status != 'completed'
        |        ORDER BY queuePosition
        |        LIMIT 1
        |        
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, currentPosition.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSubjectId: Int = getColumnIndexOrThrow(_stmt, "subjectId")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfFocusDurationMinutes: Int = getColumnIndexOrThrow(_stmt, "focusDurationMinutes")
        val _columnIndexOfBreakMinutes: Int = getColumnIndexOrThrow(_stmt, "breakMinutes")
        val _columnIndexOfPriority: Int = getColumnIndexOrThrow(_stmt, "priority")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfPosition: Int = getColumnIndexOrThrow(_stmt, "position")
        val _columnIndexOfQueuePosition: Int = getColumnIndexOrThrow(_stmt, "queuePosition")
        val _columnIndexOfCompletedAtMillis: Int = getColumnIndexOrThrow(_stmt, "completedAtMillis")
        val _columnIndexOfHiddenFromPlan: Int = getColumnIndexOrThrow(_stmt, "hiddenFromPlan")
        val _columnIndexOfHiddenFromCompleted: Int = getColumnIndexOrThrow(_stmt, "hiddenFromCompleted")
        val _result: StudyTaskEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpSubjectId: String
          _tmpSubjectId = _stmt.getText(_columnIndexOfSubjectId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpFocusDurationMinutes: Int
          _tmpFocusDurationMinutes = _stmt.getLong(_columnIndexOfFocusDurationMinutes).toInt()
          val _tmpBreakMinutes: Int
          _tmpBreakMinutes = _stmt.getLong(_columnIndexOfBreakMinutes).toInt()
          val _tmpPriority: String
          _tmpPriority = _stmt.getText(_columnIndexOfPriority)
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpPosition: Int
          _tmpPosition = _stmt.getLong(_columnIndexOfPosition).toInt()
          val _tmpQueuePosition: Int?
          if (_stmt.isNull(_columnIndexOfQueuePosition)) {
            _tmpQueuePosition = null
          } else {
            _tmpQueuePosition = _stmt.getLong(_columnIndexOfQueuePosition).toInt()
          }
          val _tmpCompletedAtMillis: Long?
          if (_stmt.isNull(_columnIndexOfCompletedAtMillis)) {
            _tmpCompletedAtMillis = null
          } else {
            _tmpCompletedAtMillis = _stmt.getLong(_columnIndexOfCompletedAtMillis)
          }
          val _tmpHiddenFromPlan: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfHiddenFromPlan).toInt()
          _tmpHiddenFromPlan = _tmp != 0
          val _tmpHiddenFromCompleted: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfHiddenFromCompleted).toInt()
          _tmpHiddenFromCompleted = _tmp_1 != 0
          _result = StudyTaskEntity(_tmpId,_tmpSubjectId,_tmpTitle,_tmpFocusDurationMinutes,_tmpBreakMinutes,_tmpPriority,_tmpStatus,_tmpPosition,_tmpQueuePosition,_tmpCompletedAtMillis,_tmpHiddenFromPlan,_tmpHiddenFromCompleted)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteById(id: String) {
    val _sql: String = "DELETE FROM study_tasks WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteCompleted() {
    val _sql: String = "DELETE FROM study_tasks WHERE status = 'completed'"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteAll() {
    val _sql: String = "DELETE FROM study_tasks"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun reassignSubject(deletedSubjectId: String) {
    val _sql: String = "UPDATE study_tasks SET subjectId = 'subject_other' WHERE subjectId = ?"
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

  public override suspend fun shiftPositionsAfter(afterPosition: Int) {
    val _sql: String = "UPDATE study_tasks SET position = position + 1 WHERE position > ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, afterPosition.toLong())
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updatePosition(id: String, position: Int) {
    val _sql: String = "UPDATE study_tasks SET position = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, position.toLong())
        _argIndex = 2
        _stmt.bindText(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun resetActiveTasks() {
    val _sql: String = "UPDATE study_tasks SET status = 'pending' WHERE status = 'active'"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clearQueue() {
    val _sql: String = "UPDATE study_tasks SET queuePosition = NULL"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun setQueuePosition(id: String, queuePosition: Int?) {
    val _sql: String = "UPDATE study_tasks SET queuePosition = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        if (queuePosition == null) {
          _stmt.bindNull(_argIndex)
        } else {
          _stmt.bindLong(_argIndex, queuePosition.toLong())
        }
        _argIndex = 2
        _stmt.bindText(_argIndex, id)
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
