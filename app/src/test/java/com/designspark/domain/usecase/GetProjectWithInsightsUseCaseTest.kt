package com.designspark.domain.usecase

import com.designspark.domain.model.GeneratedInsight
import com.designspark.domain.model.InsightType
import com.designspark.domain.model.Project
import com.designspark.domain.model.ProjectWithInsights
import com.designspark.domain.repository.ProjectRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
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
    fun `emits project with grouped insights`() = runTest {
        val project = Project("p1", "App", "Desc", 0L, 0L)
        val competitor = GeneratedInsight(
            "i1", "p1", InsightType.COMPETITOR, "C1", "{}", null, 0, 0L
        )
        val pwi = ProjectWithInsights(
            project = project,
            competitors = listOf(competitor),
            interviews = emptyList(),
            swotItems = emptyList()
        )
        every { repository.getProjectWithInsights("p1") } returns flowOf(pwi)

        val emitted = useCase("p1").toList()

        assertEquals(listOf(pwi), emitted)
        assertEquals(1, emitted.first().competitors.size)
    }

    @Test
    fun `uses given project id`() = runTest {
        val project = Project("p1", "App", "Desc", 0L, 0L)
        val pwi = ProjectWithInsights(project, emptyList(), emptyList(), emptyList())
        every { repository.getProjectWithInsights("other") } returns flowOf(pwi)

        useCase("other").toList()

        verify(exactly = 1) { repository.getProjectWithInsights("other") }
    }
}
