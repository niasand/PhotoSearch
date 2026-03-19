package com.photosearch.app.domain.usecase

import com.photosearch.app.data.model.ImageFeature
import com.photosearch.app.data.model.IndexStatus
import com.photosearch.app.data.model.SearchResult
import com.photosearch.app.data.repository.PhotoSearchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * 索引图片 UseCase
 */
class IndexPhotosUseCase @Inject constructor(
    private val repository: PhotoSearchRepository
) {
    private val _indexStatus = MutableStateFlow<IndexStatus>(IndexStatus.Idle)
    val indexStatus: StateFlow<IndexStatus> = _indexStatus.asStateFlow()
    
    suspend operator fun invoke(photos: List<ImageFeature>) {
        _indexStatus.value = IndexStatus.Running(0, photos.size)
        
        try {
            repository.saveImageFeatures(photos)
            _indexStatus.value = IndexStatus.Completed
        } catch (e: Exception) {
            _indexStatus.value = IndexStatus.Error(e.message ?: "Unknown error")
        }
    }
}

/**
 * 搜索图片 UseCase
 */
class SearchPhotosUseCase @Inject constructor(
    private val repository: PhotoSearchRepository
) {
    suspend fun searchByText(query: String, topK: Int = 20): List<SearchResult> {
        return if (query.isBlank()) {
            emptyList()
        } else {
            repository.searchByText(query, topK)
        }
    }
    
    suspend fun searchByImage(imageUri: String, topK: Int = 20): List<SearchResult> {
        return repository.searchByImage(imageUri, topK)
    }
}

/**
 * 查找重复图片 UseCase
 */
class FindDuplicatesUseCase @Inject constructor(
    private val repository: PhotoSearchRepository
) {
    suspend operator fun invoke(threshold: Float = 0.95f): Map<String, List<SearchResult>> {
        return repository.findDuplicates(threshold)
    }
}

/**
 * 获取图片列表 UseCase
 */
class GetPhotosUseCase @Inject constructor(
    private val repository: PhotoSearchRepository
) {
    operator fun invoke(): Flow<List<ImageFeature>> {
        return repository.getAllIndexedImages()
    }
    
    suspend fun getIndexedCount(): Int = repository.getIndexedCount()
    suspend fun getTotalCount(): Int = repository.getTotalCount()
}
