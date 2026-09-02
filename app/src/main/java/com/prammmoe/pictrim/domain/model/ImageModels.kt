package com.prammmoe.pictrim.domain.model

import android.net.Uri

enum class OutputFormat(val label: String, val mimeType: String, val extension: String) {
    ORIGINAL("Keep original", "", ""),
    JPEG("JPG", "image/jpeg", "jpg"),
    PNG("PNG", "image/png", "png"),
    WEBP("WebP", "image/webp", "webp")
}

enum class EditorMode { COMPRESS, RESIZE, COMPRESS_AND_RESIZE }
enum class CropAspectRatio(val label: String, val width: Int?, val height: Int?) { ORIGINAL("Original", null, null), SQUARE("1:1", 1, 1), PORTRAIT("4:5", 4, 5), STORY("9:16", 9, 16), WIDE("16:9", 16, 9) }
enum class MetadataPolicy { PRESERVE, REMOVE }

data class ImageInfo(
    val uri: Uri,
    val displayName: String,
    val sizeBytes: Long,
    val width: Int,
    val height: Int,
    val mimeType: String,
    val format: OutputFormat
)

data class CompressionOptions(
    val quality: Int = 75,
    val targetBytes: Long? = null
)

data class ResizeOptions(
    val width: Int? = null,
    val height: Int? = null,
    val percentage: Int? = null,
    val keepAspectRatio: Boolean = true
)

data class ProcessingOptions(
    val mode: EditorMode,
    val compression: CompressionOptions = CompressionOptions(),
    val resize: ResizeOptions = ResizeOptions(),
    val outputFormat: OutputFormat = OutputFormat.ORIGINAL,
    val cropAspectRatio: CropAspectRatio = CropAspectRatio.ORIGINAL,
    val metadataPolicy: MetadataPolicy = MetadataPolicy.PRESERVE
)

data class ProcessedImage(
    val uri: Uri,
    val info: ImageInfo,
    val targetReached: Boolean = true
)

sealed interface ProcessingResult {
    data class Success(val image: ProcessedImage) : ProcessingResult
    data class Error(val message: String) : ProcessingResult
}

enum class BatchItemStatus { PENDING, PROCESSING, SUCCESS, FAILED }
data class BatchItem(val sourceUri: String, val displayName: String, val status: BatchItemStatus = BatchItemStatus.PENDING, val outputUri: String? = null, val savedUri: String? = null, val error: String? = null)
data class BatchJob(val id: String, val options: ProcessingOptions, val items: List<BatchItem>, val createdAtMillis: Long = System.currentTimeMillis())
data class BatchProgress(val completed: Int, val total: Int, val running: Boolean, val cancelled: Boolean = false) { val percent: Int get() = if (total == 0) 0 else completed * 100 / total }
