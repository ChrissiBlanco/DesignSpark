package com.designspark.ui.screens.summary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.designspark.domain.model.ProjectWithInsights
import com.designspark.domain.usecase.GetProjectWithInsightsUseCase
import com.designspark.domain.usecase.MarkStage1CompleteUseCase
import com.designspark.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

data class SummaryUiState(
    val projectWithInsights: ProjectWithInsights? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class SummaryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getProjectWithInsightsUseCase: GetProjectWithInsightsUseCase,
    private val markStage1CompleteUseCase: MarkStage1CompleteUseCase
) : ViewModel() {

    private val projectId: String = checkNotNull(savedStateHandle[Screen.ARG_PROJECT_ID])
    private val stage1Marked = AtomicBoolean(false)

    private val _uiState = MutableStateFlow(SummaryUiState())
    val uiState: StateFlow<SummaryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getProjectWithInsightsUseCase(projectId).collect { pwi ->
                val complete = pwi.competitors.isNotEmpty() &&
                    pwi.interviews.isNotEmpty() &&
                    pwi.swotItems.isNotEmpty()
                if (complete &&
                    !pwi.project.stage1Complete &&
                    stage1Marked.compareAndSet(false, true)
                ) {
                    markStage1CompleteUseCase(projectId)
                }
                _uiState.update {
                    SummaryUiState(projectWithInsights = pwi, isLoading = false)
                }
            }
        }
    }
}
