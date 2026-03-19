package com.photosearch.app.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.photosearch.app.data.repository.PhotoSearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val indexedCount: Int = 0,
    val totalCount: Int = 0
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: PhotoSearchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            val indexed = repository.getIndexedCount()
            val total = repository.getTotalCount()
            _uiState.value = SettingsUiState(
                indexedCount = indexed,
                totalCount = total
            )
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            // 这里应该只清除索引标记，而不是删除数据库记录
            // 实际实现根据需要调整
            loadStats()
        }
    }
}