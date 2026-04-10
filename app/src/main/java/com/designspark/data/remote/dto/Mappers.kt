package com.designspark.data.remote.dto

import com.designspark.domain.model.GeneratedInsight
import com.designspark.domain.model.InsightType
import com.designspark.domain.model.SwotQuadrant
import com.google.gson.Gson
import java.util.UUID

fun CompetitorScanResponseDto.toGeneratedInsights(projectId: String): List<GeneratedInsight> {
    val gson = Gson()
    val now = System.currentTimeMillis()
    val out = mutableListOf<GeneratedInsight>()
    var index = 0

    competitors.forEach { c ->
        val payload = mapOf("description" to c.description, "weakness" to c.weakness)
        out += GeneratedInsight(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            type = InsightType.COMPETITOR,
            title = c.name,
            content = gson.toJson(payload),
            quadrant = null,
            orderIndex = index++,
            generatedAt = now
        )
    }
    out += GeneratedInsight(
        id = UUID.randomUUID().toString(),
        projectId = projectId,
        type = InsightType.COMPETITOR,
        title = "Market gap",
        content = marketGap,
        quadrant = null,
        orderIndex = index++,
        generatedAt = now
    )
    painPoints.forEach { p ->
        out += GeneratedInsight(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            type = InsightType.COMPETITOR,
            title = p.painPoint,
            content = p.rationale,
            quadrant = null,
            orderIndex = index++,
            generatedAt = now
        )
    }
    return out
}

fun UserInterviewsResponseDto.toGeneratedInsights(projectId: String): List<GeneratedInsight> {
    val gson = Gson()
    val now = System.currentTimeMillis()
    return interviews.mapIndexed { idx, i ->
        val payload = mapOf("role" to i.role, "reaction" to i.reaction, "quote" to i.quote)
        GeneratedInsight(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            type = InsightType.USER_INTERVIEW,
            title = "${i.name} — ${i.sentiment}",
            content = gson.toJson(payload),
            quadrant = null,
            orderIndex = idx,
            generatedAt = now
        )
    }
}

fun SwotResponseDto.toGeneratedInsights(projectId: String): List<GeneratedInsight> {
    val now = System.currentTimeMillis()
    val out = mutableListOf<GeneratedInsight>()
    var order = 0

    fun addAll(items: List<String>, quadrant: SwotQuadrant) {
        items.forEach { text ->
            out += GeneratedInsight(
                id = UUID.randomUUID().toString(),
                projectId = projectId,
                type = InsightType.SWOT_ITEM,
                title = text,
                content = text,
                quadrant = quadrant,
                orderIndex = order++,
                generatedAt = now
            )
        }
    }

    addAll(swot.strengths, SwotQuadrant.STRENGTH)
    addAll(swot.weaknesses, SwotQuadrant.WEAKNESS)
    addAll(swot.opportunities, SwotQuadrant.OPPORTUNITY)
    addAll(swot.threats, SwotQuadrant.THREAT)
    return out
}
