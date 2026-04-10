package com.designspark.ui.screens.userinterviews

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.designspark.domain.model.GeneratedInsight
import com.designspark.domain.model.Project
import com.designspark.domain.usecase.GenerateInterviewsUseCase
import com.designspark.domain.usecase.GetProjectWithInsightsUseCase
import com.designspark.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

data class UserInterviewsUiState(
    val isLoading: Boolean = false,
    val interviews: List<GeneratedInsight> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class UserInterviewsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getProjectWithInsightsUseCase: GetProjectWithInsightsUseCase,
    private val generateInterviewsUseCase: GenerateInterviewsUseCase
) : ViewModel() {

    private val projectId: String = checkNotNull(savedStateHandle[Screen.ARG_PROJECT_ID])
    private val generationStarted = AtomicBoolean(false)

    private val _uiState = MutableStateFlow(UserInterviewsUiState(isLoading = true))
    val uiState: StateFlow<UserInterviewsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getProjectWithInsightsUseCase(projectId).collect { pwi ->
                val list = pwi.interviews
                if (list.isNotEmpty()) {
                    _uiState.value = UserInterviewsUiState(
                        isLoading = false,
                        interviews = list.sortedBy { it.orderIndex },
                        error = null
                    )
                } else if (pwi.project != null && generationStarted.compareAndSet(false, true)) {
                    runGenerate(pwi.project)
                }
            }
        }
    }

    fun retry() {
        viewModelScope.launch {
            generationStarted.set(false)
            val pwi = getProjectWithInsightsUseCase(projectId).first()
            val project = pwi.project ?: return@launch
            if (pwi.interviews.isEmpty() && generationStarted.compareAndSet(false, true)) {
                runGenerate(project)
            }
        }
    }

    private suspend fun runGenerate(project: Project) {
        _uiState.update { UserInterviewsUiState(isLoading = true, error = null) }
        val result = generateInterviewsUseCase(project)
        result.onFailure { e ->
            generationStarted.set(false)
            _uiState.update {
                UserInterviewsUiState(
                    isLoading = false,
                    error = e.message ?: "Generation failed"
                )
            }
        }
    }
}
