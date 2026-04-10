package com.designspark.ui.screens.newidea

import app.cash.turbine.test
import com.designspark.domain.model.Project
import com.designspark.domain.usecase.CreateProjectUseCase
import io.mockk.any
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NewIdeaViewModelTest {

    private val createProjectUseCase: CreateProjectUseCase = mockk()
    private val testDispatcher = StandardTestDispatcher()
    private val testProject = Project(
        id = "proj-1",
        title = "My App",
        description = "A great idea",
        createdAt = 0L,
        updatedAt = 0L
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `submit success emits navigate id and clears loading`() = runTest(testDispatcher) {
        coEvery { createProjectUseCase(any(), any()) } returns Result.success(testProject)
        val vm = NewIdeaViewModel(createProjectUseCase)

        vm.uiState.test {
            assertFalse(awaitItem().isLoading)

            vm.submit("T", "D")
            assertTrue(awaitItem().isLoading)

            val done = awaitItem()
            assertFalse(done.isLoading)
            assertEquals("proj-1", done.navigateToProjectId)
            assertNull(done.error)
        }
    }

    @Test
    fun `submit failure sets error`() = runTest(testDispatcher) {
        coEvery { createProjectUseCase(any(), any()) } returns
            Result.failure(IllegalArgumentException("bad"))
        val vm = NewIdeaViewModel(createProjectUseCase)

        vm.uiState.test {
            awaitItem()
            vm.submit("a", "b")
            awaitItem() // loading
            val err = awaitItem()
            assertFalse(err.isLoading)
            assertEquals("bad", err.error)
            assertNull(err.navigateToProjectId)
        }
    }
}
