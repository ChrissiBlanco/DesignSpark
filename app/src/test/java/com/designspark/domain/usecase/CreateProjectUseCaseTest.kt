package com.designspark.domain.usecase

import com.designspark.domain.model.Project
import com.designspark.domain.repository.ProjectRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CreateProjectUseCaseTest {

    private val repository: ProjectRepository = mockk()
    private lateinit var useCase: CreateProjectUseCase

    @Before
    fun setUp() {
        coEvery { repository.createProject(any()) } returns Unit
        useCase = CreateProjectUseCase(repository)
    }

    @Test
    fun `happy path - creates project with trimmed fields`() = runTest {
        val captured = slot<Project>()
        val result = useCase("My App", "A product for students")

        val project = result.getOrThrow()
        assertEquals("My App", project.title)
        assertEquals("A product for students", project.description)
        assertTrue(!project.stage1Complete)
        coVerify(exactly = 1) { repository.createProject(capture(captured)) }
        assertEquals(project.id, captured.captured.id)
    }

    @Test
    fun `trims title and description`() = runTest {
        val result = useCase("  My App  ", "  Desc  ")

        assertEquals("My App", result.getOrThrow().title)
        assertEquals("Desc", result.getOrThrow().description)
    }

    @Test
    fun `error - blank title returns failure`() = runTest {
        val result = useCase("", "Context")

        assertTrue(result.isFailure)
    }

    @Test
    fun `error - whitespace-only title returns failure`() = runTest {
        val result = useCase("   ", "Context")

        assertTrue(result.isFailure)
    }

    @Test
    fun `error - blank description returns failure`() = runTest {
        val result = useCase("Title", "")

        assertTrue(result.isFailure)
    }

    @Test
    fun `error - whitespace-only description returns failure`() = runTest {
        val result = useCase("Title", "   ")

        assertTrue(result.isFailure)
    }
}
