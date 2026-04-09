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
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getProjectsUseCase: GetProjectsUseCase,
    private val deleteProjectUseCase: DeleteProjectUseCase
) : ViewModel() {

    // IDs currently soft-deleted (hidden from list, not yet removed from DB)
    private val _deletingIds = MutableStateFlow<Set<String>>(emptySet())

    val uiState: StateFlow<HomeUiState> = combine(
        getProjectsUseCase(),
        _deletingIds
    ) { projects, deletingIds ->
        HomeUiState(
            projects = projects.filter { it.id !in deletingIds },
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )

    /** Hide project from list immediately; wait for [confirmDelete] or [undoDelete]. */
    fun requestDelete(projectId: String) {
        _deletingIds.update { it + projectId }
    }

    /** Re-show the project — user pressed Undo. */
    fun undoDelete(projectId: String) {
        _deletingIds.update { it - projectId }
    }

    /** Actually delete from DB — snackbar timed out without Undo. */
    fun confirmDelete(projectId: String) {
        viewModelScope.launch {
            deleteProjectUseCase(projectId)
            _deletingIds.update { it - projectId }
        }
    }
}
