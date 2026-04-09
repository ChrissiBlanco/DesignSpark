package com.designspark.domain.usecase

import com.designspark.domain.repository.ProjectRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DeleteProjectUseCaseTest {

    private val repository: ProjectRepository = mockk()
    private lateinit var useCase: DeleteProjectUseCase

    @Before
    fun setUp() {
        useCase = DeleteProjectUseCase(repository)
    }

    @Test
    fun `delegates delete to repository with correct id`() = runTest {
        coEvery { repository.deleteProject("proj-1") } returns Unit

        useCase("proj-1")

        coVerify(exactly = 1) { repository.deleteProject("proj-1") }
    }

    @Test
    fun `different ids call repository with different ids`() = runTest {
        coEvery { repository.deleteProject(any()) } returns Unit

        useCase("proj-abc")

        coVerify { repository.deleteProject("proj-abc") }
    }
}
