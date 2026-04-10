package com.designspark.domain.usecase

import com.designspark.domain.model.Project
import com.designspark.domain.repository.ProjectRepository
import java.util.UUID
import javax.inject.Inject

class CreateProjectUseCase @Inject constructor(
    private val repository: ProjectRepository
) {
    suspend operator fun invoke(title: String, description: String): Result<Project> = runCatching {
        require(title.isNotBlank()) { "Title must not be empty" }
        require(description.isNotBlank()) { "Description must not be empty" }

        val now = System.currentTimeMillis()
        val project = Project(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            description = description.trim(),
            createdAt = now,
            updatedAt = now,
            stage1Complete = false
        )
        repository.createProject(project)
        project
    }
}
