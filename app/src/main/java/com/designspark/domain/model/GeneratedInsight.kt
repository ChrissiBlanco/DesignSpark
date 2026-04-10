package com.designspark.domain.model

data class GeneratedInsight(
    val id: String,
    val projectId: String,
    val type: InsightType,
    val title: String,
    val content: String,
    val quadrant: SwotQuadrant?,
    val orderIndex: Int,
    val generatedAt: Long
)

enum class InsightType { COMPETITOR, USER_INTERVIEW, SWOT_ITEM }

enum class SwotQuadrant { STRENGTH, WEAKNESS, OPPORTUNITY, THREAT }
