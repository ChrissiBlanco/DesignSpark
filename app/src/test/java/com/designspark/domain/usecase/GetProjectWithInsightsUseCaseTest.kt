package com.designspark.domain.usecase

import app.cash.turbine.test
import com.designspark.domain.model.GeneratedInsight
import com.designspark.domain.model.InsightType
import com.designspark.domain.model.Project
import com.designspark.domain.model.ProjectStage
import com.designspark.domain.model.ProjectStatus
import com.designspark.domain.model.ProjectWithInsights
import com.designspark.domain.repository.ProjectRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetProjectWithInsightsUseCaseTest {

    private val repository: ProjectRepository = mockk()
    private lateinit var useCase: GetProjectWithInsightsUseCase

    @Before
    fun setUp() {
        useCase = GetProjectWithInsightsUseCase(repository)
    }

    @Test
    fun `emits project with insights from repository`() = runTest {
        val project = Project("p1", "App", "Users", "Context", ProjectStage.NOTHING, 0L, 0L, ProjectStatus.GENERATED, false)
        val insight = GeneratedInsight("i1", "p1", InsightType.PERSONA, "Alex", "{}", null, 0, 0L)
        val pwi = ProjectWithInsights(project = project, insights = listOf(insight))
        every { repository.getProjectWithInsights("p1") } returns flowOf(pwi)

        useCase("p1").test {
            val result = awaitItem()
            assertEquals(pwi, result)
            assertEquals(1, result.insights.size)
            awaitComplete()
        }
    }

    @Test
    fun `emits project with empty insights list`() = runTest {
        val project = Project("p1", "App", "Users", "Context", ProjectStage.NOTHING, 0L, 0L, ProjectStatus.DRAFT, false)
        val pwi = ProjectWithInsights(project = project, insights = emptyList())
        every { repository.getProjectWithInsights("p1") } returns flowOf(pwi)

        useCase("p1").test {
            assertEquals(pwi, awaitItem())
            awaitComplete()
        }
    }
}
