package com.photosearch.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room 数据库
 */
@Database(
    entities = [ImageFeatureEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PhotoSearchDatabase : RoomDatabase() {
    
    abstract fun imageFeatureDao(): ImageFeatureDao
    
    companion object {
        @Volatile
        private var INSTANCE: PhotoSearchDatabase? = null
        
        fun getDatabase(context: Context): PhotoSearchDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PhotoSearchDatabase::class.java,
                    "photo_search_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
