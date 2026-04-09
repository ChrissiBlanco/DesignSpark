package com.designspark.domain.model

data class GeneratedInsight(
    val id: String,
    val projectId: String,
    val type: InsightType,
    val title: String,
    val content: String,
    val riskLevel: RiskLevel?,
    val orderIndex: Int,
    val generatedAt: Long
)

enum class InsightType { PERSONA, METHOD_CARD, ASSUMPTION, RECRUIT_BRIEF }
enum class RiskLevel { HIGH, MEDIUM, LOW }
