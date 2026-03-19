package com.photosearch.app.presentation.duplicate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.photosearch.app.data.model.SearchResult
import com.photosearch.app.domain.usecase.FindDuplicatesUseCase
import com.photosearch.app.data.repository.PhotoSearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DuplicatesUiState(
    val duplicateGroups: Map<String, List<SearchResult>> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DuplicatesViewModel @Inject constructor(
    private val findDuplicatesUseCase: FindDuplicatesUseCase,
    private val repository: PhotoSearchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DuplicatesUiState())
    val uiState: StateFlow<DuplicatesUiState> = _uiState.asStateFlow()

    fun findDuplicates(threshold: Float = 0.95f) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val groups = findDuplicatesUseCase(threshold)
                _uiState.value = _uiState.value.copy(
                    duplicateGroups = groups,
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

    fun deleteImage(uri: String) {
        viewModelScope.launch {
            try {
                repository.deleteImage(uri)
                // 刷新列表
                findDuplicates()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun deleteAllDuplicates() {
        viewModelScope.launch {
            try {
                _uiState.value.duplicateGroups.values.flatten().forEach { result ->
                    repository.deleteImage(result.imageFeature.uri)
                }
                // 刷新列表
                findDuplicates()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}