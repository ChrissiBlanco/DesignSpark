package com.designspark.domain.usecase

import com.designspark.domain.model.Project
import com.designspark.domain.repository.ProjectRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetProjectsUseCaseTest {

    private val repository: ProjectRepository = mockk()
    private lateinit var useCase: GetProjectsUseCase

    @Before
    fun setUp() {
        useCase = GetProjectsUseCase(repository)
    }

    @Test
    fun `emits projects from repository`() = runTest {
        val projects = listOf(
            Project("1", "App A", "Desc A", 0L, 0L),
            Project("2", "App B", "Desc B", 0L, 0L)
        )
        every { repository.getProjects() } returns flowOf(projects)

        val emitted = useCase().toList()

        assertEquals(listOf(projects), emitted)
    }
}
