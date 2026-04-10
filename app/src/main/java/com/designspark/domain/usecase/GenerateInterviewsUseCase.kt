package com.designspark.domain.usecase

import com.designspark.domain.model.Project
import com.designspark.domain.repository.ProjectRepository
import javax.inject.Inject

class GenerateInterviewsUseCase @Inject constructor(
    private val repository: ProjectRepository
) {
    suspend operator fun invoke(project: Project): Result<Unit> =
        repository.generateInterviews(project)
}
