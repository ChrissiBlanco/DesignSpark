package com.designspark.domain.repository

import com.designspark.domain.model.Annotation
import com.designspark.domain.model.Project
import com.designspark.domain.model.ProjectWithInsights
import kotlinx.coroutines.flow.Flow

interface ProjectRepository {
    suspend fun createProject(project: Project)
    suspend fun generateInsights(project: Project): Result<Unit>
    fun getProjects(): Flow<List<Project>>
    fun getProjectWithInsights(projectId: String): Flow<ProjectWithInsights>
    suspend fun saveAnnotation(annotation: Annotation)
    suspend fun deleteProject(projectId: String)
}
