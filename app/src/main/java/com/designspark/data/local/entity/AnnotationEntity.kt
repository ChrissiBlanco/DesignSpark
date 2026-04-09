package com.designspark.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "annotations",
    foreignKeys = [ForeignKey(
        entity = GeneratedInsightEntity::class,
        parentColumns = ["id"],
        childColumns = ["insight_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("insight_id")]
)
data class AnnotationEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "insight_id") val insightId: String,
    val note: String,
    @ColumnInfo(name = "created_at") val createdAt: Long
)
