package com.sklabs.nagomi.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        SubjectEntity::class,
        StudyTaskEntity::class,
        FocusSessionEntity::class,
        FocusTimerStateEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class NagomiDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun studyTaskDao(): StudyTaskDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun focusTimerStateDao(): FocusTimerStateDao

    companion object {
        @Volatile
        private var instance: NagomiDatabase? = null

        fun getInstance(context: Context): NagomiDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                NagomiDatabase::class.java,
                "nagomi.db",
            ).addMigrations(MIGRATION_1_2).build().also { instance = it }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `focus_timer_state` (
                        `id` INTEGER NOT NULL,
                        `taskId` TEXT NOT NULL,
                        `phase` TEXT NOT NULL,
                        `remainingSeconds` INTEGER NOT NULL,
                        `totalSeconds` INTEGER NOT NULL,
                        `isRunning` INTEGER NOT NULL,
                        `endTimestampMillis` INTEGER,
                        `focusStartedAtMillis` INTEGER,
                        `awaySeconds` INTEGER NOT NULL,
                        `backgroundStartedAtMillis` INTEGER,
                        `updatedAtMillis` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent(),
                )
            }
        }
    }
}
