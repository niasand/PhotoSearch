package com.photosearch.app.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

class IndexingService : Service() {

    companion object {
        const val ACTION_START = "com.photosearch.app.action.START_INDEXING"
        const val ACTION_STOP = "com.photosearch.app.action.STOP_INDEXING"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startIndexing()
            ACTION_STOP -> stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun startIndexing() {
        // TODO: 实现图片索引逻辑
        stopSelf()
    }
}
