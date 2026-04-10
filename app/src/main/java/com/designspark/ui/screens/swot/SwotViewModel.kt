package com.designspark.ui.screens.swot

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.designspark.domain.model.GeneratedInsight
import com.designspark.domain.model.Project
import com.designspark.domain.model.SwotQuadrant
import com.designspark.domain.usecase.GenerateSwotUseCase
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

data class SwotUiState(
    val isLoading: Boolean = false,
    val strengths: List<GeneratedInsight> = emptyList(),
    val weaknesses: List<GeneratedInsight> = emptyList(),
    val opportunities: List<GeneratedInsight> = emptyList(),
    val threats: List<GeneratedInsight> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class SwotViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getProjectWithInsightsUseCase: GetProjectWithInsightsUseCase,
    private val generateSwotUseCase: GenerateSwotUseCase
) : ViewModel() {

    private val projectId: String = checkNotNull(savedStateHandle[Screen.ARG_PROJECT_ID])
    private val generationStarted = AtomicBoolean(false)

    private val _uiState = MutableStateFlow(SwotUiState(isLoading = true))
    val uiState: StateFlow<SwotUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getProjectWithInsightsUseCase(projectId).collect { pwi ->
                val list = pwi.swotItems
                if (list.isNotEmpty()) {
                    _uiState.value = splitSwot(list)
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
            if (pwi.swotItems.isEmpty() && generationStarted.compareAndSet(false, true)) {
                runGenerate(project)
            }
        }
    }

    private suspend fun runGenerate(project: Project) {
        _uiState.update { SwotUiState(isLoading = true, error = null) }
        val result = generateSwotUseCase(project)
        result.onFailure { e ->
            generationStarted.set(false)
            _uiState.update {
                SwotUiState(
                    isLoading = false,
                    error = e.message ?: "Generation failed"
                )
            }
        }
    }

    private fun splitSwot(items: List<GeneratedInsight>): SwotUiState {
        fun quad(q: SwotQuadrant) =
            items.filter { it.quadrant == q }.sortedBy { it.orderIndex }
        return SwotUiState(
            isLoading = false,
            strengths = quad(SwotQuadrant.STRENGTH),
            weaknesses = quad(SwotQuadrant.WEAKNESS),
            opportunities = quad(SwotQuadrant.OPPORTUNITY),
            threats = quad(SwotQuadrant.THREAT),
            error = null
        )
    }
}
