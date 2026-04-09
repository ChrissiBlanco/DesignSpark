package com.designspark.domain.usecase

import com.designspark.domain.model.Project
import com.designspark.domain.model.ProjectStage
import com.designspark.domain.model.ProjectStatus
import com.designspark.domain.repository.ProjectRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GenerateInsightsUseCaseTest {

    private val repository: ProjectRepository = mockk()
    private lateinit var useCase: GenerateInsightsUseCase

    private val project = Project(
        id = "proj-1",
        title = "Test App",
        userGroup = "Students",
        context = "Mobile",
        stage = ProjectStage.ROUGH_IDEA,
        createdAt = 0L,
        updatedAt = 0L,
        status = ProjectStatus.DRAFT,
        isSynced = false
    )

    @Before
    fun setUp() {
        useCase = GenerateInsightsUseCase(repository)
    }

    @Test
    fun `happy path - returns success when repository succeeds`() = runTest {
        coEvery { repository.generateInsights(project) } returns Result.success(Unit)

        val result = useCase(project)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.generateInsights(project) }
    }

    @Test
    fun `error path - returns failure when repository fails`() = runTest {
        coEvery { repository.generateInsights(project) } returns Result.failure(RuntimeException("API error"))

        val result = useCase(project)

        assertTrue(result.isFailure)
        assertEquals("API error", result.exceptionOrNull()?.message)
    }
}
