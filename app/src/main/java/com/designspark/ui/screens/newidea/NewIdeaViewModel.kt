package com.designspark.ui.screens.newidea

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.designspark.domain.usecase.CreateProjectUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NewIdeaUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val navigateToProjectId: String? = null
)

@HiltViewModel
class NewIdeaViewModel @Inject constructor(
    private val createProjectUseCase: CreateProjectUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewIdeaUiState())
    val uiState: StateFlow<NewIdeaUiState> = _uiState.asStateFlow()

    fun submit(title: String, description: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = createProjectUseCase(title, description)
            result.fold(
                onSuccess = { project ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            navigateToProjectId = project.id,
                            error = null
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "Something went wrong",
                            navigateToProjectId = null
                        )
                    }
                }
            )
        }
    }

    fun consumeNavigation() {
        _uiState.update { it.copy(navigateToProjectId = null) }
    }
}
