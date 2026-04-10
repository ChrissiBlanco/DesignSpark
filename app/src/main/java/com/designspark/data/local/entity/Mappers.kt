package com.designspark.data.local.entity

import com.designspark.domain.model.Annotation
import com.designspark.domain.model.GeneratedInsight
import com.designspark.domain.model.InsightType
import com.designspark.domain.model.Project
import com.designspark.domain.model.SwotQuadrant

// ── ProjectEntity ↔ Project ──────────────────────────────────────────────────

fun ProjectEntity.toDomain(): Project = Project(
    id = id,
    title = title,
    description = description,
    createdAt = createdAt,
    updatedAt = updatedAt,
    stage1Complete = stage1Complete
)

fun Project.toEntity(): ProjectEntity = ProjectEntity(
    id = id,
    title = title,
    description = description,
    createdAt = createdAt,
    updatedAt = updatedAt,
    stage1Complete = stage1Complete
)

// ── GeneratedInsightEntity ↔ GeneratedInsight ────────────────────────────────

fun GeneratedInsightEntity.toDomain(): GeneratedInsight = GeneratedInsight(
    id = id,
    projectId = projectId,
    type = InsightType.valueOf(type),
    title = title,
    content = content,
    quadrant = quadrant?.let { SwotQuadrant.valueOf(it) },
    orderIndex = orderIndex,
    generatedAt = generatedAt
)

fun GeneratedInsight.toEntity(): GeneratedInsightEntity = GeneratedInsightEntity(
    id = id,
    projectId = projectId,
    type = type.name,
    title = title,
    content = content,
    quadrant = quadrant?.name,
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
