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
import com.example.glucosetracker.data.local.entities.InsulinEntry
import com.example.glucosetracker.data.local.entities.MealEntry

@Database(
    entities = [
        GlucoseEntry::class,
        MealEntry::class,
        InjectionEntry::class,
        InsulinEntry::class,
        DataSourceConfig::class
    ],
    version = 6
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

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `data_source_config` ADD COLUMN `nightscoutBaseUrl` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `data_source_config` ADD COLUMN `nightscoutToken` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `data_source_config` ADD COLUMN `xDripBaseUrl` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `data_source_config` ADD COLUMN `xDripToken` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `data_source_config` ADD COLUMN `otherApiBaseUrl` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `data_source_config` ADD COLUMN `otherApiToken` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `data_source_config` ADD COLUMN `connectionMode` TEXT NOT NULL DEFAULT 'manual'")
                db.execSQL(
                    """
                    UPDATE `data_source_config`
                    SET
                        `nightscoutBaseUrl` = CASE WHEN `nightscoutBaseUrl` = '' THEN `baseUrl` ELSE `nightscoutBaseUrl` END,
                        `nightscoutToken` = CASE WHEN `nightscoutToken` = '' THEN `apiSecret` ELSE `nightscoutToken` END,
                        `connectionMode` = CASE
                            WHEN `sourceType` = 'Nightscout' THEN 'nightscout'
                            WHEN `sourceType` = 'xDrip bridge' THEN 'xdrip_bridge'
                            WHEN `sourceType` = 'Other API' THEN 'other_api'
                            ELSE 'manual'
                        END
                    WHERE `id` = 0
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `insulin_entries` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `insulinType` TEXT NOT NULL,
                        `units` REAL NOT NULL,
                        `note` TEXT NOT NULL,
                        `source` TEXT NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `meal_entries` RENAME TO `meal_entries_old`")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `meal_entries` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `mealName` TEXT NOT NULL,
                        `carbsGrams` REAL NOT NULL,
                        `proteinGrams` REAL,
                        `fatGrams` REAL,
                        `calories` INTEGER,
                        `mealType` TEXT NOT NULL,
                        `note` TEXT NOT NULL,
                        `source` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO `meal_entries`
                        (`id`, `mealName`, `carbsGrams`, `proteinGrams`, `fatGrams`, `calories`, `mealType`, `note`, `source`, `timestamp`)
                    SELECT
                        `id`,
                        `mealName`,
                        CAST(`carbs` AS REAL),
                        NULL,
                        NULL,
                        NULL,
                        'snack',
                        '',
                        'Manual',
                        `timestamp`
                    FROM `meal_entries_old`
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE `meal_entries_old`")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "glucose_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}