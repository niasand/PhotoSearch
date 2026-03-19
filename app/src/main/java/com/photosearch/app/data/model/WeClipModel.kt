package com.photosearch.app.data.model

import android.graphics.Bitmap

/**
 * WeCLIP V2 模型推理接口
 */
interface WeClipModel {
    /**
     * 提取图片特征向量
     * @param bitmap 输入图片（建议 224x224）
     * @return 512维特征向量
     */
    fun encodeImage(bitmap: Bitmap): FloatArray
    
    /**
     * 提取文本特征向量
     * @param text 输入文本
     * @return 512维特征向量
     */
    fun encodeText(text: String): FloatArray
    
    /**
     * 计算余弦相似度
     */
    fun cosineSimilarity(a: FloatArray, b: FloatArray): Float
    
    /**
     * 释放模型资源
     */
    fun close()
}

/**
 * 图片特征数据类
 */
data class ImageFeature(
    val id: Long = 0,
    val uri: String,
    val path: String?,
    val feature: FloatArray,
    val width: Int,
    val height: Int,
    val dateTaken: Long,
    val fileSize: Long,
    val isIndexed: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ImageFeature
        return id == other.id && uri == other.uri
    }
    
    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + uri.hashCode()
        return result
    }
}

/**
 * 搜索结果
 */
data class SearchResult(
    val imageFeature: ImageFeature,
    val similarity: Float,
    val rank: Int
)

/**
 * 搜索类型
 */
enum class SearchType {
    TEXT_TO_IMAGE,   // 文搜图
    IMAGE_TO_IMAGE,  // 图搜图
    DUPLICATE        // 重复图片
}

/**
 * 索引状态
 */
sealed class IndexStatus {
    object Idle : IndexStatus()
    data class Running(val current: Int, val total: Int) : IndexStatus()
    object Completed : IndexStatus()
    data class Error(val message: String) : IndexStatus()
}
