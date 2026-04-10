package com.designspark.domain.model

data class Project(
    val id: String,
    val title: String,
    val description: String,
    val createdAt: Long,
    val updatedAt: Long,
    val stage1Complete: Boolean = false
)
