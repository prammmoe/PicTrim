package com.prammmoe.pictrim.domain.usecase

import android.content.Intent
import android.net.Uri
import com.prammmoe.pictrim.domain.model.ProcessedImage
import com.prammmoe.pictrim.domain.model.ProcessingOptions
import com.prammmoe.pictrim.domain.repository.ImageRepository
import javax.inject.Inject

class ReadImageInfoUseCase @Inject constructor(private val repository: ImageRepository) {
    suspend operator fun invoke(uri: Uri) = repository.readImageInfo(uri)
}

class ProcessImageUseCase @Inject constructor(private val repository: ImageRepository) {
    suspend operator fun invoke(uri: Uri, options: ProcessingOptions) = repository.process(uri, options)
}

class SaveProcessedImageUseCase @Inject constructor(private val repository: ImageRepository) {
    suspend operator fun invoke(image: ProcessedImage) = repository.save(image)
}

class ShareProcessedImageUseCase @Inject constructor(private val repository: ImageRepository) {
    operator fun invoke(image: ProcessedImage): Intent = repository.createShareIntent(image)
}

class ViewSavedImageUseCase @Inject constructor(private val repository: ImageRepository) {
    operator fun invoke(uri: Uri): Intent = repository.createViewIntent(uri)
}
