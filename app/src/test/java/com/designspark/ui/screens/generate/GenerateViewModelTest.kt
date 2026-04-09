package com.designspark.ui.screens.generate

import app.cash.turbine.test
import com.designspark.domain.model.Project
import com.designspark.domain.model.ProjectStage
import com.designspark.domain.model.ProjectStatus
import com.designspark.domain.usecase.CreateProjectUseCase
import com.designspark.domain.usecase.GenerateInsightsUseCase
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GenerateViewModelTest {

    private val createProjectUseCase: CreateProjectUseCase = mockk()
    private val generateInsightsUseCase: GenerateInsightsUseCase = mockk()

    private val testDispatcher = StandardTestDispatcher()

    private val testProject = Project(
        id = "proj-1",
        title = "My App",
        userGroup = "Students",
        context = "Mobile learning tool",
        stage = ProjectStage.ROUGH_IDEA,
        createdAt = 0L,
        updatedAt = 0L,
        status = ProjectStatus.DRAFT,
        isSynced = false
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
    fun `onGenerate - loading to success - sets navigateToProject`() = runTest(testDispatcher) {
        coEvery { createProjectUseCase(any(), any(), any(), any()) } returns Result.success(testProject)
        coEvery { generateInsightsUseCase(testProject) } returns Result.success(Unit)
        val viewModel = GenerateViewModel(createProjectUseCase, generateInsightsUseCase)

        // Set form state before collecting to avoid intermediate field-change emissions
        viewModel.onTitleChange("My App")
        viewModel.onUserGroupChange("Students")
        viewModel.onContextChange("Mobile learning tool")
        viewModel.onStageChange(ProjectStage.ROUGH_IDEA)

        viewModel.uiState.test {
            awaitItem() // current state with form filled

            viewModel.onGenerate()

            val loading = awaitItem()
            assertTrue(loading.isLoading)
            assertNull(loading.navigateToProject)

            val success = awaitItem()
            assertFalse(success.isLoading)
            assertEquals("proj-1", success.navigateToProject)
            assertNull(success.errorMessage)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onGenerate - loading to error when createProject fails`() = runTest(testDispatcher) {
        coEvery { createProjectUseCase(any(), any(), any(), any()) } returns
                Result.failure(IllegalArgumentException("Title must not be empty"))
        val viewModel = GenerateViewModel(createProjectUseCase, generateInsightsUseCase)

        viewModel.onTitleChange("Title")
        viewModel.onUserGroupChange("Group")
        viewModel.onContextChange("Context")

        viewModel.uiState.test {
            awaitItem() // form state

            viewModel.onGenerate()

            val loading = awaitItem()
            assertTrue(loading.isLoading)

            val error = awaitItem()
            assertFalse(error.isLoading)
            assertNotNull(error.errorMessage)
            assertEquals("Title must not be empty", error.errorMessage)
            assertNull(error.navigateToProject)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onGenerate - loading to error when generateInsights fails`() = runTest(testDispatcher) {
        coEvery { createProjectUseCase(any(), any(), any(), any()) } returns Result.success(testProject)
        coEvery { generateInsightsUseCase(testProject) } returns Result.failure(RuntimeException("API error"))
        val viewModel = GenerateViewModel(createProjectUseCase, generateInsightsUseCase)

        viewModel.onTitleChange("My App")
        viewModel.onUserGroupChange("Students")
        viewModel.onContextChange("Mobile learning tool")

        viewModel.uiState.test {
            awaitItem() // form state

            viewModel.onGenerate()

            val loading = awaitItem()
            assertTrue(loading.isLoading)

            val error = awaitItem()
            assertFalse(error.isLoading)
            assertEquals("API error", error.errorMessage)
            assertNull(error.navigateToProject)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onErrorDismissed - clears errorMessage`() = runTest(testDispatcher) {
        coEvery { createProjectUseCase(any(), any(), any(), any()) } returns
                Result.failure(RuntimeException("Network error"))
        val viewModel = GenerateViewModel(createProjectUseCase, generateInsightsUseCase)

        viewModel.onTitleChange("T")
        viewModel.onUserGroupChange("G")
        viewModel.onContextChange("C")

        viewModel.uiState.test {
            awaitItem()
            viewModel.onGenerate()
            awaitItem() // loading
            awaitItem() // error

            viewModel.onErrorDismissed()

            val cleared = awaitItem()
            assertNull(cleared.errorMessage)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onNavigatedToProject - clears navigateToProject`() = runTest(testDispatcher) {
        coEvery { createProjectUseCase(any(), any(), any(), any()) } returns Result.success(testProject)
        coEvery { generateInsightsUseCase(testProject) } returns Result.success(Unit)
        val viewModel = GenerateViewModel(createProjectUseCase, generateInsightsUseCase)

        viewModel.onTitleChange("My App")
        viewModel.onUserGroupChange("Students")
        viewModel.onContextChange("Mobile learning tool")

        viewModel.uiState.test {
            awaitItem()
            viewModel.onGenerate()
            awaitItem() // loading
            val success = awaitItem()
            assertNotNull(success.navigateToProject)

            viewModel.onNavigatedToProject()

            val cleared = awaitItem()
            assertNull(cleared.navigateToProject)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
