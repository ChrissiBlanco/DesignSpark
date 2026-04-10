package com.designspark.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.designspark.data.local.AppDatabase
import com.designspark.data.remote.api.OpenAiApiService
import com.designspark.data.remote.dto.OpenAiChatAssistantMessageDto
import com.designspark.data.remote.dto.OpenAiChatChoiceDto
import com.designspark.data.remote.dto.OpenAiChatCompletionResponseDto
import com.designspark.domain.model.Project
import com.google.gson.Gson
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
    private val apiService: OpenAiApiService = mockk()
    private val gson = Gson()

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
            openAiApiService = apiService,
            gson = gson
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun generateCompetitors_writes_competitor_type_insights() = runTest {
        repository.createProject(testProject())
        coEvery { apiService.createChatCompletion(any()) } returns competitorApiResponse()

        val result = repository.generateCompetitors(testProject())

        assertTrue(result.isSuccess)
        val insights = db.generatedInsightDao().getByProjectId("proj-1").first()
        assertTrue(insights.all { it.type == "COMPETITOR" })
        assertEquals(1, insights.count { it.title == "Market gap" })
        assertEquals(2, insights.count { it.title.startsWith("Pain ") })
    }

    @Test
    fun generateInterviews_writes_three_user_interview_insights() = runTest {
        repository.createProject(testProject())
        coEvery { apiService.createChatCompletion(any()) } returns interviewsApiResponse()

        val result = repository.generateInterviews(testProject())

        assertTrue(result.isSuccess)
        val insights = db.generatedInsightDao().getByProjectId("proj-1").first()
        assertEquals(3, insights.size)
        assertTrue(insights.all { it.type == "USER_INTERVIEW" })
    }

    @Test
    fun generateSwot_without_competitors_returns_failure() = runTest {
        repository.createProject(testProject())

        val result = repository.generateSwot(testProject())

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
    }

    @Test
    fun generateSwot_writes_sixteen_swot_items_with_order_and_quadrants() = runTest {
        repository.createProject(testProject())
        coEvery { apiService.createChatCompletion(any()) } returnsMany listOf(
            competitorApiResponse(),
            swotApiResponse()
        )

        repository.generateCompetitors(testProject())
        val result = repository.generateSwot(testProject())

        assertTrue(result.isSuccess)
        val swot = db.generatedInsightDao()
            .getByProjectIdAndType("proj-1", "SWOT_ITEM")
            .first()
        assertEquals(16, swot.size)
        assertEquals((0..15).toList(), swot.map { it.orderIndex })
        assertEquals(listOf("STRENGTH"), swot.take(4).map { it.quadrant }.distinct())
        assertEquals(listOf("WEAKNESS"), swot.drop(4).take(4).map { it.quadrant }.distinct())
    }

    @Test
    fun generateCompetitors_strips_markdown_fences() = runTest {
        repository.createProject(testProject())
        val jsonWithFences = "```json\n${competitorJson()}\n```"
        coEvery { apiService.createChatCompletion(any()) } returns apiResponse(jsonWithFences)

        val result = repository.generateCompetitors(testProject())

        assertTrue(result.isSuccess)
    }

    private fun testProject() = Project(
        id = "proj-1",
        title = "Test App",
        description = "Mobile learning for students",
        createdAt = 1000L,
        updatedAt = 1000L
    )

    private fun competitorApiResponse() = apiResponse(competitorJson())

    private fun competitorJson() = """
        {
          "competitors": [
            { "name": "Duolingo", "description": "Language learning", "weakness": "Not course-wide" }
          ],
          "marketGap": "Integrated campus scheduling",
          "painPoints": [
            { "painPoint": "Pain 1", "rationale": "R1" },
            { "painPoint": "Pain 2", "rationale": "R2" }
          ]
        }
    """.trimIndent()

    private fun interviewsApiResponse() = apiResponse(
        """
        {
          "interviews": [
            { "name": "Alex", "role": "Student", "sentiment": "ENTHUSIASTIC",
              "reaction": "Loves it", "quote": "I would use this daily." },
            { "name": "Sam", "role": "TA", "sentiment": "SKEPTICAL",
              "reaction": "Unsure", "quote": "I doubt adoption." },
            { "name": "Lee", "role": "Admin", "sentiment": "INDIFFERENT",
              "reaction": "Neutral", "quote": "I might try it." }
          ]
        }
        """.trimIndent()
    )

    private fun swotApiResponse() = apiResponse(
        """
        {
          "swot": {
            "strengths": ["S1","S2","S3","S4"],
            "weaknesses": ["W1","W2","W3","W4"],
            "opportunities": ["O1","O2","O3","O4"],
            "threats": ["T1","T2","T3","T4"]
          }
        }
        """.trimIndent()
    )

    private fun apiResponse(json: String) = OpenAiChatCompletionResponseDto(
        choices = listOf(
            OpenAiChatChoiceDto(
                message = OpenAiChatAssistantMessageDto(
                    role = "assistant",
                    content = json
                )
            )
        )
    )
}
