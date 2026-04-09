package com.designspark.ui.screens.projectdetail

import app.cash.turbine.test
import androidx.lifecycle.SavedStateHandle
import com.designspark.domain.model.GeneratedInsight
import com.designspark.domain.model.InsightType
import com.designspark.domain.model.Project
import com.designspark.domain.model.ProjectStage
import com.designspark.domain.model.ProjectStatus
import com.designspark.domain.model.ProjectWithInsights
import com.designspark.domain.model.RiskLevel
import com.designspark.domain.usecase.GetProjectWithInsightsUseCase
import com.designspark.domain.usecase.SaveAnnotationUseCase
import com.designspark.ui.navigation.Screen
import com.google.gson.Gson
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProjectDetailViewModelTest {

    private val getProjectWithInsightsUseCase: GetProjectWithInsightsUseCase = mockk()
    private val saveAnnotationUseCase: SaveAnnotationUseCase = mockk()
    private val gson = Gson()

    private val testDispatcher = StandardTestDispatcher()

    // JSON strings matching the actual DTO fields serialized by remote/dto/Mappers.kt
    private val personaJson = """{"name":"Alex","age":25,"role":"Student","goal":"Learn quickly","frustration":"Complex UI"}"""
    private val methodJson = """{"method":"User Interviews","whyThisFits":"Direct insight from target users","estimatedTime":"1h per session"}"""
    private val assumptionJson = """{"assumption":"Users will adopt new workflow","risk":"HIGH","rationale":"Requires behavior change"}"""
    private val briefJson = """{"whoToFind":"University students","screenFor":"Active learners","exclude":"Staff"}"""

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loading to success - parses all insight types correctly`() = runTest(testDispatcher) {
        val pwi = projectWithInsights()
        every { getProjectWithInsightsUseCase("proj-1") } returns flowOf(pwi)
        val viewModel = buildViewModel()

        viewModel.uiState.test {
            val loading = awaitItem()
            assertTrue(loading.isLoading)

            val success = awaitItem()
            assertFalse(success.isLoading)
            assertEquals("Test Project", success.projectTitle)

            // Personas
            assertEquals(1, success.personas.size)
            val persona = success.personas[0]
            assertEquals("Alex", persona.data.name)
            assertEquals(25, persona.data.age)
            assertEquals("Student", persona.data.role)

            // Method cards
            assertEquals(1, success.methodCards.size)
            assertEquals("User Interviews", success.methodCards[0].data.method)
            assertEquals("Direct insight from target users", success.methodCards[0].data.whyThisFits)

            // Assumptions
            assertEquals(1, success.assumptions.size)
            val assumption = success.assumptions[0]
            assertEquals(RiskLevel.HIGH, assumption.riskLevel)
            assertEquals("Requires behavior change", assumption.data.rationale)

            // Recruit brief
            assertNotNull(success.recruitBrief)
            assertEquals("University students", success.recruitBrief?.data?.whoToFind)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loading to success - empty insights list`() = runTest(testDispatcher) {
        val project = project("proj-1")
        val pwi = ProjectWithInsights(project = project, insights = emptyList())
        every { getProjectWithInsightsUseCase("proj-1") } returns flowOf(pwi)
        val viewModel = buildViewModel()

        viewModel.uiState.test {
            awaitItem() // loading

            val success = awaitItem()
            assertFalse(success.isLoading)
            assertTrue(success.personas.isEmpty())
            assertTrue(success.methodCards.isEmpty())
            assertTrue(success.assumptions.isEmpty())
            assertNull(success.recruitBrief)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `saveAnnotation - delegates to use case with correct arguments`() = runTest(testDispatcher) {
        every { getProjectWithInsightsUseCase("proj-1") } returns flowOf(
            ProjectWithInsights(project("proj-1"), emptyList())
        )
        coEvery { saveAnnotationUseCase("insight-1", "Great point") } returns Result.success(Unit)
        val viewModel = buildViewModel()

        viewModel.saveAnnotation("insight-1", "Great point")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { saveAnnotationUseCase("insight-1", "Great point") }
    }

    private fun buildViewModel() = ProjectDetailViewModel(
        savedStateHandle = SavedStateHandle(mapOf(Screen.ARG_PROJECT_ID to "proj-1")),
        getProjectWithInsightsUseCase = getProjectWithInsightsUseCase,
        saveAnnotationUseCase = saveAnnotationUseCase,
        gson = gson
    )

    private fun project(id: String) = Project(
        id = id,
        title = "Test Project",
        userGroup = "Test Users",
        context = "Test Context",
        stage = ProjectStage.NOTHING,
        createdAt = 0L,
        updatedAt = 0L,
        status = ProjectStatus.GENERATED,
        isSynced = true
    )

    private fun projectWithInsights() = ProjectWithInsights(
        project = project("proj-1"),
        insights = listOf(
            GeneratedInsight("i1", "proj-1", InsightType.PERSONA, "Alex", personaJson, null, 0, 0L),
            GeneratedInsight("i2", "proj-1", InsightType.METHOD_CARD, "User Interviews", methodJson, null, 1, 0L),
            GeneratedInsight("i3", "proj-1", InsightType.ASSUMPTION, "Users will adopt", assumptionJson, RiskLevel.HIGH, 2, 0L),
            GeneratedInsight("i4", "proj-1", InsightType.RECRUIT_BRIEF, "Recruit Brief", briefJson, null, 3, 0L)
        )
    )
}
