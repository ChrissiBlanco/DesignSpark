package com.designspark.domain.repository

import com.designspark.domain.model.Annotation
import com.designspark.domain.model.Project
import com.designspark.domain.model.ProjectWithInsights
import kotlinx.coroutines.flow.Flow

interface ProjectRepository {
    suspend fun createProject(project: Project)
    fun getProjects(): Flow<List<Project>>
    fun getProjectWithInsights(projectId: String): Flow<ProjectWithInsights>
    suspend fun generateCompetitors(project: Project): Result<Unit>
    suspend fun generateInterviews(project: Project): Result<Unit>
    suspend fun generateSwot(project: Project): Result<Unit>
    suspend fun markStage1Complete(projectId: String)
    suspend fun saveAnnotation(annotation: Annotation)
    suspend fun deleteProject(projectId: String)
}
