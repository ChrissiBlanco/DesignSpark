package com.designspark.ui.screens.stage1

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.designspark.domain.model.Project
import com.designspark.domain.usecase.GetProjectWithInsightsUseCase
import com.designspark.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class Stage1UiState(
    val project: Project? = null,
    val competitorsComplete: Boolean = false,
    val interviewsComplete: Boolean = false,
    val swotComplete: Boolean = false
)

@HiltViewModel
class Stage1ViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getProjectWithInsightsUseCase: GetProjectWithInsightsUseCase
) : ViewModel() {

    private val projectId: String = checkNotNull(savedStateHandle[Screen.ARG_PROJECT_ID])

    private val _uiState = MutableStateFlow(Stage1UiState())
    val uiState: StateFlow<Stage1UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getProjectWithInsightsUseCase(projectId).collect { pwi ->
                _uiState.update {
                    Stage1UiState(
                        project = pwi.project,
                        competitorsComplete = pwi.competitors.isNotEmpty(),
                        interviewsComplete = pwi.interviews.isNotEmpty(),
                        swotComplete = pwi.swotItems.isNotEmpty()
                    )
                }
            }
        }
    }
}
