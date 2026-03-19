package com.photosearch.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters

/**
 * 图片特征数据库实体
 */
@Entity(tableName = "image_features")
@TypeConverters(FeatureConverter::class)
data class ImageFeatureEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val uri: String,
    val path: String?,
    val feature: FloatArray? = null,
    val width: Int,
    val height: Int,
    val dateTaken: Long,
    val fileSize: Long,
    val isIndexed: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ImageFeatureEntity
        return id == other.id && uri == other.uri
    }
    
    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + uri.hashCode()
        return result
    }
}

/**
 * FloatArray 类型转换器
 */
class FeatureConverter {
    @TypeConverter
    fun fromFloatArray(features: FloatArray?): String? {
        return features?.joinToString(",")
    }
    
    @TypeConverter
    fun toFloatArray(featuresString: String?): FloatArray? {
        return featuresString?.split(",")?.map { it.toFloat() }?.toFloatArray()
    }
}
