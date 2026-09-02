package com.prammmoe.pictrim.data

import android.content.Context
import com.prammmoe.pictrim.domain.model.BatchItem
import com.prammmoe.pictrim.domain.model.BatchItemStatus
import com.prammmoe.pictrim.domain.model.BatchJob
import com.prammmoe.pictrim.domain.model.CompressionOptions
import com.prammmoe.pictrim.domain.model.CropAspectRatio
import com.prammmoe.pictrim.domain.model.EditorMode
import com.prammmoe.pictrim.domain.model.MetadataPolicy
import com.prammmoe.pictrim.domain.model.OutputFormat
import com.prammmoe.pictrim.domain.model.ProcessingOptions
import com.prammmoe.pictrim.domain.model.ResizeOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONArray
import org.json.JSONObject

@Singleton
class BatchStore @Inject constructor(@param:ApplicationContext private val context: Context) {
    private fun file(id: String) = File(File(context.filesDir, "batches").also { it.mkdirs() }, "$id.json")
    @Synchronized fun save(job: BatchJob) { file(job.id).writeText(job.toJson().toString()) }
    @Synchronized fun load(id: String): BatchJob? = runCatching { JSONObject(file(id).readText()).toJob() }.getOrNull()
    @Synchronized fun updateItem(id: String, index: Int, item: BatchItem) { load(id)?.let { job -> save(job.copy(items = job.items.toMutableList().apply { set(index, item) })) } }
    @Synchronized fun delete(id: String) { load(id)?.items?.mapNotNull { it.outputUri }?.forEach { uri -> File(uri.removePrefix("file://")).delete() }; file(id).delete() }
    fun cleanupExpired(now: Long = System.currentTimeMillis()) { File(context.filesDir, "batches").listFiles()?.filter { now - it.lastModified() > DAY_MILLIS }?.forEach { it.delete() } }

    private fun BatchJob.toJson() = JSONObject().apply { put("id", id); put("created", createdAtMillis); put("mode", options.mode.name); put("quality", options.compression.quality); put("target", options.compression.targetBytes ?: -1); put("format", options.outputFormat.name); put("crop", options.cropAspectRatio.name); put("metadata", options.metadataPolicy.name); put("width", options.resize.width ?: -1); put("height", options.resize.height ?: -1); put("percent", options.resize.percentage ?: -1); put("keep", options.resize.keepAspectRatio); put("items", JSONArray().also { array -> items.forEach { item -> array.put(JSONObject().apply { put("uri", item.sourceUri); put("name", item.displayName); put("status", item.status.name); put("output", item.outputUri); put("saved", item.savedUri); put("error", item.error) }) } }) }
    private fun JSONObject.toJob(): BatchJob { val a = getJSONArray("items"); val items = List(a.length()) { i -> a.getJSONObject(i).let { BatchItem(it.getString("uri"), it.getString("name"), BatchItemStatus.valueOf(it.getString("status")), it.optString("output").takeIf(String::isNotBlank), it.optString("saved").takeIf(String::isNotBlank), it.optString("error").takeIf(String::isNotBlank)) } }; return BatchJob(getString("id"), ProcessingOptions(EditorMode.valueOf(getString("mode")), CompressionOptions(getInt("quality"), optLong("target").takeIf { it >= 0 }), ResizeOptions(optInt("width").takeIf { it >= 0 }, optInt("height").takeIf { it >= 0 }, optInt("percent").takeIf { it >= 0 }, getBoolean("keep")), OutputFormat.valueOf(getString("format")), CropAspectRatio.valueOf(getString("crop")), MetadataPolicy.valueOf(getString("metadata"))), items, getLong("created")) }
    private companion object { const val DAY_MILLIS = 24 * 60 * 60 * 1000L }
}
