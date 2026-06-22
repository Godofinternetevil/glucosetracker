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
import com.example.glucosetracker.data.repository.GlucosePredictionRepository
import com.example.glucosetracker.data.repository.GlucoseRepository
import com.example.glucosetracker.domain.ml.GlucosePredictor
import com.example.glucosetracker.domain.ml.PredictionResult
import com.example.glucosetracker.sync.GlucoseSyncCoordinator
import com.example.glucosetracker.sync.GlucoseSyncResult
import com.example.glucosetracker.sync.GlucoseSyncWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
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

enum class AddEventType {
    Glucose,
    Meal,
    Insulin
}

enum class AddEventStatus {
    Idle,
    Loading,
    Success,
    Error
}

data class AddEventState(
    val type: AddEventType? = null,
    val status: AddEventStatus = AddEventStatus.Idle,
    val errorText: String? = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)

    private val dao = database.glucoseDao()

    private val repository = GlucoseRepository(dao)

    private val predictionRepository = GlucosePredictionRepository(database.predictionDao())

    private val syncCoordinator = GlucoseSyncCoordinator(dao)

    private val glucosePredictor = GlucosePredictor(application.applicationContext)

    private val _syncState = MutableStateFlow(SyncState())
    val syncState: StateFlow<SyncState> = _syncState

    private val _configError = MutableStateFlow<String?>(null)
    val configError: StateFlow<String?> = _configError

    private val _addEventState = MutableStateFlow(AddEventState())
    val addEventState: StateFlow<AddEventState> = _addEventState

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

    val predictedGlucose: StateFlow<PredictionResult?> = combine(
        glucoseList,
        mealsList,
        insulinList
    ) { glucose, meals, insulin ->
        glucosePredictor.predict30Min(glucose, meals, insulin, System.currentTimeMillis())
    }.onEach { prediction ->
        prediction?.let { predictionRepository.savePrediction(it) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val glucosePrediction: StateFlow<PredictionResult?> = predictedGlucose

    val dataSourceConfig = repository.dataSourceConfig
        .map { it ?: DataSourceConfig() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DataSourceConfig()
        )

    override fun onCleared() {
        glucosePredictor.close()
        super.onCleared()
    }

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


    fun exportCsv(): String {
        val header = listOf(
            "type",
            "timestamp",
            "name",
            "glucoseMmolL",
            "carbsGrams",
            "proteinGrams",
            "fatGrams",
            "calories",
            "mealType",
            "note",
            "source"
        ).joinToString(",")
        val meals = mealsList.value.map { meal ->
            listOf(
                "meal",
                meal.timestamp.toString(),
                meal.mealName,
                "",
                meal.carbsGrams.toString(),
                meal.proteinGrams?.toString().orEmpty(),
                meal.fatGrams?.toString().orEmpty(),
                meal.calories?.toString().orEmpty(),
                meal.mealType,
                meal.note,
                meal.source
            ).joinToCsvRow()
        }
        val glucose = glucoseList.value.map { entry ->
            listOf(
                "glucose",
                entry.timestamp.toString(),
                "",
                entry.glucoseMmolL.toString(),
                "",
                "",
                "",
                "",
                "",
                entry.trendDirection.orEmpty(),
                entry.source
            ).joinToCsvRow()
        }
        return (listOf(header) + glucose + meals).joinToString("\n")
    }

    fun clearAddEventState() {
        _addEventState.value = AddEventState()
    }

    fun addGlucose(level: Float) {
        viewModelScope.launch {
            _addEventState.value = AddEventState(type = AddEventType.Glucose, status = AddEventStatus.Loading)
            runCatching {
                repository.insertGlucose(
                    GlucoseEntry(
                        glucoseMmolL = level,
                        glucoseMgDl = level * 18.0182f,
                        source = DataSourceConfig.SOURCE_MANUAL,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }.onSuccess {
                _addEventState.value = AddEventState(type = AddEventType.Glucose, status = AddEventStatus.Success)
            }.onFailure { error ->
                _addEventState.value = AddEventState(
                    type = AddEventType.Glucose,
                    status = AddEventStatus.Error,
                    errorText = error.localizedMessage ?: "Не удалось сохранить глюкозу"
                )
            }
        }
    }

    fun addMeal(
        name: String,
        carbsGrams: Float,
        proteinGrams: Float?,
        fatGrams: Float?,
        calories: Int?,
        mealType: String,
        note: String,
        source: String = DataSourceConfig.SOURCE_MANUAL,
        timestamp: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            _addEventState.value = AddEventState(type = AddEventType.Meal, status = AddEventStatus.Loading)
            runCatching {
                repository.insertMeal(
                    createMealEntry(
                        name = name,
                        carbsGrams = carbsGrams,
                        proteinGrams = proteinGrams,
                        fatGrams = fatGrams,
                        calories = calories,
                        mealType = mealType,
                        note = note,
                        source = source,
                        timestamp = timestamp
                    )
                )
            }.onSuccess {
                _addEventState.value = AddEventState(type = AddEventType.Meal, status = AddEventStatus.Success)
            }.onFailure { error ->
                _addEventState.value = AddEventState(
                    type = AddEventType.Meal,
                    status = AddEventStatus.Error,
                    errorText = error.localizedMessage ?: "Не удалось сохранить еду"
                )
            }
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
            _addEventState.value = AddEventState(type = AddEventType.Insulin, status = AddEventStatus.Loading)
            runCatching {
                repository.insertInsulin(
                    InsulinEntry(
                        units = units,
                        insulinType = insulinType,
                        note = note,
                        source = source,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }.onSuccess {
                _addEventState.value = AddEventState(type = AddEventType.Insulin, status = AddEventStatus.Success)
            }.onFailure { error ->
                _addEventState.value = AddEventState(
                    type = AddEventType.Insulin,
                    status = AddEventStatus.Error,
                    errorText = error.localizedMessage ?: "Не удалось сохранить инсулин"
                )
            }
        }
    }
}

private fun List<String>.joinToCsvRow(): String = joinToString(",") { value ->
    val escaped = value.replace("\"", "\"\"")
    if (escaped.any { it == ',' || it == '\n' || it == '"' }) "\"$escaped\"" else escaped
}

internal fun createMealEntry(
    name: String,
    carbsGrams: Float,
    proteinGrams: Float?,
    fatGrams: Float?,
    calories: Int?,
    mealType: String,
    note: String,
    source: String = DataSourceConfig.SOURCE_MANUAL,
    timestamp: Long = System.currentTimeMillis()
): MealEntry = MealEntry(
    mealName = name,
    carbsGrams = carbsGrams,
    proteinGrams = proteinGrams,
    fatGrams = fatGrams,
    calories = calories,
    mealType = mealType,
    note = note,
    source = source,
    timestamp = timestamp
)