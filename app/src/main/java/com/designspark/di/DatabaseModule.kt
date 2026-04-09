package com.designspark.di

import android.content.Context
import androidx.room.Room
import com.designspark.data.local.AppDatabase
import com.designspark.data.local.dao.AnnotationDao
import com.designspark.data.local.dao.GeneratedInsightDao
import com.designspark.data.local.dao.ProjectDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "designspark.db").build()

    @Provides
    fun provideProjectDao(db: AppDatabase): ProjectDao = db.projectDao()

    @Provides
    fun provideGeneratedInsightDao(db: AppDatabase): GeneratedInsightDao = db.generatedInsightDao()

    @Provides
    fun provideAnnotationDao(db: AppDatabase): AnnotationDao = db.annotationDao()
}
