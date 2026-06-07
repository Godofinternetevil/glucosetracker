package com.example.glucosetracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.glucosetracker.data.local.entities.DataSourceConfig
import com.example.glucosetracker.data.local.entities.GlucoseEntry
import com.example.glucosetracker.data.local.entities.InjectionEntry
import com.example.glucosetracker.data.local.entities.MealEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface GlucoseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGlucose(entry: GlucoseEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGlucoseEntries(entries: List<GlucoseEntry>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(entry: MealEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInjection(entry: InjectionEntry)

    @Query("SELECT * FROM glucose_entries ORDER BY timestamp ASC")
    fun getAllGlucose(): Flow<List<GlucoseEntry>>

    @Query("SELECT * FROM meal_entries ORDER BY timestamp DESC")
    fun getAllMeals(): Flow<List<MealEntry>>

    @Query("SELECT * FROM injection_entries ORDER BY timestamp DESC")
    fun getAllInjections(): Flow<List<InjectionEntry>>

    @Query("SELECT * FROM data_source_config WHERE id = :id LIMIT 1")
    fun observeDataSourceConfig(id: Int = DataSourceConfig.DEFAULT_ID): Flow<DataSourceConfig?>

    @Query("SELECT * FROM data_source_config WHERE id = :id LIMIT 1")
    suspend fun getDataSourceConfig(id: Int = DataSourceConfig.DEFAULT_ID): DataSourceConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDataSourceConfig(config: DataSourceConfig)
}