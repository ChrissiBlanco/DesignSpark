package com.designspark.ui.screens.home

import app.cash.turbine.test
import com.designspark.domain.model.Project
import com.designspark.domain.usecase.DeleteProjectUseCase
import com.designspark.domain.usecase.GetProjectsUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val getProjectsUseCase: GetProjectsUseCase = mockk()
    private val deleteProjectUseCase: DeleteProjectUseCase = mockk()

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loading to success - emits initial loading then project list`() = runTest(testDispatcher) {
        val projects = listOf(
            project("1", "App A"),
            project("2", "App B")
        )
        every { getProjectsUseCase() } returns flowOf(projects)
        val viewModel = HomeViewModel(getProjectsUseCase, deleteProjectUseCase)

        viewModel.uiState.test {
            val loading = awaitItem()
            assertTrue(loading.isLoading)
            assertTrue(loading.projects.isEmpty())

            val success = awaitItem()
            assertFalse(success.isLoading)
            assertEquals(projects, success.projects)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loading to success - empty list`() = runTest(testDispatcher) {
        every { getProjectsUseCase() } returns flowOf(emptyList())
        val viewModel = HomeViewModel(getProjectsUseCase, deleteProjectUseCase)

        viewModel.uiState.test {
            val loading = awaitItem()
            assertTrue(loading.isLoading)

            val success = awaitItem()
            assertFalse(success.isLoading)
            assertEquals(emptyList<Project>(), success.projects)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `confirmDelete - calls use case with correct id`() = runTest(testDispatcher) {
        every { getProjectsUseCase() } returns flowOf(emptyList())
        coEvery { deleteProjectUseCase("proj-1") } returns Unit
        val viewModel = HomeViewModel(getProjectsUseCase, deleteProjectUseCase)

        viewModel.confirmDelete("proj-1")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { deleteProjectUseCase("proj-1") }
    }

    private fun project(id: String, title: String) = Project(
        id = id,
        title = title,
        description = "Context",
        createdAt = 0L,
        updatedAt = 0L
    )
}
