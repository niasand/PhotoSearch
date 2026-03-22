package com.photosearch.app.data.model

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.FloatBuffer
import javax.inject.Inject
import javax.inject.Singleton

/**
 * WeCLIP V2 ONNX 模型实现
 */
@Singleton
class WeClipModelImpl @Inject constructor(
    private val context: Context
) : WeClipModel {
    
    private var ortEnvironment: OrtEnvironment? = null
    private var imageSession: OrtSession? = null
    private var textSession: OrtSession? = null
    private var isInitialized = false
    
    companion object {
        const val IMAGE_SIZE = 224
        const val FEATURE_DIM = 512
        const val IMAGE_MODEL_PATH = "models/weclip_v2_image.onnx"
        const val TEXT_MODEL_PATH = "models/weclip_v2_text.onnx"
    }
    
    /**
     * 初始化模型
     */
    suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        if (isInitialized) return@withContext true
        
        return@withContext try {
            ortEnvironment = OrtEnvironment.getEnvironment()
            
            val imageModelFile = context.assets.open(IMAGE_MODEL_PATH)
            val imageModelBytes = imageModelFile.readBytes()
            imageSession = ortEnvironment?.createSession(imageModelBytes)
            
            val textModelFile = context.assets.open(TEXT_MODEL_PATH)
            val textModelBytes = textModelFile.readBytes()
            textSession = ortEnvironment?.createSession(textModelBytes)
            
            isInitialized = true
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    override fun encodeImage(bitmap: Bitmap): FloatArray {
        require(isInitialized) { "Model not initialized" }
        
        // 预处理图片
        val processedBitmap = preprocessImage(bitmap)
        val inputTensor = bitmapToFloatBuffer(processedBitmap)
        
        val inputName = imageSession?.inputNames?.iterator()?.next()
        val inputs = mapOf(inputName to inputTensor)
        
        val results = imageSession?.run(inputs)
        val outputTensor = results?.get(0) as? OnnxTensor
        val features = outputTensor?.floatBuffer?.array() ?: FloatArray(FEATURE_DIM)
        
        // 归一化特征向量
        return normalize(features)
    }
    
    override fun encodeText(text: String): FloatArray {
        require(isInitialized) { "Model not initialized" }
        
        // 文本预处理（简单实现，实际需用 tokenizer）
        val tokens = tokenize(text)
        val inputTensor = OnnxTensor.createTensor(
            ortEnvironment,
            FloatBuffer.wrap(tokens),
            longArrayOf(1, tokens.size.toLong())
        )
        
        val inputName = textSession?.inputNames?.iterator()?.next()
        val inputs = mapOf(inputName to inputTensor)
        
        val results = textSession?.run(inputs)
        val outputTensor = results?.get(0) as? OnnxTensor
        val features = outputTensor?.floatBuffer?.array() ?: FloatArray(FEATURE_DIM)
        
        return normalize(features)
    }
    
    override fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
        var dot = 0.0f
        var normA = 0.0f
        var normB = 0.0f
        
        for (i in a.indices) {
            dot += a[i] * b[i]
            normA += a[i] * a[i]
            normB += b[i] * b[i]
        }
        
        return if (normA > 0 && normB > 0) {
            dot / (kotlin.math.sqrt(normA) * kotlin.math.sqrt(normB))
        } else {
            0.0f
        }
    }
    
    override fun close() {
        imageSession?.close()
        textSession?.close()
        ortEnvironment?.close()
        isInitialized = false
    }
    
    /**
     * 预处理图片：调整大小、归一化
     */
    private fun preprocessImage(bitmap: Bitmap): Bitmap {
        // 调整大小为 224x224
        val matrix = Matrix()
        val scale = IMAGE_SIZE.toFloat() / bitmap.width.coerceAtMost(bitmap.height)
        matrix.postScale(scale, scale)
        
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
    
    /**
     * 将 Bitmap 转换为 FloatBuffer (CHW格式)
     */
    private fun bitmapToFloatBuffer(bitmap: Bitmap): OnnxTensor {
        val width = bitmap.width
        val height = bitmap.height
        val channels = 3
        
        val floatBuffer = FloatBuffer.allocate(channels * height * width)
        
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        // ImageNet 归一化参数
        val mean = floatArrayOf(0.48145466f, 0.4578275f, 0.40821073f)
        val std = floatArrayOf(0.26862954f, 0.26130258f, 0.27577711f)
        
        // 转换为 CHW 格式并归一化
        for (c in 0 until channels) {
            for (h in 0 until height) {
                for (w in 0 until width) {
                    val pixel = pixels[h * width + w]
                    val value = when (c) {
                        0 -> (pixel shr 16 and 0xFF) / 255.0f  // R
                        1 -> (pixel shr 8 and 0xFF) / 255.0f   // G
                        2 -> (pixel and 0xFF) / 255.0f         // B
                        else -> 0.0f
                    }
                    floatBuffer.put((value - mean[c]) / std[c])
                }
            }
        }
        
        floatBuffer.rewind()
        
        return OnnxTensor.createTensor(
            ortEnvironment,
            floatBuffer,
            longArrayOf(1, channels.toLong(), height.toLong(), width.toLong())
        )
    }
    
    /**
     * 简单文本分词（占位实现）
     */
    private fun tokenize(text: String): FloatArray {
        // 实际实现需要使用 WeCLIP 的 tokenizer
        // 这里返回一个占位符向量
        return FloatArray(77) { 0.0f }
    }
    
    /**
     * 归一化特征向量
     */
    private fun normalize(vector: FloatArray): FloatArray {
        var norm = 0.0f
        for (v in vector) {
            norm += v * v
        }
        norm = kotlin.math.sqrt(norm)
        
        return if (norm > 0) {
            vector.map { it / norm }.toFloatArray()
        } else {
            vector
        }
    }
}
