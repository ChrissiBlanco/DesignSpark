package com.designspark.ui.screens.projectdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.designspark.domain.model.InsightType
import com.designspark.domain.model.RiskLevel
import com.designspark.domain.usecase.GetProjectWithInsightsUseCase
import com.designspark.domain.usecase.SaveAnnotationUseCase
import com.designspark.ui.navigation.Screen
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PersonaUi(
    val name: String,
    val age: Int,
    val role: String,
    val goal: String,
    val frustration: String
)

data class MethodCardUi(
    val method: String,
    val whyThisFits: String,
    val estimatedTime: String
)

data class AssumptionUi(
    val assumption: String,
    val risk: String,
    val rationale: String
)

data class RecruitBriefUi(
    val whoToFind: String,
    val screenFor: String,
    val exclude: String
)

sealed interface InsightCardUi {
    val id: String
    val title: String
    val riskLevel: RiskLevel?

    data class Persona(
        override val id: String,
        override val title: String,
        val data: PersonaUi
    ) : InsightCardUi {
        override val riskLevel: RiskLevel? = null
    }

    data class MethodCard(
        override val id: String,
        override val title: String,
        val data: MethodCardUi
    ) : InsightCardUi {
        override val riskLevel: RiskLevel? = null
    }

    data class Assumption(
        override val id: String,
        override val title: String,
        override val riskLevel: RiskLevel?,
        val data: AssumptionUi
    ) : InsightCardUi

    data class RecruitBrief(
        override val id: String,
        override val title: String,
        val data: RecruitBriefUi
    ) : InsightCardUi {
        override val riskLevel: RiskLevel? = null
    }
}

data class ProjectDetailUiState(
    val projectTitle: String = "",
    val personas: List<InsightCardUi.Persona> = emptyList(),
    val methodCards: List<InsightCardUi.MethodCard> = emptyList(),
    val assumptions: List<InsightCardUi.Assumption> = emptyList(),
    val recruitBrief: InsightCardUi.RecruitBrief? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class ProjectDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getProjectWithInsightsUseCase: GetProjectWithInsightsUseCase,
    private val saveAnnotationUseCase: SaveAnnotationUseCase,
    private val gson: Gson
) : ViewModel() {

    private val projectId: String = checkNotNull(savedStateHandle[Screen.ARG_PROJECT_ID])

    val uiState: StateFlow<ProjectDetailUiState> = getProjectWithInsightsUseCase(projectId)
        .map { pwi ->
            val personas = pwi.insights.filter { it.type == InsightType.PERSONA }.map { insight ->
                InsightCardUi.Persona(
                    id = insight.id,
                    title = insight.title,
                    data = gson.fromJson(insight.content, PersonaUi::class.java)
                )
            }
            val methodCards = pwi.insights.filter { it.type == InsightType.METHOD_CARD }.map { insight ->
                InsightCardUi.MethodCard(
                    id = insight.id,
                    title = insight.title,
                    data = gson.fromJson(insight.content, MethodCardUi::class.java)
                )
            }
            val assumptions = pwi.insights.filter { it.type == InsightType.ASSUMPTION }.map { insight ->
                InsightCardUi.Assumption(
                    id = insight.id,
                    title = insight.title,
                    riskLevel = insight.riskLevel,
                    data = gson.fromJson(insight.content, AssumptionUi::class.java)
                )
            }
            val recruitBrief = pwi.insights.firstOrNull { it.type == InsightType.RECRUIT_BRIEF }?.let { insight ->
                InsightCardUi.RecruitBrief(
                    id = insight.id,
                    title = insight.title,
                    data = gson.fromJson(insight.content, RecruitBriefUi::class.java)
                )
            }
            ProjectDetailUiState(
                projectTitle = pwi.project.title,
                personas = personas,
                methodCards = methodCards,
                assumptions = assumptions,
                recruitBrief = recruitBrief,
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ProjectDetailUiState()
        )

    fun saveAnnotation(insightId: String, note: String) {
        viewModelScope.launch {
            saveAnnotationUseCase(insightId, note)
        }
    }
}
