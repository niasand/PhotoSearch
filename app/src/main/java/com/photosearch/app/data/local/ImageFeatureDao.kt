package com.photosearch.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * 图片特征数据访问对象
 */
@Dao
interface ImageFeatureDao {
    
    @Query("SELECT * FROM image_features WHERE isIndexed = 1 ORDER BY dateTaken DESC")
    fun getAllIndexedImages(): Flow<List<ImageFeatureEntity>>
    
    @Query("SELECT * FROM image_features WHERE uri = :uri LIMIT 1")
    suspend fun getImageByUri(uri: String): ImageFeatureEntity?
    
    @Query("SELECT COUNT(*) FROM image_features WHERE isIndexed = 1")
    suspend fun getIndexedCount(): Int
    
    @Query("SELECT COUNT(*) FROM image_features")
    suspend fun getTotalCount(): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImageFeature(imageFeature: ImageFeatureEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImageFeatures(imageFeatures: List<ImageFeatureEntity>)
    
    @Query("UPDATE image_features SET isIndexed = 1, feature = :feature WHERE uri = :uri")
    suspend fun updateFeature(uri: String, feature: FloatArray)
    
    @Query("DELETE FROM image_features WHERE uri = :uri")
    suspend fun deleteByUri(uri: String)
    
    @Query("DELETE FROM image_features")
    suspend fun deleteAll()
    
    @Query("SELECT * FROM image_features WHERE isIndexed = 1")
    suspend fun getAllFeaturesSync(): List<ImageFeatureEntity>
    
    @Query("SELECT uri FROM image_features WHERE isIndexed = 1")
    suspend fun getAllIndexedUris(): List<String>
}
