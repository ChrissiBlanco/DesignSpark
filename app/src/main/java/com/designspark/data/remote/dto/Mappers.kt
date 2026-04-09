package com.designspark.data.remote.dto

import com.designspark.domain.model.GeneratedInsight
import com.designspark.domain.model.InsightType
import com.designspark.domain.model.RiskLevel
import com.google.gson.Gson
import java.util.UUID

/**
 * Converts the top-level Anthropic JSON response into a flat list of [GeneratedInsight]
 * domain objects. Content for each insight is re-serialised to JSON so the UI layer can
 * parse the type-specific fields it needs.
 */
fun InsightResponseDto.toDomain(projectId: String): List<GeneratedInsight> {
    val now = System.currentTimeMillis()
    val gson = Gson()
    val results = mutableListOf<GeneratedInsight>()
    var index = 0

    personas.forEach { p ->
        results += GeneratedInsight(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            type = InsightType.PERSONA,
            title = p.name,
            content = gson.toJson(p),
            riskLevel = null,
            orderIndex = index++,
            generatedAt = now
        )
    }
    methodCards.forEach { m ->
        results += GeneratedInsight(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            type = InsightType.METHOD_CARD,
            title = m.method,
            content = gson.toJson(m),
            riskLevel = null,
            orderIndex = index++,
            generatedAt = now
        )
    }
    assumptionsToTest.forEach { a ->
        results += GeneratedInsight(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            type = InsightType.ASSUMPTION,
            title = a.assumption,
            content = gson.toJson(a),
            riskLevel = runCatching { RiskLevel.valueOf(a.risk) }.getOrNull(),
            orderIndex = index++,
            generatedAt = now
        )
    }
    results += GeneratedInsight(
        id = UUID.randomUUID().toString(),
        projectId = projectId,
        type = InsightType.RECRUIT_BRIEF,
        title = "Recruit Brief",
        content = gson.toJson(recruitBrief),
        riskLevel = null,
        orderIndex = index,
        generatedAt = now
    )
    return results
}
