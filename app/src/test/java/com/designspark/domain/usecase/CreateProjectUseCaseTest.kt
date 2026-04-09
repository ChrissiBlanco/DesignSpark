package com.designspark.domain.usecase

import com.designspark.domain.model.ProjectStage
import com.designspark.domain.model.ProjectStatus
import com.designspark.domain.repository.ProjectRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CreateProjectUseCaseTest {

    private val repository: ProjectRepository = mockk()
    private lateinit var useCase: CreateProjectUseCase

    @Before
    fun setUp() {
        useCase = CreateProjectUseCase(repository)
    }

    @Test
    fun `happy path - returns project with correct fields`() = runTest {
        coEvery { repository.createProject(any()) } returns Unit

        val result = useCase("My App", "Students", "Mobile study tool", ProjectStage.ROUGH_IDEA)

        assertTrue(result.isSuccess)
        val project = result.getOrThrow()
        assertEquals("My App", project.title)
        assertEquals("Students", project.userGroup)
        assertEquals("Mobile study tool", project.context)
        assertEquals(ProjectStage.ROUGH_IDEA, project.stage)
        assertEquals(ProjectStatus.DRAFT, project.status)
        assertNotNull(project.id)
        coVerify(exactly = 1) { repository.createProject(any()) }
    }

    @Test
    fun `happy path - trims whitespace from inputs`() = runTest {
        coEvery { repository.createProject(any()) } returns Unit

        val result = useCase("  My App  ", " Students ", " Context ", ProjectStage.NOTHING)

        assertTrue(result.isSuccess)
        assertEquals("My App", result.getOrThrow().title)
        assertEquals("Students", result.getOrThrow().userGroup)
        assertEquals("Context", result.getOrThrow().context)
    }

    @Test
    fun `error - blank title returns failure without calling repository`() = runTest {
        val result = useCase("", "Students", "Context", ProjectStage.NOTHING)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Title") == true)
        coVerify(exactly = 0) { repository.createProject(any()) }
    }

    @Test
    fun `error - whitespace title returns failure`() = runTest {
        val result = useCase("   ", "Students", "Context", ProjectStage.NOTHING)

        assertTrue(result.isFailure)
    }

    @Test
    fun `error - blank userGroup returns failure`() = runTest {
        val result = useCase("Title", "", "Context", ProjectStage.NOTHING)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("group") == true)
    }

    @Test
    fun `error - blank context returns failure`() = runTest {
        val result = useCase("Title", "Group", "", ProjectStage.NOTHING)

        assertTrue(result.isFailure)
    }

    @Test
    fun `error - repository throws propagates as failure`() = runTest {
        coEvery { repository.createProject(any()) } throws RuntimeException("DB error")

        val result = useCase("Title", "Group", "Context", ProjectStage.NOTHING)

        assertTrue(result.isFailure)
        assertEquals("DB error", result.exceptionOrNull()?.message)
    }
}
