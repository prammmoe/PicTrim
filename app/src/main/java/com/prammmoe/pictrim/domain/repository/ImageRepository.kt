package com.prammmoe.pictrim.domain.repository

import android.content.Intent
import android.net.Uri
import com.prammmoe.pictrim.domain.model.ImageInfo
import com.prammmoe.pictrim.domain.model.ProcessedImage
import com.prammmoe.pictrim.domain.model.ProcessingOptions
import com.prammmoe.pictrim.domain.model.ProcessingResult

interface ImageRepository {
    suspend fun readImageInfo(uri: Uri): Result<ImageInfo>
    suspend fun process(uri: Uri, options: ProcessingOptions): ProcessingResult
    suspend fun save(image: ProcessedImage): Result<Uri>
    fun createShareIntent(image: ProcessedImage): Intent
    fun createViewIntent(uri: Uri): Intent
}
