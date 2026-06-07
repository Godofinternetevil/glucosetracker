package com.example.glucosetracker.data.repository

import com.example.glucosetracker.data.local.GlucoseDao
import com.example.glucosetracker.data.local.entities.GlucoseEntry
import com.example.glucosetracker.data.local.entities.InjectionEntry
import com.example.glucosetracker.data.local.entities.MealEntry

class GlucoseRepository(
    private val dao: GlucoseDao
) {

    val glucoseList = dao.getAllGlucose()

    val mealsList = dao.getAllMeals()

    val injectionsList = dao.getAllInjections()

    suspend fun insertGlucose(entry: GlucoseEntry) {
        dao.insertGlucose(entry)
    }

    suspend fun insertMeal(entry: MealEntry) {
        dao.insertMeal(entry)
    }

    suspend fun insertInjection(entry: InjectionEntry) {
        dao.insertInjection(entry)
    }
}