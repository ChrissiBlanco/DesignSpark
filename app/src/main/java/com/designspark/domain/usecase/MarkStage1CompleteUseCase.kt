package com.designspark.domain.usecase

import com.designspark.domain.repository.ProjectRepository
import javax.inject.Inject

class MarkStage1CompleteUseCase @Inject constructor(
    private val repository: ProjectRepository
) {
    suspend operator fun invoke(projectId: String) {
        repository.markStage1Complete(projectId)
    }
}
