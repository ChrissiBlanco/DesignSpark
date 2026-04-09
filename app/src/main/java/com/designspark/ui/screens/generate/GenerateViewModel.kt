package com.designspark.ui.screens.generate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.designspark.domain.model.ProjectStage
import com.designspark.domain.usecase.CreateProjectUseCase
import com.designspark.domain.usecase.GenerateInsightsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GenerateUiState(
    val title: String = "",
    val userGroup: String = "",
    val context: String = "",
    val stage: ProjectStage = ProjectStage.NOTHING,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val navigateToProject: String? = null
)

@HiltViewModel
class GenerateViewModel @Inject constructor(
    private val createProjectUseCase: CreateProjectUseCase,
    private val generateInsightsUseCase: GenerateInsightsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GenerateUiState())
    val uiState: StateFlow<GenerateUiState> = _uiState.asStateFlow()

    fun onTitleChange(value: String) = _uiState.update { it.copy(title = value) }
    fun onUserGroupChange(value: String) = _uiState.update { it.copy(userGroup = value) }
    fun onContextChange(value: String) = _uiState.update { it.copy(context = value) }
    fun onStageChange(value: ProjectStage) = _uiState.update { it.copy(stage = value) }

    fun onGenerate() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            createProjectUseCase(state.title, state.userGroup, state.context, state.stage)
                .onSuccess { project ->
                    generateInsightsUseCase(project)
                        .onSuccess {
                            _uiState.update { it.copy(isLoading = false, navigateToProject = project.id) }
                        }
                        .onFailure { e ->
                            _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Generation failed") }
                        }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Failed to create project") }
                }
        }
    }

    fun onErrorDismissed() = _uiState.update { it.copy(errorMessage = null) }
    fun onNavigatedToProject() = _uiState.update { it.copy(navigateToProject = null) }
}
