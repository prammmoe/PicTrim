package com.prammmoe.pictrim.ui.batch

import android.net.Uri
import android.content.Context
import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prammmoe.pictrim.data.BatchStore
import com.prammmoe.pictrim.data.BatchWorkScheduler
import com.prammmoe.pictrim.domain.model.CompressionOptions
import com.prammmoe.pictrim.domain.model.CropAspectRatio
import com.prammmoe.pictrim.domain.model.EditorMode
import com.prammmoe.pictrim.domain.model.ImageInfo
import com.prammmoe.pictrim.domain.model.MetadataPolicy
import com.prammmoe.pictrim.domain.model.OutputFormat
import com.prammmoe.pictrim.domain.model.ProcessingOptions
import com.prammmoe.pictrim.domain.model.ResizeOptions
import com.prammmoe.pictrim.domain.usecase.ReadImageInfoUseCase
import com.prammmoe.pictrim.domain.usecase.SaveProcessedImageUseCase
import com.prammmoe.pictrim.domain.usecase.ViewSavedImageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BatchEditorUiState(val images: List<ImageInfo> = emptyList(), val rejected: Int = 0, val quality: Int = 75, val targetKb: String = "", val width: String = "", val height: String = "", val outputFormat: OutputFormat = OutputFormat.ORIGINAL, val crop: CropAspectRatio = CropAspectRatio.ORIGINAL, val removeMetadata: Boolean = false, val processing: Boolean = false, val jobId: String? = null, val error: String? = null)
@HiltViewModel class BatchEditorViewModel @Inject constructor(@param:ApplicationContext private val context: Context, private val readImage: ReadImageInfoUseCase, private val scheduler: BatchWorkScheduler) : ViewModel() {
    private val _state = MutableStateFlow(BatchEditorUiState()); val state: StateFlow<BatchEditorUiState> = _state.asStateFlow()
    fun select(uris: List<Uri>) = viewModelScope.launch { val accepted = uris.take(50).mapNotNull { uri -> runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }; readImage(uri).getOrNull() }; _state.value = _state.value.copy(images = accepted, rejected = uris.size - accepted.size, error = if (accepted.isEmpty()) "No supported images selected." else null) }
    fun quality(v: Int) = update { copy(quality = v) }; fun target(v: String) = update { copy(targetKb = v.filter(Char::isDigit)) }; fun width(v: String) = update { copy(width = v.filter(Char::isDigit)) }; fun height(v: String) = update { copy(height = v.filter(Char::isDigit)) }; fun format(v: OutputFormat) = update { copy(outputFormat = v) }; fun crop(v: CropAspectRatio) = update { copy(crop = v) }; fun removeMetadata(v: Boolean) = update { copy(removeMetadata = v) }
    fun start() { val state = _state.value; if (state.images.isEmpty()) { update { copy(error = "Choose at least one image.") }; return }; val options = ProcessingOptions(EditorMode.COMPRESS_AND_RESIZE, CompressionOptions(state.quality, state.targetKb.toLongOrNull()?.times(1024)), ResizeOptions(state.width.toIntOrNull(), state.height.toIntOrNull()), state.outputFormat, state.crop, if (state.removeMetadata) MetadataPolicy.REMOVE else MetadataPolicy.PRESERVE); runCatching { scheduler.enqueue(state.images.map { it.uri.toString() }, state.images.map { it.displayName }, options) }.onSuccess { id -> _state.value = state.copy(processing = true, jobId = id) }.onFailure { error -> _state.value = state.copy(processing = false, error = error.message ?: "Couldn't start batch processing.") } }
    private inline fun update(block: BatchEditorUiState.() -> BatchEditorUiState) { _state.value = _state.value.block() }
}

data class BatchResultUiState(val job: com.prammmoe.pictrim.domain.model.BatchJob? = null, val savedCount: Int = 0)
@HiltViewModel class BatchResultViewModel @Inject constructor(savedStateHandle: SavedStateHandle, private val store: BatchStore, private val readImage: ReadImageInfoUseCase, private val saveImage: SaveProcessedImageUseCase, private val viewSavedImage: ViewSavedImageUseCase) : ViewModel() {
    private val id = checkNotNull(savedStateHandle.get<String>("jobId")); private val _state = MutableStateFlow(BatchResultUiState()); val state: StateFlow<BatchResultUiState> = _state.asStateFlow()
    init { viewModelScope.launch { repeat(600) { refresh(); delay(1000) } } }
    fun refresh() { _state.value = _state.value.copy(job = store.load(id)) }
    fun saveAll() = viewModelScope.launch { val job = _state.value.job ?: return@launch; var saved = 0; job.items.forEachIndexed { index, item -> if (item.outputUri != null && item.savedUri == null) { val uri = Uri.parse(item.outputUri); val info = readImage(uri).getOrNull() ?: return@forEachIndexed; saveImage(com.prammmoe.pictrim.domain.model.ProcessedImage(uri, info)).onSuccess { savedUri -> store.updateItem(id, index, item.copy(savedUri = savedUri.toString())) }.onSuccess { saved++ } } }; refresh(); _state.value = _state.value.copy(savedCount = saved) }
    fun openGallery(savedUri: String) = viewSavedImage(Uri.parse(savedUri))
}
