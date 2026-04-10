package com.designspark.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "generated_insights",
    foreignKeys = [ForeignKey(
        entity = ProjectEntity::class,
        parentColumns = ["id"],
        childColumns = ["project_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("project_id")]
)
data class GeneratedInsightEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "project_id") val projectId: String,
    @ColumnInfo(name = "type") val type: String,
    val title: String,
    val content: String,
    @ColumnInfo(name = "quadrant") val quadrant: String?,
    @ColumnInfo(name = "order_index") val orderIndex: Int,
    @ColumnInfo(name = "generated_at") val generatedAt: Long
)
