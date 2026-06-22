package com.example.glucosetracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.glucosetracker.data.local.entities.PredictionEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface PredictionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrediction(entry: PredictionEntry): Long

    @Query("SELECT * FROM prediction_entries ORDER BY createdAt DESC LIMIT :limit")
    fun getLatestPredictions(limit: Int): Flow<List<PredictionEntry>>

    @Query("DELETE FROM prediction_entries WHERE createdAt < :createdBefore")
    suspend fun deletePredictionsOlderThan(createdBefore: Long): Int
}