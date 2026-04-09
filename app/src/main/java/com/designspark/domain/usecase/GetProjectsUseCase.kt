package com.designspark.domain.usecase

import com.designspark.domain.model.Project
import com.designspark.domain.repository.ProjectRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProjectsUseCase @Inject constructor(
    private val repository: ProjectRepository
) {
    operator fun invoke(): Flow<List<Project>> = repository.getProjects()
}
