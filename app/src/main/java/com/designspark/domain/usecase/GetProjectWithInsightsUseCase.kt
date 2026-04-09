package com.designspark.domain.usecase

import com.designspark.domain.model.ProjectWithInsights
import com.designspark.domain.repository.ProjectRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProjectWithInsightsUseCase @Inject constructor(
    private val repository: ProjectRepository
) {
    operator fun invoke(projectId: String): Flow<ProjectWithInsights> =
        repository.getProjectWithInsights(projectId)
}
