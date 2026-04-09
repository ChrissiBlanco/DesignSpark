package com.designspark.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.room.withTransaction
import com.designspark.data.local.AppDatabase
import com.designspark.data.local.dao.AnnotationDao
import com.designspark.data.local.dao.GeneratedInsightDao
import com.designspark.data.local.dao.ProjectDao
import com.designspark.data.local.entity.toDomain
import com.designspark.data.local.entity.toEntity
import com.designspark.data.remote.api.AnthropicApiService
import com.designspark.data.remote.dto.AnthropicRequestDto
import com.designspark.data.remote.dto.InsightResponseDto
import com.designspark.data.remote.dto.MessageDto
import com.designspark.data.remote.dto.toDomain
import com.designspark.domain.model.Annotation
import com.designspark.domain.model.Project
import com.designspark.domain.model.ProjectStatus
import com.designspark.domain.model.ProjectWithInsights
import com.designspark.domain.repository.ProjectRepository
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ProjectRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: AppDatabase,
    private val projectDao: ProjectDao,
    private val generatedInsightDao: GeneratedInsightDao,
    private val annotationDao: AnnotationDao,
    private val anthropicApiService: AnthropicApiService
) : ProjectRepository {

    override suspend fun createProject(project: Project) = withContext(Dispatchers.IO) {
        projectDao.insert(project.toEntity())
    }

    override suspend fun generateInsights(project: Project): Result<Unit> =
        withContext(Dispatchers.IO) {
            if (!isConnected()) {
                return@withContext Result.failure(
                    Exception("No internet connection. Connect to a network and try again.")
                )
            }
            runCatching {
                val response = anthropicApiService.generateInsights(buildRequest(project))
                val raw = response.content.first().text
                    .replace("```json", "")
                    .replace("```", "")
                    .trim()
                val dto = Gson().fromJson(raw, InsightResponseDto::class.java)
                val insights = dto.toDomain(project.id)
                db.withTransaction {
                    generatedInsightDao.deleteByProjectId(project.id)
                    generatedInsightDao.insertAll(insights.map { it.toEntity() })
                    projectDao.update(
                        project.copy(
                            status = ProjectStatus.GENERATED,
                            updatedAt = System.currentTimeMillis(),
                            isSynced = true
                        ).toEntity()
                    )
                }
            }
        }

    override fun getProjects(): Flow<List<Project>> =
        projectDao.getAll().map { list -> list.map { it.toDomain() } }

    override fun getProjectWithInsights(projectId: String): Flow<ProjectWithInsights> =
        combine(
            projectDao.getById(projectId).filterNotNull(),
            generatedInsightDao.getByProjectId(projectId)
        ) { projectEntity, insightEntities ->
            ProjectWithInsights(
                project = projectEntity.toDomain(),
                insights = insightEntities.map { it.toDomain() }
            )
        }

    override suspend fun saveAnnotation(annotation: Annotation) = withContext(Dispatchers.IO) {
        annotationDao.insert(annotation.toEntity())
    }

    override suspend fun deleteProject(projectId: String) = withContext(Dispatchers.IO) {
        projectDao.delete(projectId)
    }

    private fun isConnected(): Boolean {
        val cm = context.getSystemService(ConnectivityManager::class.java) ?: return false
        val network = cm.activeNetwork ?: return false
        return cm.getNetworkCapabilities(network)
            ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    private fun buildRequest(project: Project) = AnthropicRequestDto(
        model = "claude-sonnet-4-20250514",
        maxTokens = 1500,
        system = SYSTEM_PROMPT,
        messages = listOf(
            MessageDto(
                role = "user",
                content = "Project title: ${project.title}\n" +
                        "User group: ${project.userGroup}\n" +
                        "Context: ${project.context}\n" +
                        "Stage: ${project.stage.name}"
            )
        )
    )

    companion object {
        private val SYSTEM_PROMPT = """
            You are a senior HCI researcher. Given a project brief, return ONLY valid JSON with
            no preamble, no markdown, no code fences — raw JSON only.

            Use this exact shape:
            {
              "personas": [
                { "name": "", "age": 0, "role": "", "goal": "", "frustration": "" }
              ],
              "methodCards": [
                { "method": "", "whyThisFits": "", "estimatedTime": "" }
              ],
              "assumptionsToTest": [
                { "assumption": "", "risk": "HIGH|MEDIUM|LOW", "rationale": "" }
              ],
              "recruitBrief": {
                "whoToFind": "",
                "screenFor": "",
                "exclude": ""
              }
            }

            Rules:
            - Generate exactly 3 personas, 3 method cards, 4 assumptions sorted HIGH to LOW risk, and 1 recruit brief.
            - Tailor every output specifically to the user group, context, and stage provided.
            - No generic advice. Each output must only make sense for THIS project.
            - Method cards must explain WHY this method fits this specific user group and stage, not just name the method.
            - Assumptions must identify real risks in this idea, not textbook placeholders.
        """.trimIndent()
    }
}
