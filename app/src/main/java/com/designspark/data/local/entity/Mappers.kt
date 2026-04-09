package com.designspark.data.local.entity

import com.designspark.domain.model.Annotation
import com.designspark.domain.model.GeneratedInsight
import com.designspark.domain.model.InsightType
import com.designspark.domain.model.Project
import com.designspark.domain.model.ProjectStage
import com.designspark.domain.model.ProjectStatus
import com.designspark.domain.model.RiskLevel

// ── ProjectEntity ↔ Project ──────────────────────────────────────────────────

fun ProjectEntity.toDomain(): Project = Project(
    id = id,
    title = title,
    userGroup = userGroup,
    context = context,
    stage = ProjectStage.valueOf(stage),
    createdAt = createdAt,
    updatedAt = updatedAt,
    status = ProjectStatus.valueOf(status),
    isSynced = isSynced
)

fun Project.toEntity(): ProjectEntity = ProjectEntity(
    id = id,
    title = title,
    userGroup = userGroup,
    context = context,
    stage = stage.name,
    createdAt = createdAt,
    updatedAt = updatedAt,
    status = status.name,
    isSynced = isSynced
)

// ── GeneratedInsightEntity ↔ GeneratedInsight ────────────────────────────────

fun GeneratedInsightEntity.toDomain(): GeneratedInsight = GeneratedInsight(
    id = id,
    projectId = projectId,
    type = InsightType.valueOf(type),
    title = title,
    content = content,
    riskLevel = riskLevel?.let { RiskLevel.valueOf(it) },
    orderIndex = orderIndex,
    generatedAt = generatedAt
)

fun GeneratedInsight.toEntity(): GeneratedInsightEntity = GeneratedInsightEntity(
    id = id,
    projectId = projectId,
    type = type.name,
    title = title,
    content = content,
    riskLevel = riskLevel?.name,
    orderIndex = orderIndex,
    generatedAt = generatedAt
)

// ── AnnotationEntity ↔ Annotation ────────────────────────────────────────────

fun AnnotationEntity.toDomain(): Annotation = Annotation(
    id = id,
    insightId = insightId,
    note = note,
    createdAt = createdAt
)

fun Annotation.toEntity(): AnnotationEntity = AnnotationEntity(
    id = id,
    insightId = insightId,
    note = note,
    createdAt = createdAt
)
