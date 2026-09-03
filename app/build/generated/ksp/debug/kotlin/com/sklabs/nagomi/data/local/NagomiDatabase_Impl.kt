package com.sklabs.nagomi.`data`.local

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class NagomiDatabase_Impl : NagomiDatabase() {
  private val _subjectDao: Lazy<SubjectDao> = lazy {
    SubjectDao_Impl(this)
  }

  private val _studyTaskDao: Lazy<StudyTaskDao> = lazy {
    StudyTaskDao_Impl(this)
  }

  private val _focusSessionDao: Lazy<FocusSessionDao> = lazy {
    FocusSessionDao_Impl(this)
  }

  private val _focusTimerStateDao: Lazy<FocusTimerStateDao> = lazy {
    FocusTimerStateDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(2, "25af6b608427d2037e7cbe067003ca49", "31a6b37264e1fe2586bc6bb98556118a") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `subjects` (`id` TEXT NOT NULL, `name` TEXT, `nameKey` TEXT, `color` TEXT NOT NULL, `isDefault` INTEGER NOT NULL, `position` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `study_tasks` (`id` TEXT NOT NULL, `subjectId` TEXT NOT NULL, `title` TEXT NOT NULL, `focusDurationMinutes` INTEGER NOT NULL, `breakMinutes` INTEGER NOT NULL, `priority` TEXT NOT NULL, `status` TEXT NOT NULL, `position` INTEGER NOT NULL, `queuePosition` INTEGER, `completedAtMillis` INTEGER, `hiddenFromPlan` INTEGER NOT NULL, `hiddenFromCompleted` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_study_tasks_subjectId` ON `study_tasks` (`subjectId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_study_tasks_status` ON `study_tasks` (`status`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_study_tasks_position` ON `study_tasks` (`position`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `focus_sessions` (`id` TEXT NOT NULL, `taskId` TEXT, `taskTitle` TEXT NOT NULL, `subjectId` TEXT NOT NULL, `subjectName` TEXT NOT NULL, `mode` TEXT NOT NULL, `source` TEXT NOT NULL, `durationSeconds` INTEGER NOT NULL, `awaySeconds` INTEGER NOT NULL, `startedAtMillis` INTEGER, `completedAtMillis` INTEGER NOT NULL, `completed` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_focus_sessions_taskId` ON `focus_sessions` (`taskId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_focus_sessions_subjectId` ON `focus_sessions` (`subjectId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_focus_sessions_completedAtMillis` ON `focus_sessions` (`completedAtMillis`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `focus_timer_state` (`id` INTEGER NOT NULL, `taskId` TEXT NOT NULL, `phase` TEXT NOT NULL, `remainingSeconds` INTEGER NOT NULL, `totalSeconds` INTEGER NOT NULL, `isRunning` INTEGER NOT NULL, `endTimestampMillis` INTEGER, `focusStartedAtMillis` INTEGER, `awaySeconds` INTEGER NOT NULL, `backgroundStartedAtMillis` INTEGER, `updatedAtMillis` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '25af6b608427d2037e7cbe067003ca49')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `subjects`")
        connection.execSQL("DROP TABLE IF EXISTS `study_tasks`")
        connection.execSQL("DROP TABLE IF EXISTS `focus_sessions`")
        connection.execSQL("DROP TABLE IF EXISTS `focus_timer_state`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection): RoomOpenDelegate.ValidationResult {
        val _columnsSubjects: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsSubjects.put("id", TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSubjects.put("name", TableInfo.Column("name", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSubjects.put("nameKey", TableInfo.Column("nameKey", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSubjects.put("color", TableInfo.Column("color", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSubjects.put("isDefault", TableInfo.Column("isDefault", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSubjects.put("position", TableInfo.Column("position", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysSubjects: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesSubjects: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoSubjects: TableInfo = TableInfo("subjects", _columnsSubjects, _foreignKeysSubjects, _indicesSubjects)
        val _existingSubjects: TableInfo = read(connection, "subjects")
        if (!_infoSubjects.equals(_existingSubjects)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |subjects(com.sklabs.nagomi.data.local.SubjectEntity).
              | Expected:
              |""".trimMargin() + _infoSubjects + """
              |
              | Found:
              |""".trimMargin() + _existingSubjects)
        }
        val _columnsStudyTasks: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsStudyTasks.put("id", TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsStudyTasks.put("subjectId", TableInfo.Column("subjectId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsStudyTasks.put("title", TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsStudyTasks.put("focusDurationMinutes", TableInfo.Column("focusDurationMinutes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsStudyTasks.put("breakMinutes", TableInfo.Column("breakMinutes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsStudyTasks.put("priority", TableInfo.Column("priority", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsStudyTasks.put("status", TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsStudyTasks.put("position", TableInfo.Column("position", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsStudyTasks.put("queuePosition", TableInfo.Column("queuePosition", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsStudyTasks.put("completedAtMillis", TableInfo.Column("completedAtMillis", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsStudyTasks.put("hiddenFromPlan", TableInfo.Column("hiddenFromPlan", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsStudyTasks.put("hiddenFromCompleted", TableInfo.Column("hiddenFromCompleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysStudyTasks: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesStudyTasks: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesStudyTasks.add(TableInfo.Index("index_study_tasks_subjectId", false, listOf("subjectId"), listOf("ASC")))
        _indicesStudyTasks.add(TableInfo.Index("index_study_tasks_status", false, listOf("status"), listOf("ASC")))
        _indicesStudyTasks.add(TableInfo.Index("index_study_tasks_position", false, listOf("position"), listOf("ASC")))
        val _infoStudyTasks: TableInfo = TableInfo("study_tasks", _columnsStudyTasks, _foreignKeysStudyTasks, _indicesStudyTasks)
        val _existingStudyTasks: TableInfo = read(connection, "study_tasks")
        if (!_infoStudyTasks.equals(_existingStudyTasks)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |study_tasks(com.sklabs.nagomi.data.local.StudyTaskEntity).
              | Expected:
              |""".trimMargin() + _infoStudyTasks + """
              |
              | Found:
              |""".trimMargin() + _existingStudyTasks)
        }
        val _columnsFocusSessions: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsFocusSessions.put("id", TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFocusSessions.put("taskId", TableInfo.Column("taskId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFocusSessions.put("taskTitle", TableInfo.Column("taskTitle", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFocusSessions.put("subjectId", TableInfo.Column("subjectId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFocusSessions.put("subjectName", TableInfo.Column("subjectName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFocusSessions.put("mode", TableInfo.Column("mode", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFocusSessions.put("source", TableInfo.Column("source", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFocusSessions.put("durationSeconds", TableInfo.Column("durationSeconds", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFocusSessions.put("awaySeconds", TableInfo.Column("awaySeconds", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFocusSessions.put("startedAtMillis", TableInfo.Column("startedAtMillis", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFocusSessions.put("completedAtMillis", TableInfo.Column("completedAtMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFocusSessions.put("completed", TableInfo.Column("completed", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysFocusSessions: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesFocusSessions: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesFocusSessions.add(TableInfo.Index("index_focus_sessions_taskId", false, listOf("taskId"), listOf("ASC")))
        _indicesFocusSessions.add(TableInfo.Index("index_focus_sessions_subjectId", false, listOf("subjectId"), listOf("ASC")))
        _indicesFocusSessions.add(TableInfo.Index("index_focus_sessions_completedAtMillis", false, listOf("completedAtMillis"), listOf("ASC")))
        val _infoFocusSessions: TableInfo = TableInfo("focus_sessions", _columnsFocusSessions, _foreignKeysFocusSessions, _indicesFocusSessions)
        val _existingFocusSessions: TableInfo = read(connection, "focus_sessions")
        if (!_infoFocusSessions.equals(_existingFocusSessions)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |focus_sessions(com.sklabs.nagomi.data.local.FocusSessionEntity).
              | Expected:
              |""".trimMargin() + _infoFocusSessions + """
              |
              | Found:
              |""".trimMargin() + _existingFocusSessions)
        }
        val _columnsFocusTimerState: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsFocusTimerState.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFocusTimerState.put("taskId", TableInfo.Column("taskId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFocusTimerState.put("phase", TableInfo.Column("phase", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFocusTimerState.put("remainingSeconds", TableInfo.Column("remainingSeconds", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFocusTimerState.put("totalSeconds", TableInfo.Column("totalSeconds", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFocusTimerState.put("isRunning", TableInfo.Column("isRunning", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFocusTimerState.put("endTimestampMillis", TableInfo.Column("endTimestampMillis", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFocusTimerState.put("focusStartedAtMillis", TableInfo.Column("focusStartedAtMillis", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFocusTimerState.put("awaySeconds", TableInfo.Column("awaySeconds", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFocusTimerState.put("backgroundStartedAtMillis", TableInfo.Column("backgroundStartedAtMillis", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFocusTimerState.put("updatedAtMillis", TableInfo.Column("updatedAtMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysFocusTimerState: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesFocusTimerState: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoFocusTimerState: TableInfo = TableInfo("focus_timer_state", _columnsFocusTimerState, _foreignKeysFocusTimerState, _indicesFocusTimerState)
        val _existingFocusTimerState: TableInfo = read(connection, "focus_timer_state")
        if (!_infoFocusTimerState.equals(_existingFocusTimerState)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |focus_timer_state(com.sklabs.nagomi.data.local.FocusTimerStateEntity).
              | Expected:
              |""".trimMargin() + _infoFocusTimerState + """
              |
              | Found:
              |""".trimMargin() + _existingFocusTimerState)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "subjects", "study_tasks", "focus_sessions", "focus_timer_state")
  }

  public override fun clearAllTables() {
    super.performClear(false, "subjects", "study_tasks", "focus_sessions", "focus_timer_state")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(SubjectDao::class, SubjectDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(StudyTaskDao::class, StudyTaskDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(FocusSessionDao::class, FocusSessionDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(FocusTimerStateDao::class, FocusTimerStateDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>): List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun subjectDao(): SubjectDao = _subjectDao.value

  public override fun studyTaskDao(): StudyTaskDao = _studyTaskDao.value

  public override fun focusSessionDao(): FocusSessionDao = _focusSessionDao.value

  public override fun focusTimerStateDao(): FocusTimerStateDao = _focusTimerStateDao.value
}
