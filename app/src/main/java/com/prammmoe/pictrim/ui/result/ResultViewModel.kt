package com.prammmoe.pictrim.ui.result

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prammmoe.pictrim.domain.model.ImageInfo
import com.prammmoe.pictrim.domain.model.ProcessedImage
import com.prammmoe.pictrim.domain.usecase.ReadImageInfoUseCase
import com.prammmoe.pictrim.domain.usecase.SaveProcessedImageUseCase
import com.prammmoe.pictrim.domain.usecase.ShareProcessedImageUseCase
import com.prammmoe.pictrim.domain.usecase.ViewSavedImageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ResultUiState(val loading: Boolean = true, val original: ImageInfo? = null, val result: ProcessedImage? = null, val savedUri: Uri? = null, val saving: Boolean = false, val error: String? = null)

@HiltViewModel
class ResultViewModel @Inject constructor(savedStateHandle: SavedStateHandle, private val readInfo: ReadImageInfoUseCase, private val saveImage: SaveProcessedImageUseCase, private val shareImage: ShareProcessedImageUseCase, private val viewSavedImage: ViewSavedImageUseCase) : ViewModel() {
    private val originalUri = Uri.parse(checkNotNull(savedStateHandle.get<String>("originalUri")))
    private val resultUri = Uri.parse(checkNotNull(savedStateHandle.get<String>("resultUri")))
    private val _state = MutableStateFlow(ResultUiState())
    val state: StateFlow<ResultUiState> = _state.asStateFlow()
    init { load() }
    private fun load() = viewModelScope.launch {
        val original = readInfo(originalUri).getOrNull()
        val output = readInfo(resultUri).getOrNull()
        _state.value = if (original != null && output != null) ResultUiState(loading = false, original = original, result = ProcessedImage(resultUri, output)) else ResultUiState(loading = false, error = "Couldn't read the processed image.")
    }
    fun save() = viewModelScope.launch {
        val image = _state.value.result ?: return@launch
        _state.value = _state.value.copy(saving = true, error = null)
        _state.value = saveImage(image).fold({ _state.value.copy(saving = false, savedUri = it) }, { _state.value.copy(saving = false, error = it.message ?: "Couldn't save the image.") })
    }
    fun share(): Intent? = _state.value.result?.let(shareImage::invoke)
    fun showInGallery(): Intent? = _state.value.savedUri?.let(viewSavedImage::invoke)
}
