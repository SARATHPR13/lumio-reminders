package com.lumio.app.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lumio.app.data.repository.BackupService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class BackupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val backupService: BackupService
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            backupService.createBackup()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "lumio_auto_backup"
    }
}
