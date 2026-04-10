package com.designspark.domain.model

data class ProjectWithInsights(
    val project: Project,
    val competitors: List<GeneratedInsight>,
    val interviews: List<GeneratedInsight>,
    val swotItems: List<GeneratedInsight>
)
