package com.prammmoe.pictrim.ui.preview

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prammmoe.pictrim.domain.model.ImageInfo
import com.prammmoe.pictrim.domain.usecase.ReadImageInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PreviewUiState(val loading: Boolean = true, val image: ImageInfo? = null, val error: String? = null)

@HiltViewModel
class PreviewViewModel @Inject constructor(savedStateHandle: SavedStateHandle, private val readImageInfo: ReadImageInfoUseCase) : ViewModel() {
    private val _state = MutableStateFlow(PreviewUiState())
    val state: StateFlow<PreviewUiState> = _state.asStateFlow()
    private val uri = Uri.parse(checkNotNull(savedStateHandle["uri"]))
    init { load() }
    fun load() = viewModelScope.launch { _state.value = PreviewUiState(loading = true); _state.value = readImageInfo(uri).fold({ PreviewUiState(image = it, loading = false) }, { PreviewUiState(loading = false, error = it.message ?: "Couldn't open image.") }) }
}
