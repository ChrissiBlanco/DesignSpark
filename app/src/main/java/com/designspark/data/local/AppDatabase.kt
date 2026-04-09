package com.designspark.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.designspark.data.local.dao.AnnotationDao
import com.designspark.data.local.dao.GeneratedInsightDao
import com.designspark.data.local.dao.ProjectDao
import com.designspark.data.local.entity.AnnotationEntity
import com.designspark.data.local.entity.GeneratedInsightEntity
import com.designspark.data.local.entity.ProjectEntity

@Database(
    entities = [ProjectEntity::class, GeneratedInsightEntity::class, AnnotationEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun generatedInsightDao(): GeneratedInsightDao
    abstract fun annotationDao(): AnnotationDao
}
