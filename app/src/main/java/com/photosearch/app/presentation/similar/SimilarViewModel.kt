package com.photosearch.app.presentation.similar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.photosearch.app.data.model.SearchResult
import com.photosearch.app.domain.usecase.SearchPhotosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SimilarUiState(
    val similarImages: List<SearchResult> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SimilarViewModel @Inject constructor(
    private val searchPhotosUseCase: SearchPhotosUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SimilarUiState())
    val uiState: StateFlow<SimilarUiState> = _uiState.asStateFlow()

    fun searchSimilarImages(imageUri: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val results = searchPhotosUseCase.searchByImage(imageUri)
                _uiState.value = _uiState.value.copy(
                    similarImages = results,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }
}