package com.example.glucosetracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.glucosetracker.data.local.AppDatabase
import com.example.glucosetracker.data.local.entities.DataSourceConfig
import com.example.glucosetracker.data.local.entities.GlucoseEntry
import com.example.glucosetracker.data.local.entities.InjectionEntry
import com.example.glucosetracker.data.local.entities.InsulinEntry
import com.example.glucosetracker.data.local.entities.MealEntry
import com.example.glucosetracker.data.repository.GlucoseRepository
import com.example.glucosetracker.sync.GlucoseSyncCoordinator
import com.example.glucosetracker.sync.GlucoseSyncResult
import com.example.glucosetracker.sync.GlucoseSyncWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SyncStatus {
    Idle,
    Loading,
    Success,
    Error
}

data class SyncState(
    val status: SyncStatus = SyncStatus.Idle,
    val lastSyncAt: Long? = null,
    val errorText: String? = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase
        .getDatabase(application)
        .glucoseDao()

    private val repository = GlucoseRepository(dao)

    private val syncCoordinator = GlucoseSyncCoordinator(dao)

    private val _syncState = MutableStateFlow(SyncState())
    val syncState: StateFlow<SyncState> = _syncState

    private val _configError = MutableStateFlow<String?>(null)
    val configError: StateFlow<String?> = _configError

    init {
        GlucoseSyncWorker.schedule(application.applicationContext)
        syncGlucose()
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

    val injectionsList = repository.injectionsList
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val insulinList = repository.insulinList
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val dataSourceConfig = repository.dataSourceConfig
        .map { it ?: DataSourceConfig() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DataSourceConfig()
        )

    fun syncFromSource(force: Boolean = false) {
        syncGlucose(force)
    }

    fun syncFromNightscout(force: Boolean = false) {
        syncGlucose(force)
    }

    fun syncGlucose(force: Boolean = false) {
        viewModelScope.launch {
            val config = repository.getDataSourceConfig()
            if (config.sourceType == DataSourceConfig.SOURCE_MANUAL) {
                _syncState.update {
                    it.copy(
                        status = SyncStatus.Idle,
                        lastSyncAt = config.lastSyncAt,
                        errorText = null
                    )
                }
                return@launch
            }

            if (!force && !config.autoSyncEnabled) {
                _syncState.update {
                    it.copy(
                        status = SyncStatus.Idle,
                        lastSyncAt = config.lastSyncAt,
                        errorText = null
                    )
                }
                return@launch
            }

            _syncState.value = SyncState(status = SyncStatus.Loading, lastSyncAt = config.lastSyncAt)

            when (val result = syncCoordinator.sync(config, force)) {
                is GlucoseSyncResult.Success -> {
                    _syncState.value = SyncState(
                        status = SyncStatus.Success,
                        lastSyncAt = result.syncedAt,
                        errorText = null
                    )
                }

                is GlucoseSyncResult.Skipped -> {
                    _syncState.value = SyncState(
                        status = SyncStatus.Idle,
                        lastSyncAt = config.lastSyncAt,
                        errorText = null
                    )
                }

                is GlucoseSyncResult.ValidationError -> {
                    _syncState.value = SyncState(
                        status = SyncStatus.Error,
                        lastSyncAt = config.lastSyncAt,
                        errorText = result.message
                    )
                }

                is GlucoseSyncResult.Failure -> {
                    _syncState.value = SyncState(
                        status = SyncStatus.Error,
                        lastSyncAt = config.lastSyncAt,
                        errorText = result.message
                    )
                }
            }
        }
    }

    fun saveDataSourceConfig(config: DataSourceConfig) {
        viewModelScope.launch {
            val normalized = config.normalizedForStorage()
            val validationErrors = normalized.validationErrors()
            if (validationErrors.isNotEmpty()) {
                _configError.value = validationErrors.joinToString("\n")
                _syncState.update {
                    it.copy(
                        status = SyncStatus.Error,
                        errorText = validationErrors.firstOrNull()
                    )
                }
                return@launch
            }

            repository.saveDataSourceConfig(normalized)
            _configError.value = null
            _syncState.update { it.copy(status = SyncStatus.Idle, errorText = null) }
            GlucoseSyncWorker.schedule(getApplication<Application>().applicationContext)
        }
    }

    fun addGlucose(level: Float) {
        viewModelScope.launch {
            repository.insertGlucose(
                GlucoseEntry(
                    glucoseMmolL = level,
                    glucoseMgDl = level * 18.0182f,
                    source = DataSourceConfig.SOURCE_MANUAL
                )
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

    fun addInjection(
        insulinUnits: Float,
        insulinType: String,
        injectionType: String,
        notes: String
    ) {
        viewModelScope.launch {
            repository.insertInjection(
                InjectionEntry(
                    insulinUnits = insulinUnits,
                    insulinType = insulinType,
                    injectionType = injectionType,
                    notes = notes
                )
            )
        }
    }

    fun addInsulin(
        units: Float,
        insulinType: String,
        note: String,
        source: String = DataSourceConfig.SOURCE_MANUAL
    ) {
        viewModelScope.launch {
            repository.insertInsulin(
                InsulinEntry(
                    units = units,
                    insulinType = insulinType,
                    note = note,
                    source = source
                )
            )
        }
    }
}