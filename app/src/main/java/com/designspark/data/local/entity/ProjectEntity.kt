package com.designspark.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val title: String,
    @ColumnInfo(name = "user_group") val userGroup: String,
    val context: String,
    @ColumnInfo(name = "stage") val stage: String,       // ProjectStage.name
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "status") val status: String,     // ProjectStatus.name
    @ColumnInfo(name = "is_synced") val isSynced: Boolean
)
