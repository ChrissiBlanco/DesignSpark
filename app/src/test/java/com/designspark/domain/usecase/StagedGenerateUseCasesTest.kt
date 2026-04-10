package com.designspark.domain.usecase

import com.designspark.domain.model.Project
import com.designspark.domain.repository.ProjectRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class StagedGenerateUseCasesTest {

    private val repository: ProjectRepository = mockk(relaxed = true)
    private val project = Project("p1", "T", "D", 0L, 0L)

    private lateinit var generateCompetitors: GenerateCompetitorsUseCase
    private lateinit var generateInterviews: GenerateInterviewsUseCase
    private lateinit var generateSwot: GenerateSwotUseCase
    private lateinit var markStage1Complete: MarkStage1CompleteUseCase

    @Before
    fun setUp() {
        generateCompetitors = GenerateCompetitorsUseCase(repository)
        generateInterviews = GenerateInterviewsUseCase(repository)
        generateSwot = GenerateSwotUseCase(repository)
        markStage1Complete = MarkStage1CompleteUseCase(repository)
    }

    @Test
    fun `GenerateCompetitorsUseCase delegates to repository`() = runTest {
        coEvery { repository.generateCompetitors(project) } returns Result.success(Unit)

        val result = generateCompetitors(project)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.generateCompetitors(project) }
    }

    @Test
    fun `GenerateInterviewsUseCase delegates to repository`() = runTest {
        coEvery { repository.generateInterviews(project) } returns Result.success(Unit)

        val result = generateInterviews(project)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.generateInterviews(project) }
    }

    @Test
    fun `GenerateSwotUseCase delegates to repository`() = runTest {
        coEvery { repository.generateSwot(project) } returns Result.success(Unit)

        val result = generateSwot(project)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.generateSwot(project) }
    }

    @Test
    fun `MarkStage1CompleteUseCase delegates to repository`() = runTest {
        coEvery { repository.markStage1Complete(any()) } returns Unit

        markStage1Complete("p1")

        coVerify(exactly = 1) { repository.markStage1Complete("p1") }
    }
}
