package com.prammmoe.pictrim.domain.model

object ImageProcessingRules {
    fun dimensions(originalWidth: Int, originalHeight: Int, options: ResizeOptions): Pair<Int, Int> {
        options.percentage?.let { return (originalWidth * it / 100).coerceAtLeast(1) to (originalHeight * it / 100).coerceAtLeast(1) }
        val width = options.width?.coerceAtLeast(1)
        val height = options.height?.coerceAtLeast(1)
        if (options.keepAspectRatio) return when {
            width != null -> width to (originalHeight.toFloat() * width / originalWidth).toInt().coerceAtLeast(1)
            height != null -> (originalWidth.toFloat() * height / originalHeight).toInt().coerceAtLeast(1) to height
            else -> originalWidth to originalHeight
        }
        return (width ?: originalWidth) to (height ?: originalHeight)
    }

    fun supportsTargetSize(format: OutputFormat) = format == OutputFormat.JPEG || format == OutputFormat.WEBP
    fun formatFromMime(mimeType: String): OutputFormat? = when (mimeType.lowercase()) { "image/jpeg", "image/jpg" -> OutputFormat.JPEG; "image/png" -> OutputFormat.PNG; "image/webp" -> OutputFormat.WEBP; else -> null }
    fun savingsPercent(originalBytes: Long, outputBytes: Long): Int = if (originalBytes <= 0) 0 else ((1 - outputBytes.toDouble() / originalBytes) * 100).toInt()
}
