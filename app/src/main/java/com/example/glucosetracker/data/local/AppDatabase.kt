package com.example.glucosetracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.glucosetracker.data.local.entities.GlucoseEntry
import com.example.glucosetracker.data.local.entities.InjectionEntry
import com.example.glucosetracker.data.local.entities.MealEntry

@Database(
    entities = [
        GlucoseEntry::class,
        MealEntry::class,
        InjectionEntry::class
    ],
    version = 2
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

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "glucose_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}