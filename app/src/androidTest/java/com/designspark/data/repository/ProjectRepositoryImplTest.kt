package com.designspark.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.designspark.data.local.AppDatabase
import com.designspark.data.local.entity.ProjectEntity
import com.designspark.data.remote.api.AnthropicApiService
import com.designspark.data.remote.dto.AnthropicResponseDto
import com.designspark.data.remote.dto.ContentDto
import com.designspark.domain.model.Project
import com.designspark.domain.model.ProjectStage
import com.designspark.domain.model.ProjectStatus
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProjectRepositoryImplTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: ProjectRepositoryImpl
    private val apiService: AnthropicApiService = mockk()

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        repository = ProjectRepositoryImpl(
            context = ApplicationProvider.getApplicationContext<Context>(),
            db = db,
            projectDao = db.projectDao(),
            generatedInsightDao = db.generatedInsightDao(),
            annotationDao = db.annotationDao(),
            anthropicApiService = apiService
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun generateInsights_writes_all_insight_types_to_room() = runTest {
        repository.createProject(testProject())
        coEvery { apiService.generateInsights(any()) } returns validApiResponse()

        val result = repository.generateInsights(testProject())

        assertTrue(result.isSuccess)
        val insights = db.generatedInsightDao().getByProjectId("proj-1").first()
        // 3 personas + 3 method cards + 4 assumptions + 1 recruit brief = 11
        assertEquals(11, insights.size)
        assertEquals(3, insights.count { it.type == "PERSONA" })
        assertEquals(3, insights.count { it.type == "METHOD_CARD" })
        assertEquals(4, insights.count { it.type == "ASSUMPTION" })
        assertEquals(1, insights.count { it.type == "RECRUIT_BRIEF" })
    }

    @Test
    fun generateInsights_updates_project_status_to_generated() = runTest {
        repository.createProject(testProject())
        coEvery { apiService.generateInsights(any()) } returns validApiResponse()

        repository.generateInsights(testProject())

        val project = db.projectDao().getById("proj-1").first()
        assertEquals("GENERATED", project?.status)
        assertEquals(true, project?.isSynced)
    }

    @Test
    fun generateInsights_stores_correct_persona_title_from_name_field() = runTest {
        repository.createProject(testProject())
        coEvery { apiService.generateInsights(any()) } returns validApiResponse()

        repository.generateInsights(testProject())

        val insights = db.generatedInsightDao().getByProjectId("proj-1").first()
        val personas = insights.filter { it.type == "PERSONA" }
        val titles = personas.map { it.title }.toSet()
        assertTrue(titles.contains("Alex Chen"))
    }

    @Test
    fun generateInsights_stores_assumption_risk_level() = runTest {
        repository.createProject(testProject())
        coEvery { apiService.generateInsights(any()) } returns validApiResponse()

        repository.generateInsights(testProject())

        val insights = db.generatedInsightDao().getByProjectId("proj-1").first()
        val highRisk = insights.filter { it.type == "ASSUMPTION" && it.riskLevel == "HIGH" }
        assertTrue(highRisk.isNotEmpty())
    }

    @Test
    fun generateInsights_on_api_error_returns_failure_and_writes_nothing() = runTest {
        repository.createProject(testProject())
        coEvery { apiService.generateInsights(any()) } throws RuntimeException("Network error")

        val result = repository.generateInsights(testProject())

        assertTrue(result.isFailure)
        val insights = db.generatedInsightDao().getByProjectId("proj-1").first()
        assertTrue(insights.isEmpty())
    }

    @Test
    fun generateInsights_on_api_error_leaves_project_status_unchanged() = runTest {
        repository.createProject(testProject())
        coEvery { apiService.generateInsights(any()) } throws RuntimeException("Network error")

        repository.generateInsights(testProject())

        val project = db.projectDao().getById("proj-1").first()
        assertEquals("DRAFT", project?.status)
    }

    @Test
    fun generateInsights_replaces_existing_insights_on_re_generation() = runTest {
        repository.createProject(testProject())
        coEvery { apiService.generateInsights(any()) } returns validApiResponse()

        repository.generateInsights(testProject())
        repository.generateInsights(testProject())

        val insights = db.generatedInsightDao().getByProjectId("proj-1").first()
        assertEquals(11, insights.size) // not doubled — old ones deleted first
    }

    @Test
    fun generateInsights_strips_markdown_fences_from_api_response() = runTest {
        repository.createProject(testProject())
        val jsonWithFences = "```json\n${validJson()}\n```"
        coEvery { apiService.generateInsights(any()) } returns apiResponse(jsonWithFences)

        val result = repository.generateInsights(testProject())

        assertTrue(result.isSuccess)
    }

    private fun testProject() = Project(
        id = "proj-1",
        title = "Test App",
        userGroup = "Students",
        context = "Mobile learning",
        stage = ProjectStage.ROUGH_IDEA,
        createdAt = 1000L,
        updatedAt = 1000L,
        status = ProjectStatus.DRAFT,
        isSynced = false
    )

    private fun validApiResponse() = apiResponse(validJson())

    private fun apiResponse(json: String) = AnthropicResponseDto(
        id = "msg-test",
        type = "message",
        role = "assistant",
        content = listOf(ContentDto(type = "text", text = json)),
        model = "claude-sonnet-4-20250514",
        stopReason = "end_turn"
    )

    private fun validJson() = """
        {
          "personas": [
            {"name": "Alex Chen", "age": 22, "role": "Computer Science student", "goal": "Learn efficiently", "frustration": "Cluttered interfaces"},
            {"name": "Maria Garcia", "age": 25, "role": "Education major", "goal": "Organize coursework", "frustration": "Too many apps"},
            {"name": "Sam Lee", "age": 20, "role": "First-year student", "goal": "Stay on track", "frustration": "Hard to prioritize"}
          ],
          "methodCards": [
            {"method": "Diary Study", "whyThisFits": "Captures real-world usage patterns in natural setting", "estimatedTime": "2 weeks"},
            {"method": "Contextual Inquiry", "whyThisFits": "Observes students in their study environment", "estimatedTime": "3 hours"},
            {"method": "Think-Aloud Protocol", "whyThisFits": "Reveals mental model during task completion", "estimatedTime": "1 hour"}
          ],
          "assumptionsToTest": [
            {"assumption": "Students will switch from existing tools", "risk": "HIGH", "rationale": "High switching cost"},
            {"assumption": "Daily active use will be consistent", "risk": "HIGH", "rationale": "Habit formation is hard"},
            {"assumption": "Students will invite peers", "risk": "MEDIUM", "rationale": "Social features depend on critical mass"},
            {"assumption": "Push notifications improve retention", "risk": "LOW", "rationale": "Well-established pattern"}
          ],
          "recruitBrief": {
            "whoToFind": "Undergraduate students at university",
            "screenFor": "At least 3 active courses, uses digital tools for studying",
            "exclude": "Graduate students, staff, faculty"
          }
        }
    """.trimIndent()
}
