package com.example.glucosetracker.sync

import com.example.glucosetracker.data.local.GlucoseDao
import com.example.glucosetracker.data.local.entities.DataSourceConfig
import com.example.glucosetracker.data.source.GlucoseDataSourceFactory

sealed class GlucoseSyncResult {
    data object Skipped : GlucoseSyncResult()

    data class Success(
        val insertedCount: Int,
        val syncedAt: Long
    ) : GlucoseSyncResult()

    data class ValidationError(val message: String) : GlucoseSyncResult()

    data class Failure(val message: String) : GlucoseSyncResult()
}

class GlucoseSyncCoordinator(
    private val dao: GlucoseDao,
    private val dataSourceFactory: GlucoseDataSourceFactory = GlucoseDataSourceFactory()
) {
    suspend fun sync(config: DataSourceConfig, force: Boolean = false): GlucoseSyncResult {
        val normalized = config.normalizedForStorage()
        val validationErrors = normalized.validationErrors()
        if (validationErrors.isNotEmpty()) {
            return GlucoseSyncResult.ValidationError(validationErrors.joinToString("\n"))
        }

        if (normalized.sourceType == DataSourceConfig.SOURCE_MANUAL || (!force && !normalized.autoSyncEnabled)) {
            return GlucoseSyncResult.Skipped
        }

        return try {
            val dataSource = dataSourceFactory.create(normalized)
            val entries = if (normalized.lastSyncAt == null) {
                dataSource.fetchHistory(normalized)
            } else {
                dataSource.fetchIncremental(normalized, normalized.lastSyncAt)
            }
            dao.insertGlucoseEntries(entries)
            val syncedAt = System.currentTimeMillis()
            dao.saveDataSourceConfig(normalized.copy(lastSyncAt = syncedAt))
            GlucoseSyncResult.Success(entries.size, syncedAt)
        } catch (e: Exception) {
            GlucoseSyncResult.Failure(e.message ?: "Не удалось синхронизировать данные")
        }
    }
}