package com.prammmoe.pictrim.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.prammmoe.pictrim.domain.model.BatchItemStatus
import com.prammmoe.pictrim.domain.model.ProcessingResult
import com.prammmoe.pictrim.domain.repository.ImageRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.util.UUID

class BatchWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val entry = EntryPointAccessors.fromApplication(applicationContext, BatchWorkerEntryPoint::class.java)
        val store = entry.batchStore(); val images = entry.imageRepository(); val id = inputData.getString(KEY_JOB_ID) ?: return Result.failure()
        val job = store.load(id) ?: return Result.failure()
        setForeground(notification("Processing ${job.items.size} images"))
        job.items.forEachIndexed { index, item ->
            if (isStopped) return Result.failure()
            store.updateItem(id, index, item.copy(status = BatchItemStatus.PROCESSING))
            when (val result = images.process(android.net.Uri.parse(item.sourceUri), job.options)) {
                is ProcessingResult.Success -> store.updateItem(id, index, item.copy(status = BatchItemStatus.SUCCESS, outputUri = result.image.uri.toString()))
                is ProcessingResult.Error -> store.updateItem(id, index, item.copy(status = BatchItemStatus.FAILED, error = result.message))
            }
            setProgress(workDataOf("completed" to index + 1, "total" to job.items.size))
        }
        return Result.success()
    }
    private fun notification(text: String): ForegroundInfo { val channel = "pictrim_batch"; val manager = applicationContext.getSystemService(NotificationManager::class.java); manager.createNotificationChannel(NotificationChannel(channel, "PicTrim processing", NotificationManager.IMPORTANCE_LOW)); val notification = NotificationCompat.Builder(applicationContext, channel).setSmallIcon(android.R.drawable.stat_sys_upload).setContentTitle("PicTrim").setContentText(text).setOngoing(true).build(); return ForegroundInfo(71, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC) }
    companion object { const val KEY_JOB_ID = "job_id" }
}

@EntryPoint @InstallIn(SingletonComponent::class)
interface BatchWorkerEntryPoint { fun batchStore(): BatchStore; fun imageRepository(): ImageRepository }
