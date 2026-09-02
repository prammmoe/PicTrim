package com.prammmoe.pictrim.ui.editor

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prammmoe.pictrim.domain.model.CompressionOptions
import com.prammmoe.pictrim.domain.model.EditorMode
import com.prammmoe.pictrim.domain.model.OutputFormat
import com.prammmoe.pictrim.domain.model.CropAspectRatio
import com.prammmoe.pictrim.domain.model.MetadataPolicy
import com.prammmoe.pictrim.domain.model.ProcessingOptions
import com.prammmoe.pictrim.domain.model.ResizeOptions
import com.prammmoe.pictrim.domain.usecase.ProcessImageUseCase
import com.prammmoe.pictrim.domain.usecase.ReadImageInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EditorUiState(
    val mode: EditorMode,
    val quality: Int = 75,
    val targetKb: String = "",
    val outputFormat: OutputFormat = OutputFormat.ORIGINAL,
    val width: String = "",
    val height: String = "",
    val percentage: Int? = null,
    val keepAspectRatio: Boolean = true,
    val processing: Boolean = false,
    val error: String? = null,
    val resultUri: Uri? = null,
    val originalUri: Uri,
    val sourceFormat: OutputFormat? = null,
    val crop: CropAspectRatio = CropAspectRatio.ORIGINAL,
    val removeMetadata: Boolean = false
)

@HiltViewModel
class EditorViewModel @Inject constructor(savedStateHandle: SavedStateHandle, private val processImage: ProcessImageUseCase, private val readImageInfo: ReadImageInfoUseCase) : ViewModel() {
    private val uri = Uri.parse(checkNotNull(savedStateHandle.get<String>("uri")))
    private val mode = EditorMode.valueOf(checkNotNull(savedStateHandle.get<String>("mode")))
    private val _state = MutableStateFlow(EditorUiState(mode = mode, originalUri = uri))
    val state: StateFlow<EditorUiState> = _state.asStateFlow()
    init { viewModelScope.launch { readImageInfo(uri).getOrNull()?.let { info -> update { copy(sourceFormat = info.format) } } } }
    fun setQuality(value: Int) = update { copy(quality = value, targetKb = "") }
    fun setTarget(value: String) = update { copy(targetKb = value.filter(Char::isDigit)) }
    fun setFormat(value: OutputFormat) = update { copy(outputFormat = value) }
    fun setWidth(value: String) = update { copy(width = value.filter(Char::isDigit), percentage = null) }
    fun setHeight(value: String) = update { copy(height = value.filter(Char::isDigit), percentage = null) }
    fun setPercentage(value: Int) = update { copy(percentage = value, width = "", height = "") }
    fun setKeepAspect(value: Boolean) = update { copy(keepAspectRatio = value) }
    fun setCrop(value: CropAspectRatio) = update { copy(crop = value) }
    fun setRemoveMetadata(value: Boolean) = update { copy(removeMetadata = value) }
    fun dismissError() = update { copy(error = null) }
    fun process() = viewModelScope.launch {
        val current = _state.value
        val target = current.targetKb.toLongOrNull()?.times(1024)
        if (target != null && target <= 0) return@launch
        _state.value = current.copy(processing = true, error = null)
        val options = ProcessingOptions(
            mode = current.mode,
            compression = CompressionOptions(current.quality, target),
            resize = ResizeOptions(current.width.toIntOrNull(), current.height.toIntOrNull(), current.percentage, current.keepAspectRatio),
            outputFormat = current.outputFormat,
            cropAspectRatio = current.crop,
            metadataPolicy = if (current.removeMetadata) MetadataPolicy.REMOVE else MetadataPolicy.PRESERVE
        )
        when (val result = processImage(uri, options)) {
            is com.prammmoe.pictrim.domain.model.ProcessingResult.Success -> _state.value = current.copy(processing = false, resultUri = result.image.uri)
            is com.prammmoe.pictrim.domain.model.ProcessingResult.Error -> _state.value = current.copy(processing = false, error = result.message)
        }
    }
    private inline fun update(block: EditorUiState.() -> EditorUiState) { _state.value = _state.value.block() }
}
