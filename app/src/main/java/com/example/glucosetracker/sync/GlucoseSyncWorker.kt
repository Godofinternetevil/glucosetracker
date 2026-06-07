package com.example.glucosetracker.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.glucosetracker.data.local.AppDatabase
import com.example.glucosetracker.data.local.entities.DataSourceConfig
import com.example.glucosetracker.data.source.GlucoseDataSourceFactory
import java.util.concurrent.TimeUnit

class GlucoseSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val dao = AppDatabase.getDatabase(applicationContext).glucoseDao()
        val config = dao.getDataSourceConfig() ?: DataSourceConfig()
        if (!config.autoSyncEnabled || config.sourceType != DataSourceConfig.SOURCE_NIGHTSCOUT) {
            return Result.success()
        }

        return try {
            val entries = GlucoseDataSourceFactory().create(config).fetchEntries(config)
            dao.insertGlucoseEntries(entries)
            dao.saveDataSourceConfig(config.copy(lastSyncAt = System.currentTimeMillis()))
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "glucose_periodic_sync"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<GlucoseSyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }
    }
}