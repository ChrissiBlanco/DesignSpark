package com.designspark.domain.usecase

import com.designspark.domain.model.Project
import com.designspark.domain.repository.ProjectRepository
import javax.inject.Inject

class GenerateSwotUseCase @Inject constructor(
    private val repository: ProjectRepository
) {
    suspend operator fun invoke(project: Project): Result<Unit> =
        repository.generateSwot(project)
}
