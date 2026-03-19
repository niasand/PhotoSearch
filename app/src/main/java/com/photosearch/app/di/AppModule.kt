package com.photosearch.app.di

import android.content.Context
import com.photosearch.app.data.local.ImageFeatureDao
import com.photosearch.app.data.local.PhotoSearchDatabase
import com.photosearch.app.data.model.WeClipModel
import com.photosearch.app.data.model.WeClipModelImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PhotoSearchDatabase {
        return PhotoSearchDatabase.getDatabase(context)
    }
    
    @Provides
    @Singleton
    fun provideImageFeatureDao(database: PhotoSearchDatabase): ImageFeatureDao {
        return database.imageFeatureDao()
    }
    
    @Provides
    @Singleton
    fun provideWeClipModel(
        @ApplicationContext context: Context
    ): WeClipModel {
        return WeClipModelImpl(context)
    }
}
