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
public class SubjectDao_Impl(
  __db: RoomDatabase,
) : SubjectDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfSubjectEntity: EntityInsertAdapter<SubjectEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfSubjectEntity = object : EntityInsertAdapter<SubjectEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `subjects` (`id`,`name`,`nameKey`,`color`,`isDefault`,`position`) VALUES (?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: SubjectEntity) {
        statement.bindText(1, entity.id)
        val _tmpName: String? = entity.name
        if (_tmpName == null) {
          statement.bindNull(2)
        } else {
          statement.bindText(2, _tmpName)
        }
        val _tmpNameKey: String? = entity.nameKey
        if (_tmpNameKey == null) {
          statement.bindNull(3)
        } else {
          statement.bindText(3, _tmpNameKey)
        }
        statement.bindText(4, entity.color)
        val _tmp: Int = if (entity.isDefault) 1 else 0
        statement.bindLong(5, _tmp.toLong())
        statement.bindLong(6, entity.position.toLong())
      }
    }
  }

  public override suspend fun insert(subject: SubjectEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfSubjectEntity.insert(_connection, subject)
  }

  public override fun observeAll(): Flow<List<SubjectEntity>> {
    val _sql: String = "SELECT * FROM subjects ORDER BY position, id"
    return createFlow(__db, false, arrayOf("subjects")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfNameKey: Int = getColumnIndexOrThrow(_stmt, "nameKey")
        val _columnIndexOfColor: Int = getColumnIndexOrThrow(_stmt, "color")
        val _columnIndexOfIsDefault: Int = getColumnIndexOrThrow(_stmt, "isDefault")
        val _columnIndexOfPosition: Int = getColumnIndexOrThrow(_stmt, "position")
        val _result: MutableList<SubjectEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SubjectEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpName: String?
          if (_stmt.isNull(_columnIndexOfName)) {
            _tmpName = null
          } else {
            _tmpName = _stmt.getText(_columnIndexOfName)
          }
          val _tmpNameKey: String?
          if (_stmt.isNull(_columnIndexOfNameKey)) {
            _tmpNameKey = null
          } else {
            _tmpNameKey = _stmt.getText(_columnIndexOfNameKey)
          }
          val _tmpColor: String
          _tmpColor = _stmt.getText(_columnIndexOfColor)
          val _tmpIsDefault: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsDefault).toInt()
          _tmpIsDefault = _tmp != 0
          val _tmpPosition: Int
          _tmpPosition = _stmt.getLong(_columnIndexOfPosition).toInt()
          _item = SubjectEntity(_tmpId,_tmpName,_tmpNameKey,_tmpColor,_tmpIsDefault,_tmpPosition)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAll(): List<SubjectEntity> {
    val _sql: String = "SELECT * FROM subjects ORDER BY position, id"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfNameKey: Int = getColumnIndexOrThrow(_stmt, "nameKey")
        val _columnIndexOfColor: Int = getColumnIndexOrThrow(_stmt, "color")
        val _columnIndexOfIsDefault: Int = getColumnIndexOrThrow(_stmt, "isDefault")
        val _columnIndexOfPosition: Int = getColumnIndexOrThrow(_stmt, "position")
        val _result: MutableList<SubjectEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SubjectEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpName: String?
          if (_stmt.isNull(_columnIndexOfName)) {
            _tmpName = null
          } else {
            _tmpName = _stmt.getText(_columnIndexOfName)
          }
          val _tmpNameKey: String?
          if (_stmt.isNull(_columnIndexOfNameKey)) {
            _tmpNameKey = null
          } else {
            _tmpNameKey = _stmt.getText(_columnIndexOfNameKey)
          }
          val _tmpColor: String
          _tmpColor = _stmt.getText(_columnIndexOfColor)
          val _tmpIsDefault: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsDefault).toInt()
          _tmpIsDefault = _tmp != 0
          val _tmpPosition: Int
          _tmpPosition = _stmt.getLong(_columnIndexOfPosition).toInt()
          _item = SubjectEntity(_tmpId,_tmpName,_tmpNameKey,_tmpColor,_tmpIsDefault,_tmpPosition)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getById(id: String): SubjectEntity? {
    val _sql: String = "SELECT * FROM subjects WHERE id = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfNameKey: Int = getColumnIndexOrThrow(_stmt, "nameKey")
        val _columnIndexOfColor: Int = getColumnIndexOrThrow(_stmt, "color")
        val _columnIndexOfIsDefault: Int = getColumnIndexOrThrow(_stmt, "isDefault")
        val _columnIndexOfPosition: Int = getColumnIndexOrThrow(_stmt, "position")
        val _result: SubjectEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpName: String?
          if (_stmt.isNull(_columnIndexOfName)) {
            _tmpName = null
          } else {
            _tmpName = _stmt.getText(_columnIndexOfName)
          }
          val _tmpNameKey: String?
          if (_stmt.isNull(_columnIndexOfNameKey)) {
            _tmpNameKey = null
          } else {
            _tmpNameKey = _stmt.getText(_columnIndexOfNameKey)
          }
          val _tmpColor: String
          _tmpColor = _stmt.getText(_columnIndexOfColor)
          val _tmpIsDefault: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsDefault).toInt()
          _tmpIsDefault = _tmp != 0
          val _tmpPosition: Int
          _tmpPosition = _stmt.getLong(_columnIndexOfPosition).toInt()
          _result = SubjectEntity(_tmpId,_tmpName,_tmpNameKey,_tmpColor,_tmpIsDefault,_tmpPosition)
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
    val _sql: String = "SELECT MAX(position) FROM subjects"
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

  public override suspend fun updateColor(id: String, color: String) {
    val _sql: String = "UPDATE subjects SET color = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, color)
        _argIndex = 2
        _stmt.bindText(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteCustom(id: String) {
    val _sql: String = "DELETE FROM subjects WHERE id = ? AND isDefault = 0"
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

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
