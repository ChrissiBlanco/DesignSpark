package com.designspark.domain.usecase

import com.designspark.domain.repository.ProjectRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SaveAnnotationUseCaseTest {

    private val repository: ProjectRepository = mockk()
    private lateinit var useCase: SaveAnnotationUseCase

    @Before
    fun setUp() {
        useCase = SaveAnnotationUseCase(repository)
    }

    @Test
    fun `happy path - saves annotation and returns success`() = runTest {
        coEvery { repository.saveAnnotation(any()) } returns Unit

        val result = useCase("insight-1", "This is a useful insight.")

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.saveAnnotation(any()) }
    }

    @Test
    fun `happy path - trims whitespace before saving`() = runTest {
        coEvery { repository.saveAnnotation(any()) } returns Unit

        val result = useCase("insight-1", "  trimmed note  ")

        assertTrue(result.isSuccess)
        coVerify { repository.saveAnnotation(match { it.note == "trimmed note" }) }
    }

    @Test
    fun `error - blank note returns failure without calling repository`() = runTest {
        val result = useCase("insight-1", "")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("empty") == true)
        coVerify(exactly = 0) { repository.saveAnnotation(any()) }
    }

    @Test
    fun `error - whitespace-only note returns failure`() = runTest {
        val result = useCase("insight-1", "   ")

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { repository.saveAnnotation(any()) }
    }

    @Test
    fun `error - repository throws propagates as failure`() = runTest {
        coEvery { repository.saveAnnotation(any()) } throws RuntimeException("DB error")

        val result = useCase("insight-1", "Some note")

        assertTrue(result.isFailure)
    }
}
