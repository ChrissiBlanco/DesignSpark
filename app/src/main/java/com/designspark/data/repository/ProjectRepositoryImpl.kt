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
import com.designspark.data.remote.api.OpenAiApiService
import com.designspark.data.remote.dto.CompetitorScanResponseDto
import com.designspark.data.remote.dto.OpenAiChatCompletionRequestDto
import com.designspark.data.remote.dto.OpenAiChatCompletionResponseDto
import com.designspark.data.remote.dto.OpenAiChatMessageDto
import com.designspark.data.remote.dto.SwotResponseDto
import com.designspark.data.remote.dto.UserInterviewsResponseDto
import com.designspark.data.remote.dto.toGeneratedInsights
import com.designspark.domain.model.Annotation
import com.designspark.domain.model.InsightType
import com.designspark.domain.model.Project
import com.designspark.domain.model.ProjectWithInsights
import com.designspark.domain.repository.ProjectRepository
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import javax.inject.Inject

class ProjectRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: AppDatabase,
    private val projectDao: ProjectDao,
    private val generatedInsightDao: GeneratedInsightDao,
    private val annotationDao: AnnotationDao,
    private val openAiApiService: OpenAiApiService,
    private val gson: Gson
) : ProjectRepository {

    override suspend fun createProject(project: Project) = withContext(Dispatchers.IO) {
        projectDao.insert(project.toEntity())
    }

    override suspend fun generateCompetitors(project: Project): Result<Unit> =
        withContext(Dispatchers.IO) {
            if (!isConnected()) {
                return@withContext Result.failure(
                    Exception("No internet connection. Connect to a network and try again.")
                )
            }
            runCatching {
                val raw = requestJson(
                    buildRequest(
                        systemContent = COMPETITORS_SYSTEM_PROMPT,
                        userContent = productUserMessage(project)
                    )
                )
                val dto = gson.fromJson(raw, CompetitorScanResponseDto::class.java)
                val insights = dto.toGeneratedInsights(project.id)
                val now = System.currentTimeMillis()
                db.withTransaction {
                    generatedInsightDao.deleteByProjectIdAndType(project.id, InsightType.COMPETITOR.name)
                    generatedInsightDao.insertAll(insights.map { it.toEntity() })
                    projectDao.update(
                        project.copy(updatedAt = now).toEntity()
                    )
                }
            }.foldWithHttp()
        }

    override suspend fun generateInterviews(project: Project): Result<Unit> =
        withContext(Dispatchers.IO) {
            if (!isConnected()) {
                return@withContext Result.failure(
                    Exception("No internet connection. Connect to a network and try again.")
                )
            }
            runCatching {
                val raw = requestJson(
                    buildRequest(
                        systemContent = INTERVIEWS_SYSTEM_PROMPT,
                        userContent = productUserMessage(project)
                    )
                )
                val dto = gson.fromJson(raw, UserInterviewsResponseDto::class.java)
                val insights = dto.toGeneratedInsights(project.id)
                val now = System.currentTimeMillis()
                db.withTransaction {
                    generatedInsightDao.deleteByProjectIdAndType(
                        project.id,
                        InsightType.USER_INTERVIEW.name
                    )
                    generatedInsightDao.insertAll(insights.map { it.toEntity() })
                    projectDao.update(project.copy(updatedAt = now).toEntity())
                }
            }.foldWithHttp()
        }

    override suspend fun generateSwot(project: Project): Result<Unit> =
        withContext(Dispatchers.IO) {
            if (!isConnected()) {
                return@withContext Result.failure(
                    Exception("No internet connection. Connect to a network and try again.")
                )
            }
            val competitorRows = generatedInsightDao
                .getByProjectIdAndType(project.id, InsightType.COMPETITOR.name)
                .first()
            if (competitorRows.isEmpty()) {
                return@withContext Result.failure(
                    IllegalStateException("Run competitor scan first")
                )
            }
            val competitorContext = competitorRows.joinToString("\n\n") { entity ->
                "${entity.title}: ${entity.content}"
            }
            runCatching {
                val raw = requestJson(
                    buildRequest(
                        systemContent = SWOT_SYSTEM_PROMPT,
                        userContent = productUserMessage(project) +
                            "\nCompetitive landscape: $competitorContext"
                    )
                )
                val dto = gson.fromJson(raw, SwotResponseDto::class.java)
                val insights = dto.toGeneratedInsights(project.id)
                val now = System.currentTimeMillis()
                db.withTransaction {
                    generatedInsightDao.deleteByProjectIdAndType(
                        project.id,
                        InsightType.SWOT_ITEM.name
                    )
                    generatedInsightDao.insertAll(insights.map { it.toEntity() })
                    projectDao.update(project.copy(updatedAt = now).toEntity())
                }
            }.foldWithHttp()
        }

    override suspend fun markStage1Complete(projectId: String) = withContext(Dispatchers.IO) {
        projectDao.updateStage1Complete(projectId, true, System.currentTimeMillis())
    }

    override fun getProjects(): Flow<List<Project>> =
        projectDao.getAll().map { list ->
            list.map { it.toDomain() }
        }

    override fun getProjectWithInsights(projectId: String): Flow<ProjectWithInsights> =
        combine(
            projectDao.getById(projectId).filterNotNull(),
            generatedInsightDao.getByProjectId(projectId)
        ) { projectEntity, insightEntities ->
            val domain = insightEntities.map { it.toDomain() }
            ProjectWithInsights(
                project = projectEntity.toDomain(),
                competitors = domain.filter { it.type == InsightType.COMPETITOR },
                interviews = domain.filter { it.type == InsightType.USER_INTERVIEW },
                swotItems = domain.filter { it.type == InsightType.SWOT_ITEM }
            )
        }

    override suspend fun saveAnnotation(annotation: Annotation) = withContext(Dispatchers.IO) {
        annotationDao.insert(annotation.toEntity())
    }

    override suspend fun deleteProject(projectId: String) = withContext(Dispatchers.IO) {
        projectDao.delete(projectId)
    }

    private suspend fun requestJson(request: OpenAiChatCompletionRequestDto): String {
        val response = openAiApiService.createChatCompletion(request)
        return response.assistantText()
            .replace("```json", "")
            .replace("```", "")
            .trim()
    }

    private fun isConnected(): Boolean {
        val cm = context.getSystemService(ConnectivityManager::class.java) ?: return false
        val network = cm.activeNetwork ?: return false
        return cm.getNetworkCapabilities(network)
            ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    private fun buildRequest(systemContent: String, userContent: String) =
        OpenAiChatCompletionRequestDto(
            model = OPEN_AI_MODEL,
            messages = listOf(
                OpenAiChatMessageDto(role = "system", content = systemContent),
                OpenAiChatMessageDto(role = "user", content = userContent)
            ),
            maxTokens = 2500
        )

    private fun productUserMessage(project: Project) =
        "Product idea: ${project.title}\nDescription: ${project.description}"

    private inline fun <T> Result<T>.foldWithHttp(): Result<T> =
        fold(
            onSuccess = { Result.success(it) },
            onFailure = { t ->
                when (t) {
                    is HttpException -> {
                        val body = t.response()?.errorBody()?.string().orEmpty()
                        Result.failure(Exception("OpenAI HTTP ${t.code()}: $body", t))
                    }
                    else -> Result.failure(t)
                }
            }
        )

    companion object {
        private const val OPEN_AI_MODEL = "gpt-4o"

        private val COMPETITORS_SYSTEM_PROMPT = """
            You are a senior product strategist. Return ONLY valid JSON, no markdown.
            {
              "competitors": [{ "name": "", "description": "", "weakness": "" }],
              "marketGap": "",
              "painPoints": [{ "painPoint": "", "rationale": "" }]
            }
            Rules: 3-5 real named competitors, specific market gap, exactly 3 pain points.
        """.trimIndent()

        private val INTERVIEWS_SYSTEM_PROMPT = """
            You are simulating user research. Return ONLY valid JSON, no markdown.
            {
              "interviews": [{
                "name": "", "role": "",
                "sentiment": "ENTHUSIASTIC|SKEPTICAL|INDIFFERENT",
                "reaction": "", "quote": ""
              }]
            }
            Rules: exactly 3 interviews one per sentiment, quote must be first-person verbatim.
        """.trimIndent()

        private val SWOT_SYSTEM_PROMPT = """
            You are a senior product strategist. Return ONLY valid JSON, no markdown.
            {
              "swot": {
                "strengths": ["","","",""],
                "weaknesses": ["","","",""],
                "opportunities": ["","","",""],
                "threats": ["","","",""]
              }
            }
            Rules: exactly 4 per quadrant, opportunities/threats must reference the
            competitive landscape, no generic filler.
        """.trimIndent()
    }
}

private fun OpenAiChatCompletionResponseDto.assistantText(): String {
    val text = choices.firstOrNull()?.message?.content?.trim().orEmpty()
    if (text.isEmpty()) {
        throw IllegalStateException("OpenAI response had no assistant content")
    }
    return text
}
