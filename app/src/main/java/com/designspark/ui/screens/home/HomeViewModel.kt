package com.designspark.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.designspark.domain.model.Project
import com.designspark.domain.usecase.DeleteProjectUseCase
import com.designspark.domain.usecase.GetProjectsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val projects: List<Project> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getProjectsUseCase: GetProjectsUseCase,
    private val deleteProjectUseCase: DeleteProjectUseCase
) : ViewModel() {

    private val hiddenIds = MutableStateFlow<Set<String>>(emptySet())

    val uiState: StateFlow<HomeUiState> = combine(
        getProjectsUseCase(),
        hiddenIds
    ) { projects, hidden ->
        HomeUiState(
            projects = projects.filter { it.id !in hidden },
            isLoading = false,
            error = null
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(isLoading = true)
    )

    fun hideProjectForUndo(projectId: String) {
        hiddenIds.update { it + projectId }
    }

    fun unhideProject(projectId: String) {
        hiddenIds.update { it - projectId }
    }

    fun confirmDelete(projectId: String) {
        viewModelScope.launch {
            try {
                deleteProjectUseCase(projectId)
            } finally {
                hiddenIds.update { it - projectId }
            }
        }
    }
}
