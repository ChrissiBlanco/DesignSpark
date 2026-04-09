package com.designspark.domain.model

data class Project(
    val id: String,
    val title: String,
    val userGroup: String,
    val context: String,
    val stage: ProjectStage,
    val createdAt: Long,
    val updatedAt: Long,
    val status: ProjectStatus,
    val isSynced: Boolean
)

enum class ProjectStage { NOTHING, ROUGH_IDEA, PROTOTYPE }
enum class ProjectStatus { DRAFT, GENERATED, ANNOTATED }
