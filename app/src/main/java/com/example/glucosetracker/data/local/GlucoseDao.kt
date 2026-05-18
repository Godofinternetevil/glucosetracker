package com.example.glucosetracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.glucosetracker.data.local.entities.GlucoseEntry
import com.example.glucosetracker.data.local.entities.MealEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface GlucoseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGlucose(entry: GlucoseEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(entry: MealEntry)

    @Query("SELECT * FROM glucose_entries ORDER BY timestamp ASC")
    fun getAllGlucose(): Flow<List<GlucoseEntry>>

    @Query("SELECT * FROM meal_entries ORDER BY timestamp DESC")
    fun getAllMeals(): Flow<List<MealEntry>>
}