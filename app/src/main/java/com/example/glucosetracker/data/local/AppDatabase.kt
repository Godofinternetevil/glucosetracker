package com.example.glucosetracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.glucosetracker.data.local.entities.DataSourceConfig
import com.example.glucosetracker.data.local.entities.GlucoseEntry
import com.example.glucosetracker.data.local.entities.InjectionEntry
import com.example.glucosetracker.data.local.entities.MealEntry

@Database(
    entities = [
        GlucoseEntry::class,
        MealEntry::class,
        InjectionEntry::class,
        DataSourceConfig::class
    ],
    version = 3
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun glucoseDao(): GlucoseDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `injection_entries` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `insulinUnits` REAL NOT NULL,
                        `insulinType` TEXT NOT NULL,
                        `injectionType` TEXT NOT NULL,
                        `notes` TEXT NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `data_source_config` (
                        `id` INTEGER NOT NULL,
                        `sourceType` TEXT NOT NULL,
                        `baseUrl` TEXT NOT NULL,
                        `apiSecret` TEXT NOT NULL,
                        `sourceUnits` TEXT NOT NULL,
                        `autoSyncEnabled` INTEGER NOT NULL,
                        `lastSyncAt` INTEGER,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO `data_source_config`
                        (`id`, `sourceType`, `baseUrl`, `apiSecret`, `sourceUnits`, `autoSyncEnabled`, `lastSyncAt`)
                    VALUES (0, 'Manual', '', '', 'mmol/L', 1, NULL)
                    """.trimIndent()
                )
                db.execSQL("ALTER TABLE `glucose_entries` RENAME TO `glucose_entries_old`")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `glucose_entries` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `glucoseMmolL` REAL NOT NULL,
                        `glucoseMgDl` REAL NOT NULL,
                        `source` TEXT NOT NULL,
                        `sourceId` TEXT,
                        `trendDirection` TEXT,
                        `rawPayload` TEXT,
                        `timestamp` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO `glucose_entries`
                        (`id`, `glucoseMmolL`, `glucoseMgDl`, `source`, `sourceId`, `trendDirection`, `rawPayload`, `timestamp`)
                    SELECT
                        `id`,
                        `glucoseLevel`,
                        `glucoseLevel` * 18.0182,
                        'Manual',
                        NULL,
                        NULL,
                        NULL,
                        `timestamp`
                    FROM `glucose_entries_old`
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE `glucose_entries_old`")
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_glucose_entries_source_sourceId` ON `glucose_entries` (`source`, `sourceId`)"
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "glucose_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}