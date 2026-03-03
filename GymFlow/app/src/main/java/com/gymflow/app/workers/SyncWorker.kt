package com.gymflow.app.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.gymflow.app.data.local.AppDatabase
import java.util.concurrent.TimeUnit

class SyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            syncWorkouts()
            syncUserProfile()
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private suspend fun syncWorkouts() {
        val database = AppDatabase.getInstance(applicationContext)
        val workouts = database.workoutDao()
        // Sinkronizacija treninga s Firebase Firestore
    }

    private suspend fun syncUserProfile() {
        val database = AppDatabase.getInstance(applicationContext)
        val userProfile = database.userProfileDao()
        // Sinkronizacija profila s Firebase Firestore
    }

    companion object {

        const val WORK_NAME_PERIODIC = "gymflow_periodic_sync"
        const val WORK_NAME_ONE_TIME = "gymflow_one_time_sync"

        fun schedulePeriodicSync(context: Context) {
            val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
                repeatInterval = 6,
                repeatIntervalTimeUnit = TimeUnit.HOURS
            )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME_PERIODIC,
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
        }

        fun scheduleOneTimeSync(context: Context) {
            val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME_ONE_TIME,
                androidx.work.ExistingWorkPolicy.REPLACE,
                syncRequest
            )
        }

        fun cancelSync(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME_PERIODIC)
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME_ONE_TIME)
        }
    }
}