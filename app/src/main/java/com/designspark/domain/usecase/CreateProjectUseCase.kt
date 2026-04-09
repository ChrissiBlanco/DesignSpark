package com.designspark.domain.usecase

import com.designspark.domain.model.Project
import com.designspark.domain.model.ProjectStage
import com.designspark.domain.model.ProjectStatus
import com.designspark.domain.repository.ProjectRepository
import java.util.UUID
import javax.inject.Inject

class CreateProjectUseCase @Inject constructor(
    private val repository: ProjectRepository
) {
    suspend operator fun invoke(
        title: String,
        userGroup: String,
        context: String,
        stage: ProjectStage
    ): Result<Project> = runCatching {
        require(title.isNotBlank()) { "Title must not be empty" }
        require(userGroup.isNotBlank()) { "User group must not be empty" }
        require(context.isNotBlank()) { "Context must not be empty" }

        val now = System.currentTimeMillis()
        val project = Project(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            userGroup = userGroup.trim(),
            context = context.trim(),
            stage = stage,
            createdAt = now,
            updatedAt = now,
            status = ProjectStatus.DRAFT,
            isSynced = false
        )
        repository.createProject(project)
        project
    }
}
