package com.designspark.ui.screens.competitorscan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.remember
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
fun CompetitorScanScreen(
    projectId: String,
    onNext: () -> Unit,
    onBack: () -> Unit,
    viewModel: CompetitorScanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val gson = remember { Gson() }
    val hasResults = uiState.competitors.isNotEmpty() ||
        uiState.marketGap != null ||
        uiState.painPoints.isNotEmpty()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.competitor_scan_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        bottomBar = {
            if (hasResults && !uiState.isLoading && uiState.error == null) {
                Column(Modifier.padding(16.dp)) {
                    Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.competitor_scan_next))
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading && !hasResults -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(
                            stringResource(R.string.competitor_scan_loading),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                uiState.error != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            uiState.error!!,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { viewModel.retry() }) {
                            Text(stringResource(R.string.action_retry))
                        }
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        if (uiState.competitors.isNotEmpty()) {
                            Text(
                                stringResource(R.string.competitor_section_competitors),
                                style = MaterialTheme.typography.titleMedium
                            )
                            uiState.competitors.forEach { insight ->
                                CompetitorCard(insight, gson)
                            }
                        }
                        uiState.marketGap?.let { gap ->
                            Text(
                                stringResource(R.string.competitor_section_market_gap),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(0.dp)
                            ) {
                                Text(
                                    gap.content,
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        if (uiState.painPoints.isNotEmpty()) {
                            Text(
                                stringResource(R.string.competitor_section_pain_points),
                                style = MaterialTheme.typography.titleMedium
                            )
                            uiState.painPoints.forEach { insight ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(0.dp)
                                ) {
                                    Column(Modifier.padding(16.dp)) {
                                        Text(
                                            insight.title,
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            insight.content,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.padding(bottom = 72.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CompetitorCard(insight: GeneratedInsight, gson: Gson) {
    val body = remember(insight.content) { parseCompetitor(gson, insight.content) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(insight.title, style = MaterialTheme.typography.titleSmall)
            if (body != null) {
                Spacer(Modifier.height(4.dp))
                Text(body.first, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(6.dp))
                Text(
                    stringResource(R.string.competitor_gap_label, body.second),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun parseCompetitor(gson: Gson, content: String): Pair<String, String>? {
    return try {
        val map = gson.fromJson(content, Map::class.java) ?: return null
        val d = map["description"]?.toString() ?: return null
        val w = map["weakness"]?.toString() ?: return null
        d to w
    } catch (_: Exception) {
        null
    }
}
