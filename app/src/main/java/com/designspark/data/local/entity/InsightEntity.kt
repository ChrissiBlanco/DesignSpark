package com.designspark.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.designspark.domain.model.GeneratedInsight
import com.designspark.domain.model.InsightType
import com.designspark.domain.model.RiskLevel

@Entity(
    tableName = "insights",
    foreignKeys = [ForeignKey(
        entity = ProjectEntity::class,
        parentColumns = ["id"],
        childColumns = ["projectId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("projectId")]
)
data class InsightEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val type: String,
    val title: String,
    val content: String,
    val riskLevel: String?,
    val orderIndex: Int,
    val generatedAt: Long
) {
    fun toDomain() = GeneratedInsight(
        id = id,
        projectId = projectId,
        type = InsightType.valueOf(type),
        title = title,
        content = content,
        riskLevel = riskLevel?.let { RiskLevel.valueOf(it) },
        orderIndex = orderIndex,
        generatedAt = generatedAt
    )
}
