package com.designspark.domain.usecase

import app.cash.turbine.test
import com.designspark.domain.model.Project
import com.designspark.domain.model.ProjectStage
import com.designspark.domain.model.ProjectStatus
import com.designspark.domain.repository.ProjectRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
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
    fun `emits project list from repository`() = runTest {
        val projects = listOf(
            Project("1", "App A", "Users", "Context", ProjectStage.NOTHING, 0L, 0L, ProjectStatus.DRAFT, false),
            Project("2", "App B", "Designers", "Context", ProjectStage.PROTOTYPE, 0L, 0L, ProjectStatus.GENERATED, false)
        )
        every { repository.getProjects() } returns flowOf(projects)

        useCase().test {
            assertEquals(projects, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `emits empty list when no projects exist`() = runTest {
        every { repository.getProjects() } returns flowOf(emptyList())

        useCase().test {
            assertEquals(emptyList<Project>(), awaitItem())
            awaitComplete()
        }
    }
}
