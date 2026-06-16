package com.futurecode.recoverdeletedmessages.service


import android.content.Context
import androidx.work.*
import com.futurecode.recoverdeletedmessages.activity.MyApplication
import java.util.concurrent.TimeUnit

class MediaScanWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val repository = (applicationContext as MyApplication).repository
            repository.scanAllWhatsAppMedia(applicationContext)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "media_scan_work"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<MediaScanWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }

        fun runOnce(context: Context) {
            val workRequest = OneTimeWorkRequestBuilder<MediaScanWorker>().build()
            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }
}
