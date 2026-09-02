package com.prammmoe.pictrim.data

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.prammmoe.pictrim.domain.model.BatchItem
import com.prammmoe.pictrim.domain.model.BatchJob
import com.prammmoe.pictrim.domain.model.ProcessingOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BatchWorkScheduler @Inject constructor(@param:ApplicationContext private val context: Context, private val store: BatchStore) {
    fun enqueue(uris: List<String>, names: List<String>, options: ProcessingOptions): String {
        val id = UUID.randomUUID().toString()
        store.cleanupExpired()
        val items = uris.mapIndexed { index, uri -> BatchItem(uri, names.getOrElse(index) { "Image ${index + 1}" }) }
        store.save(BatchJob(id, options, items))
        val request = OneTimeWorkRequestBuilder<BatchWorker>()
            .setInputData(workDataOf(BatchWorker.KEY_JOB_ID to id))
            .addTag("pictrim_batch")
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork("pictrim-$id", ExistingWorkPolicy.REPLACE, request)
        return id
    }
    fun cancel(id: String) = WorkManager.getInstance(context).cancelUniqueWork("pictrim-$id")
}
