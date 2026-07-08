package com.example.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val database = AppDatabase.getDatabase(applicationContext)
        val offlineActionDao = database.offlineActionDao()
        val pendingActions = offlineActionDao.getAllPendingActions()

        if (pendingActions.isEmpty()) {
            return@withContext Result.success()
        }

        // We will implement the actual network calls in Phase 2 when ApiService is updated
        var allSuccess = true

        for (action in pendingActions) {
            try {
                // TODO: Parse action.payloadJson and execute Network request
                
                // For now, just pretend it succeeded and delete
                offlineActionDao.deleteAction(action.id)
            } catch (e: Exception) {
                e.printStackTrace()
                allSuccess = false
            }
        }

        if (allSuccess) {
            Result.success()
        } else {
            Result.retry()
        }
    }
}
