package com.designspark.ui.screens.stage1

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.designspark.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Stage1Screen(
    @Suppress("UNUSED_PARAMETER") projectId: String,
    onNavigateToCompetitors: () -> Unit,
    onNavigateToInterviews: () -> Unit,
    onNavigateToSwot: () -> Unit,
    onNavigateToSummary: () -> Unit,
    onBack: () -> Unit,
    viewModel: Stage1ViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val allComplete =
        uiState.competitorsComplete && uiState.interviewsComplete && uiState.swotComplete

    fun firstIncompleteAction() {
        when {
            !uiState.competitorsComplete -> onNavigateToCompetitors()
            !uiState.interviewsComplete -> onNavigateToInterviews()
            !uiState.swotComplete -> onNavigateToSwot()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(uiState.project?.title.orEmpty()) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        bottomBar = {
            Column(Modifier.padding(16.dp)) {
                Button(
                    onClick = {
                        if (allComplete) {
                            onNavigateToSummary()
                        } else {
                            firstIncompleteAction()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        when {
                            allComplete -> stringResource(R.string.stage1_cta_view_summary)
                            else -> stringResource(R.string.stage1_cta_start_continue)
                        }
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.stage1_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            StepCard(
                stepNumber = 1,
                name = stringResource(R.string.stage1_step_competitors_title),
                description = stringResource(R.string.stage1_step_competitors_desc),
                complete = uiState.competitorsComplete
            )
            StepCard(
                stepNumber = 2,
                name = stringResource(R.string.stage1_step_interviews_title),
                description = stringResource(R.string.stage1_step_interviews_desc),
                complete = uiState.interviewsComplete
            )
            StepCard(
                stepNumber = 3,
                name = stringResource(R.string.stage1_step_swot_title),
                description = stringResource(R.string.stage1_step_swot_desc),
                complete = uiState.swotComplete
            )
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun StepCard(
    stepNumber: Int,
    name: String,
    description: String,
    complete: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "$stepNumber.",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                if (complete) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(
                        Icons.Outlined.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
