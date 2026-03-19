package com.photosearch.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.photosearch.app.data.model.ImageFeature
import com.photosearch.app.data.model.SearchResult
import com.photosearch.app.domain.usecase.GetPhotosUseCase
import com.photosearch.app.domain.usecase.SearchPhotosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 首页 ViewModel
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getPhotosUseCase: GetPhotosUseCase,
    private val searchPhotosUseCase: SearchPhotosUseCase
) : ViewModel() {
    
    private val _photos = MutableStateFlow<List<ImageFeature>>(emptyList())
    val photos: StateFlow<List<ImageFeature>> = _photos.asStateFlow()
    
    private val _searchResults = MutableStateFlow<List<SearchResult>>(emptyList())
    val searchResults: StateFlow<List<SearchResult>> = _searchResults.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _indexedCount = MutableStateFlow(0)
    val indexedCount: StateFlow<Int> = _indexedCount.asStateFlow()
    
    init {
        loadPhotos()
        loadIndexedCount()
    }
    
    private fun loadPhotos() {
        viewModelScope.launch {
            getPhotosUseCase().collect {
                _photos.value = it
            }
        }
    }
    
    private fun loadIndexedCount() {
        viewModelScope.launch {
            _indexedCount.value = getPhotosUseCase.getIndexedCount()
        }
    }
    
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchResults.value = emptyList()
        }
    }
    
    fun search() {
        viewModelScope.launch {
            _isLoading.value = true
            _searchResults.value = searchPhotosUseCase.searchByText(_searchQuery.value)
            _isLoading.value = false
        }
    }
    
    fun searchByImage(imageUri: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _searchResults.value = searchPhotosUseCase.searchByImage(imageUri)
            _isLoading.value = false
        }
    }
}
