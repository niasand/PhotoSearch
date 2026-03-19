package com.photosearch.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.MediaStore
import androidx.core.app.NotificationCompat
import com.photosearch.app.MainActivity
import com.photosearch.app.R
import com.photosearch.app.data.model.ImageFeature
import com.photosearch.app.data.model.WeClipModel
import com.photosearch.app.data.repository.PhotoSearchRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 图片索引进度状态
 */
sealed class IndexingState {
    object Idle : IndexingState()
    data class Running(val current: Int, val total: Int, val currentUri: String) : IndexingState()
    object Completed : IndexingState()
    data class Error(val message: String) : IndexingState()
}

/**
 * 后台图片索引服务
 */
@AndroidEntryPoint
class IndexingService : Service() {

    @Inject
    lateinit var repository: PhotoSearchRepository

    @Inject
    lateinit var weClipModel: WeClipModel

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val notificationManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    private val _indexingState = MutableStateFlow<IndexingState>(IndexingState.Idle)
    val indexingState: StateFlow<IndexingState> = _indexingState.asStateFlow()

    companion object {
        const val CHANNEL_ID = "indexing_channel"
        const val NOTIFICATION_ID = 1
        const val ACTION_START = "action_start"
        const val ACTION_STOP = "action_stop"

        // 单例状态（用于ViewModel观察）
        private val _globalState = MutableStateFlow<IndexingState>(IndexingState.Idle)
        val globalState: StateFlow<IndexingState> = _globalState.asStateFlow()
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startIndexing()
            ACTION_STOP -> stopIndexing()
        }
        return START_STICKY
    }

    private fun startIndexing() {
        if (_indexingState.value is IndexingState.Running) return

        val notification = createNotification("正在扫描相册...", 0, 0)
        startForeground(NOTIFICATION_ID, notification)

        serviceScope.launch {
            _indexingState.value = IndexingState.Running(0, 0, "")
            _globalState.value = IndexingState.Running(0, 0, "")

            try {
                val photos = scanGallery()
                val newPhotos = filterNewPhotos(photos)

                if (newPhotos.isEmpty()) {
                    _indexingState.value = IndexingState.Completed
                    _globalState.value = IndexingState.Completed
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                    return@launch
                }

                val total = newPhotos.size
                var current = 0

                // 先批量保存图片元数据
                repository.saveImageFeatures(newPhotos)

                // 逐个提取特征
                for (photo in newPhotos) {
                    current++
                    _indexingState.value = IndexingState.Running(current, total, photo.uri)
                    _globalState.value = IndexingState.Running(current, total, photo.uri)

                    updateNotification(current, total, photo.uri)

                    try {
                        val bitmap = loadBitmap(photo.uri)
                        if (bitmap != null) {
                            val feature = weClipModel.encodeImage(bitmap)
                            repository.updateFeature(photo.uri, feature)
                            bitmap.recycle()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                _indexingState.value = IndexingState.Completed
                _globalState.value = IndexingState.Completed

            } catch (e: Exception) {
                _indexingState.value = IndexingState.Error(e.message ?: "Unknown error")
                _globalState.value = IndexingState.Error(e.message ?: "Unknown error")
            } finally {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    private fun stopIndexing() {
        serviceScope.cancel()
        _indexingState.value = IndexingState.Idle
        _globalState.value = IndexingState.Idle
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    /**
     * 扫描本地相册
     */
    private suspend fun scanGallery(): List<ImageFeature> = withContext(Dispatchers.IO) {
        val photos = mutableListOf<ImageFeature>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.SIZE
        )

        val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"

        contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
            val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val uri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                ).toString()
                val path = cursor.getString(pathColumn)
                val width = cursor.getInt(widthColumn)
                val height = cursor.getInt(heightColumn)
                val date = cursor.getLong(dateColumn)
                val size = cursor.getLong(sizeColumn)

                photos.add(ImageFeature(
                    uri = uri,
                    path = path,
                    feature = FloatArray(512),
                    width = width,
                    height = height,
                    dateTaken = date,
                    fileSize = size,
                    isIndexed = false
                ))
            }
        }

        photos
    }

    /**
     * 过滤已索引的图片
     */
    private suspend fun filterNewPhotos(photos: List<ImageFeature>): List<ImageFeature> {
        val indexedUris = repository.getAllIndexedUris()
        return photos.filter { it.uri !in indexedUris }
    }

    /**
     * 加载 Bitmap
     */
    private fun loadBitmap(uriString: String): Bitmap? {
        return try {
            val uri = Uri.parse(uriString)
            contentResolver.openInputStream(uri)?.use { stream ->
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = false
                    inSampleSize = 4 // 缩小尺寸以节省内存
                }
                BitmapFactory.decodeStream(stream, null, options)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 创建通知渠道
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "相册索引",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "显示图片索引进度"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * 创建通知
     */
    private fun createNotification(content: String, current: Int, total: Int): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("相册搜索")
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)

        if (total > 0) {
            builder.setProgress(total, current, false)
        }

        return builder.build()
    }

    /**
     * 更新通知
     */
    private fun updateNotification(current: Int, total: Int, uri: String) {
        val notification = createNotification(
            "正在处理 $current/$total 张图片",
            current,
            total
        )
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}