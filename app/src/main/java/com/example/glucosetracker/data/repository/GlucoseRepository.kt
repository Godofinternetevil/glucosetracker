package com.example.glucosetracker.data.repository

import com.example.glucosetracker.data.local.GlucoseDao
import com.example.glucosetracker.data.local.entities.DataSourceConfig
import com.example.glucosetracker.data.local.entities.GlucoseEntry
import com.example.glucosetracker.data.local.entities.InjectionEntry
import com.example.glucosetracker.data.local.entities.InsulinEntry
import com.example.glucosetracker.data.local.entities.MealEntry

class GlucoseRepository(
    private val dao: GlucoseDao
) {

    val glucoseList = dao.getAllGlucose()

    val mealsList = dao.getAllMeals()

    val injectionsList = dao.getAllInjections()

    val insulinList = dao.getAllInsulin()

    val dataSourceConfig = dao.observeDataSourceConfig()

    suspend fun insertGlucose(entry: GlucoseEntry) {
        dao.insertGlucose(entry)
    }

    suspend fun insertGlucoseEntries(entries: List<GlucoseEntry>) {
        dao.insertGlucoseEntries(entries)
    }

    suspend fun insertMeal(entry: MealEntry) {
        dao.insertMeal(entry)
    }

    suspend fun insertInjection(entry: InjectionEntry) {
        dao.insertInjection(entry)
    }

    suspend fun insertInsulin(entry: InsulinEntry) {
        dao.insertInsulin(entry)
    }

    fun getGlucoseBetween(start: Long, end: Long) = dao.getGlucoseBetween(start, end)

    fun getMealsBetween(start: Long, end: Long) = dao.getMealsBetween(start, end)

    fun getInsulinBetween(start: Long, end: Long) = dao.getInsulinBetween(start, end)

    suspend fun getDataSourceConfig(): DataSourceConfig {
        return dao.getDataSourceConfig() ?: DataSourceConfig()
    }

    suspend fun saveDataSourceConfig(config: DataSourceConfig) {
        dao.saveDataSourceConfig(config)
    }
}