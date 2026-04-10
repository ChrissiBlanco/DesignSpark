package com.designspark.ui.screens.summary

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.designspark.R
import com.designspark.domain.model.GeneratedInsight
import com.google.gson.Gson

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    @Suppress("UNUSED_PARAMETER") projectId: String,
    onContinueToStage2: () -> Unit,
    onBack: () -> Unit,
    viewModel: SummaryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val gson = remember { Gson() }
    val pwi = uiState.projectWithInsights
    val showContent = !uiState.isLoading && pwi != null

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.summary_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        bottomBar = {
            if (showContent) {
                Column(Modifier.padding(16.dp)) {
                    Button(onClick = onContinueToStage2, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.summary_continue_stage2))
                    }
                }
            }
        }
    ) { padding ->
        if (!showContent) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(48.dp))
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        val data = checkNotNull(pwi)
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryExpandableSection(
                title = stringResource(R.string.summary_section_competitors),
                initiallyExpanded = false
            ) {
                data.competitors.forEach { insight ->
                    Text(
                        formatCompetitorSummaryLine(insight, gson),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
            SummaryExpandableSection(
                title = stringResource(R.string.summary_section_interviews),
                initiallyExpanded = false
            ) {
                data.interviews.forEach { insight ->
                    Text(
                        "${insight.title}: ${insight.content}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
            SummaryExpandableSection(
                title = stringResource(R.string.summary_section_swot),
                initiallyExpanded = false
            ) {
                data.swotItems
                    .sortedBy { it.orderIndex }
                    .forEach { item ->
                        Text(
                            "• ${item.quadrant?.name}: ${item.content}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
            }
            Spacer(Modifier.height(72.dp))
        }
    }
}

@Composable
private fun SummaryExpandableSection(
    title: String,
    initiallyExpanded: Boolean,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column(Modifier.padding(top = 8.dp)) {
                    content()
                }
            }
        }
    }
}

private fun formatCompetitorSummaryLine(insight: GeneratedInsight, gson: Gson): String {
    if (insight.title == "Market gap") return "Market gap: ${insight.content}"
    val map = try {
        gson.fromJson(insight.content, Map::class.java)
    } catch (_: Exception) {
        null
    }
    return if (map != null && map.containsKey("description")) {
        "${insight.title}: ${map["description"]}"
    } else {
        "${insight.title}: ${insight.content}"
    }
}
