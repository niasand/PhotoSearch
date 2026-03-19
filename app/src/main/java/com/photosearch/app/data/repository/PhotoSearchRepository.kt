package com.photosearch.app.data.repository

import com.photosearch.app.data.local.ImageFeatureDao
import com.photosearch.app.data.local.ImageFeatureEntity
import com.photosearch.app.data.model.ImageFeature
import com.photosearch.app.data.model.SearchResult
import com.photosearch.app.data.model.WeClipModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 图片搜索仓库
 */
@Singleton
class PhotoSearchRepository @Inject constructor(
    private val imageFeatureDao: ImageFeatureDao,
    private val weClipModel: WeClipModel
) {
    
    /**
     * 获取所有已索引的图片
     */
    fun getAllIndexedImages(): Flow<List<ImageFeature>> {
        return imageFeatureDao.getAllIndexedImages().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    /**
     * 文搜图
     */
    suspend fun searchByText(query: String, topK: Int = 20): List<SearchResult> {
        val queryFeature = weClipModel.encodeText(query)
        val allFeatures = imageFeatureDao.getAllFeaturesSync()
        
        return allFeatures
            .filter { it.feature != null }
            .map { entity ->
                val similarity = weClipModel.cosineSimilarity(queryFeature, entity.feature!!)
                SearchResult(
                    imageFeature = entity.toDomainModel(),
                    similarity = similarity,
                    rank = 0
                )
            }
            .filter { it.similarity > 0.2f }  // 相似度阈值
            .sortedByDescending { it.similarity }
            .take(topK)
            .mapIndexed { index, result -> result.copy(rank = index + 1) }
    }
    
    /**
     * 图搜图
     */
    suspend fun searchByImage(imageUri: String, topK: Int = 20): List<SearchResult> {
        val targetImage = imageFeatureDao.getImageByUri(imageUri) ?: return emptyList()
        val targetFeature = targetImage.feature ?: return emptyList()
        
        val allFeatures = imageFeatureDao.getAllFeaturesSync()
        
        return allFeatures
            .filter { it.uri != imageUri && it.feature != null }
            .map { entity ->
                val similarity = weClipModel.cosineSimilarity(targetFeature, entity.feature!!)
                SearchResult(
                    imageFeature = entity.toDomainModel(),
                    similarity = similarity,
                    rank = 0
                )
            }
            .filter { it.similarity > 0.85f }  // 图搜图阈值更高
            .sortedByDescending { it.similarity }
            .take(topK)
            .mapIndexed { index, result -> result.copy(rank = index + 1) }
    }
    
    /**
     * 查找重复/相似图片
     */
    suspend fun findDuplicates(threshold: Float = 0.95f): Map<String, List<SearchResult>> {
        val allFeatures = imageFeatureDao.getAllFeaturesSync()
        val duplicates = mutableMapOf<String, MutableList<SearchResult>>()
        val processed = mutableSetOf<String>()
        
        for (image in allFeatures) {
            if (image.uri in processed || image.feature == null) continue
            
            val similar = allFeatures
                .filter { 
                    it.uri != image.uri 
&& it.feature != null 
&& it.uri !in processed 
                }
                .map { entity ->
                    val similarity = weClipModel.cosineSimilarity(image.feature, entity.feature!!)
                    SearchResult(
                        imageFeature = entity.toDomainModel(),
                        similarity = similarity,
                        rank = 0
                    )
                }
                .filter { it.similarity >= threshold }
                .sortedByDescending { it.similarity }
            
            if (similar.isNotEmpty()) {
                duplicates[image.uri] = similar.toMutableList()
                processed.add(image.uri)
                similar.forEach { processed.add(it.imageFeature.uri) }
            }
        }
        
        return duplicates
    }
    
    /**
     * 保存图片特征
     */
    suspend fun saveImageFeature(imageFeature: ImageFeature) {
        imageFeatureDao.insertImageFeature(imageFeature.toEntity())
    }
    
    /**
     * 批量保存
     */
    suspend fun saveImageFeatures(imageFeatures: List<ImageFeature>) {
        imageFeatureDao.insertImageFeatures(imageFeatures.map { it.toEntity() })
    }
    
    /**
     * 更新特征向量
     */
    suspend fun updateFeature(uri: String, feature: FloatArray) {
        imageFeatureDao.updateFeature(uri, feature)
    }
    
    /**
     * 获取已索引数量
     */
    suspend fun getIndexedCount(): Int = imageFeatureDao.getIndexedCount()
    
    /**
     * 获取总数
     */
    suspend fun getTotalCount(): Int = imageFeatureDao.getTotalCount()
    
    /**
     * 删除图片
     */
    suspend fun deleteImage(uri: String) {
        imageFeatureDao.deleteByUri(uri)
    }
    
    // 转换方法
    private fun ImageFeatureEntity.toDomainModel(): ImageFeature {
        return ImageFeature(
            id = id,
            uri = uri,
            path = path,
            feature = feature ?: FloatArray(512),
            width = width,
            height = height,
            dateTaken = dateTaken,
            fileSize = fileSize,
            isIndexed = isIndexed
        )
    }
    
    private fun ImageFeature.toEntity(): ImageFeatureEntity {
        return ImageFeatureEntity(
            id = id,
            uri = uri,
            path = path,
            feature = feature,
            width = width,
            height = height,
            dateTaken = dateTaken,
            fileSize = fileSize,
            isIndexed = isIndexed
        )
    }
}
