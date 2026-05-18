package com.example.glucosetracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.glucosetracker.data.local.entities.GlucoseEntry
import com.example.glucosetracker.data.local.entities.MealEntry

@Database(
    entities = [
        GlucoseEntry::class,
        MealEntry::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun glucoseDao(): GlucoseDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "glucose_database"
                ).build()

                INSTANCE = instance
                instance
            }
        }
    }
}