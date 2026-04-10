package com.designspark.ui.screens.competitorscan

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.designspark.domain.model.GeneratedInsight
import com.designspark.domain.model.Project
import com.designspark.domain.usecase.GenerateCompetitorsUseCase
import com.designspark.domain.usecase.GetProjectWithInsightsUseCase
import com.designspark.ui.navigation.Screen
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

data class CompetitorScanUiState(
    val isLoading: Boolean = false,
    val competitors: List<GeneratedInsight> = emptyList(),
    val marketGap: GeneratedInsight? = null,
    val painPoints: List<GeneratedInsight> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class CompetitorScanViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getProjectWithInsightsUseCase: GetProjectWithInsightsUseCase,
    private val generateCompetitorsUseCase: GenerateCompetitorsUseCase,
    private val gson: Gson
) : ViewModel() {

    private val projectId: String = checkNotNull(savedStateHandle[Screen.ARG_PROJECT_ID])
    private val generationStarted = AtomicBoolean(false)

    private val _uiState = MutableStateFlow(CompetitorScanUiState(isLoading = true))
    val uiState: StateFlow<CompetitorScanUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getProjectWithInsightsUseCase(projectId).collect { pwi ->
                val list = pwi.competitors
                if (list.isNotEmpty()) {
                    _uiState.value = splitCompetitors(list)
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
            if (pwi.competitors.isEmpty() && generationStarted.compareAndSet(false, true)) {
                runGenerate(project)
            }
        }
    }

    private suspend fun runGenerate(project: Project) {
        _uiState.update { CompetitorScanUiState(isLoading = true, error = null) }
        val result = generateCompetitorsUseCase(project)
        result.onFailure { e ->
            generationStarted.set(false)
            _uiState.update {
                CompetitorScanUiState(
                    isLoading = false,
                    error = e.message ?: "Generation failed"
                )
            }
        }
    }

    private fun splitCompetitors(all: List<GeneratedInsight>): CompetitorScanUiState {
        val marketGap = all.firstOrNull { it.title == "Market gap" }
        val competitorCards = all.filter { insight ->
            insight.title != "Market gap" && parseCompetitorJson(insight.content) != null
        }
        val painPoints = all.filter { insight ->
            insight.title != "Market gap" && parseCompetitorJson(insight.content) == null
        }
        return CompetitorScanUiState(
            isLoading = false,
            competitors = competitorCards,
            marketGap = marketGap,
            painPoints = painPoints,
            error = null
        )
    }

    private fun parseCompetitorJson(content: String): Map<*, *>? {
        return try {
            val map = gson.fromJson(content, Map::class.java) ?: return null
            if (!map.containsKey("description") || !map.containsKey("weakness")) return null
            map
        } catch (_: Exception) {
            null
        }
    }
}
