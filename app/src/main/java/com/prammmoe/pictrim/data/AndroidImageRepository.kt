package com.prammmoe.pictrim.data

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import com.prammmoe.pictrim.domain.model.CompressionOptions
import com.prammmoe.pictrim.domain.model.CropAspectRatio
import com.prammmoe.pictrim.domain.model.EditorMode
import com.prammmoe.pictrim.domain.model.ImageInfo
import com.prammmoe.pictrim.domain.model.ImageProcessingRules
import com.prammmoe.pictrim.domain.model.OutputFormat
import com.prammmoe.pictrim.domain.model.ProcessedImage
import com.prammmoe.pictrim.domain.model.ProcessingOptions
import com.prammmoe.pictrim.domain.model.ProcessingResult
import com.prammmoe.pictrim.domain.model.ResizeOptions
import com.prammmoe.pictrim.domain.repository.ImageRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidImageRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) : ImageRepository {
    private val resolver: ContentResolver get() = context.contentResolver

    override suspend fun readImageInfo(uri: Uri): Result<ImageInfo> = withContext(Dispatchers.IO) {
        runCatching {
            val mimeType = resolver.getType(uri) ?: ""
            val format = ImageProcessingRules.formatFromMime(mimeType)
            require(format != null) { "Unsupported image format." }
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            require(bounds.outWidth > 0 && bounds.outHeight > 0) { "Could not read this image." }
            val rotation = readOrientation(uri)
            val dimensions = if (rotation == 90 || rotation == 270) bounds.outHeight to bounds.outWidth else bounds.outWidth to bounds.outHeight
            ImageInfo(
                uri = uri,
                displayName = queryName(uri) ?: "image.${format.extension}",
                sizeBytes = querySize(uri),
                width = dimensions.first,
                height = dimensions.second,
                mimeType = mimeType,
                format = format
            )
        }
    }

    override suspend fun process(uri: Uri, options: ProcessingOptions): ProcessingResult = withContext(Dispatchers.IO) {
        try {
            val source = readImageInfo(uri).getOrElse { throw it }
            val outputFormat = options.outputFormat.takeUnless { it == OutputFormat.ORIGINAL } ?: source.format
            if (options.compression.targetBytes != null && !ImageProcessingRules.supportsTargetSize(outputFormat)) {
                return@withContext ProcessingResult.Error("Target file size is available for JPG and WebP output only.")
            }
            val bitmap = decodeSafely(uri) ?: return@withContext ProcessingResult.Error("Could not decode this image.")
            val oriented = applyOrientation(bitmap, readOrientation(uri))
            if (oriented !== bitmap) bitmap.recycle()
            val cropped = crop(oriented, options.cropAspectRatio)
            if (cropped !== oriented) oriented.recycle()
            val resized = if (options.mode != EditorMode.COMPRESS) resize(cropped, options.resize) else cropped
            if (resized !== cropped) cropped.recycle()
            val output = createOutputFile(outputFormat)
            val targetReached = FileOutputStream(output).use { stream ->
                encode(resized, stream, outputFormat, options.compression, options.mode != EditorMode.RESIZE)
            }
            resized.recycle()
            if (options.metadataPolicy == com.prammmoe.pictrim.domain.model.MetadataPolicy.PRESERVE) copyExif(uri, output)
            val resultUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", output)
            val resultInfo = readImageInfo(resultUri).getOrElse {
                ImageInfo(resultUri, output.name, output.length(), 0, 0, outputFormat.mimeType, outputFormat)
            }
            ProcessingResult.Success(ProcessedImage(resultUri, resultInfo, targetReached))
        } catch (error: OutOfMemoryError) {
            ProcessingResult.Error("This image is too large to process on this device.")
        } catch (error: Exception) {
            ProcessingResult.Error(error.message ?: "We couldn't process this image. Please try another image.")
        }
    }

    override suspend fun save(image: ProcessedImage): Result<Uri> = withContext(Dispatchers.IO) {
        runCatching {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, timestampedName(image.info.format))
                put(MediaStore.Images.Media.MIME_TYPE, image.info.mimeType)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/PicTrim")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }
            val destination = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: error("Couldn't create the destination image.")
            try {
                resolver.openOutputStream(destination)?.use { output ->
                    resolver.openInputStream(image.uri)?.use { input -> input.copyTo(output) }
                        ?: error("Couldn't read processed image.")
                } ?: error("Couldn't save the image.")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear(); values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(destination, values, null, null)
                }
                destination
            } catch (error: Exception) {
                resolver.delete(destination, null, null)
                throw error
            }
        }
    }

    override fun createShareIntent(image: ProcessedImage): Intent = Intent.createChooser(
        Intent(Intent.ACTION_SEND).apply {
            type = image.info.mimeType
            putExtra(Intent.EXTRA_STREAM, image.uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        },
        null
    )

    override fun createViewIntent(uri: Uri): Intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "image/*")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    private fun decodeSafely(uri: Uri): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        val sample = calculateInSampleSize(bounds.outWidth, bounds.outHeight, 4096)
        val options = BitmapFactory.Options().apply { inSampleSize = sample; inPreferredConfig = Bitmap.Config.ARGB_8888 }
        return resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
    }

    private fun encode(bitmap: Bitmap, stream: FileOutputStream, format: OutputFormat, compression: CompressionOptions, shouldCompress: Boolean): Boolean {
        val compressFormat = when (format) {
            OutputFormat.JPEG -> Bitmap.CompressFormat.JPEG
            OutputFormat.PNG -> Bitmap.CompressFormat.PNG
            OutputFormat.WEBP -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Bitmap.CompressFormat.WEBP_LOSSY else Bitmap.CompressFormat.WEBP
            OutputFormat.ORIGINAL -> Bitmap.CompressFormat.JPEG
        }
        val target = compression.targetBytes
        if (target == null) {
            bitmap.compress(compressFormat, if (shouldCompress) compression.quality else 100, stream)
            return true
        }
        var low = 10; var high = 95; var best: ByteArray? = null
        repeat(8) {
            val quality = (low + high) / 2
            val bytes = java.io.ByteArrayOutputStream().use { buffer -> bitmap.compress(compressFormat, quality, buffer); buffer.toByteArray() }
            if (bytes.size <= target) { best = bytes; low = quality + 1 } else high = quality - 1
        }
        val bytes = best ?: java.io.ByteArrayOutputStream().use { buffer -> bitmap.compress(compressFormat, 10, buffer); buffer.toByteArray() }
        stream.write(bytes)
        return bytes.size <= target
    }

    private fun resize(source: Bitmap, options: ResizeOptions): Bitmap {
        val (width, height) = ImageProcessingRules.dimensions(source.width, source.height, options)
        return if (width == source.width && height == source.height) source else Bitmap.createScaledBitmap(source, width, height, true)
    }

    private fun crop(source: Bitmap, ratio: CropAspectRatio): Bitmap {
        val targetWidth = ratio.width ?: return source
        val targetHeight = checkNotNull(ratio.height)
        val sourceRatio = source.width.toFloat() / source.height
        val targetRatio = targetWidth.toFloat() / targetHeight
        val width: Int; val height: Int
        if (sourceRatio > targetRatio) { height = source.height; width = (height * targetRatio).toInt() } else { width = source.width; height = (width / targetRatio).toInt() }
        if (width == source.width && height == source.height) return source
        return Bitmap.createBitmap(source, (source.width - width) / 2, (source.height - height) / 2, width, height)
    }

    private fun applyOrientation(bitmap: Bitmap, degrees: Int): Bitmap {
        if (degrees == 0) return bitmap
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, Matrix().apply { postRotate(degrees.toFloat()) }, true)
    }

    private fun readOrientation(uri: Uri): Int = runCatching {
        resolver.openInputStream(uri)?.use { input ->
            when (ExifInterface(input).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } ?: 0
    }.getOrDefault(0)

    private fun copyExif(source: Uri, output: File) = runCatching {
        val input = resolver.openInputStream(source) ?: return@runCatching
        input.use { from ->
            val sourceExif = ExifInterface(from)
            val outputExif = ExifInterface(output)
            EXIF_TAGS.forEach { tag -> sourceExif.getAttribute(tag)?.let { outputExif.setAttribute(tag, it) } }
            outputExif.setAttribute(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL.toString())
            outputExif.saveAttributes()
        }
    }

    private fun queryName(uri: Uri): String? = resolver.query(uri, arrayOf(MediaStore.MediaColumns.DISPLAY_NAME), null, null, null)?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
    private fun querySize(uri: Uri): Long = resolver.query(uri, arrayOf(MediaStore.MediaColumns.SIZE), null, null, null)?.use { cursor -> if (cursor.moveToFirst()) cursor.getLong(0) else 0L } ?: 0L
    private fun createOutputFile(format: OutputFormat): File = File(context.filesDir, "processed").also { it.mkdirs() }.let { File(it, timestampedName(format)) }
    private fun timestampedName(format: OutputFormat): String = "PicTrim_${SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())}.${format.extension}"

    private companion object {
        val EXIF_TAGS = listOf(ExifInterface.TAG_DATETIME, ExifInterface.TAG_DATETIME_ORIGINAL, ExifInterface.TAG_MAKE, ExifInterface.TAG_MODEL, ExifInterface.TAG_GPS_LATITUDE, ExifInterface.TAG_GPS_LONGITUDE, ExifInterface.TAG_GPS_LATITUDE_REF, ExifInterface.TAG_GPS_LONGITUDE_REF)
        fun calculateInSampleSize(width: Int, height: Int, maxDimension: Int): Int { var sample = 1; while (width / sample > maxDimension || height / sample > maxDimension) sample *= 2; return sample }
    }
}
