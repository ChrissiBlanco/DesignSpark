package com.designspark.domain.model

data class ProjectWithInsights(
    val project: Project,
    val insights: List<GeneratedInsight>
)
