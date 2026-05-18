package com.example.glucosetracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.glucosetracker.data.local.AppDatabase
import com.example.glucosetracker.data.local.entities.GlucoseEntry
import com.example.glucosetracker.data.local.entities.MealEntry
import com.example.glucosetracker.data.repository.GlucoseRepository
import com.example.glucosetracker.data.repository.NightscoutRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    init {
        syncFromNightscout()
    }

    private val dao = AppDatabase
        .getDatabase(application)
        .glucoseDao()

    private val repository = GlucoseRepository(dao)

    private val nightscoutRepository = NightscoutRepository()

    fun syncFromNightscout() {

        viewModelScope.launch {

            try {

                val remoteEntries =
                    nightscoutRepository.fetchGlucoseData()

                remoteEntries.forEach {

                    repository.insertGlucose(it)
                }

            } catch (e: Exception) {

                e.printStackTrace()
            }
        }
    }

    val glucoseList = repository.glucoseList
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val mealsList = repository.mealsList
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addGlucose(level: Float) {
        viewModelScope.launch {
            repository.insertGlucose(
                GlucoseEntry(glucoseLevel = level)
            )
        }
    }

    fun addMeal(name: String, carbs: Int) {
        viewModelScope.launch {
            repository.insertMeal(
                MealEntry(
                    mealName = name,
                    carbs = carbs
                )
            )
        }
    }
}