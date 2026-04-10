package com.designspark.ui.screens.userinterviews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.designspark.R
import com.designspark.domain.model.GeneratedInsight
import com.google.gson.Gson

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserInterviewsScreen(
    @Suppress("UNUSED_PARAMETER") projectId: String,
    onNext: () -> Unit,
    onBack: () -> Unit,
    viewModel: UserInterviewsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val gson = remember { Gson() }
    val hasResults = uiState.interviews.isNotEmpty()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.user_interviews_title)) },
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
                        Text(stringResource(R.string.user_interviews_next))
                    }
                }
            }
        }
    ) { padding ->
        Box(
            Modifier
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
                            stringResource(R.string.user_interviews_loading),
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
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Button(onClick = { viewModel.retry() }) {
                            Text(stringResource(R.string.action_retry))
                        }
                    }
                }
                else -> {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        uiState.interviews.forEach { insight ->
                            InterviewCard(insight, gson)
                        }
                        Spacer(Modifier.height(72.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun InterviewCard(insight: GeneratedInsight, gson: Gson) {
    val parts = remember(insight.title) { insight.title.split(" — ", limit = 2) }
    val name = parts.getOrNull(0)?.trim().orEmpty()
    val sentiment = parts.getOrNull(1)?.trim()?.uppercase().orEmpty()
    val fields = remember(insight.content) { parseInterviewJson(gson, insight.content) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "$name${if (fields != null) " · ${fields.role}" else ""}",
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(Modifier.height(8.dp))
            SentimentChip(sentiment)
            Spacer(Modifier.height(8.dp))
            fields?.reaction?.let { r ->
                Text(r, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
            }
            fields?.quote?.let { q ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            RoundedCornerShape(8.dp)
                        )
                ) {
                    Box(
                        Modifier
                            .width(4.dp)
                            .fillMaxHeight()
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)
                            )
                    )
                    Text(
                        q,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic)
                    )
                }
            }
        }
    }
}

@Composable
private fun SentimentChip(sentiment: String) {
    val (container, labelColor) = when (sentiment) {
        "ENTHUSIASTIC" -> Color(0xFFE3F4E7) to Color(0xFF1B5E20)
        "SKEPTICAL" -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    AssistChip(
        onClick = {},
        label = { Text(sentiment) },
        enabled = false,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = container,
            labelColor = labelColor,
            disabledContainerColor = container,
            disabledLabelColor = labelColor
        ),
        border = null
    )
}

private data class InterviewFields(val role: String, val reaction: String, val quote: String)

private fun parseInterviewJson(gson: Gson, content: String): InterviewFields? {
    return try {
        val m = gson.fromJson(content, Map::class.java) ?: return null
        InterviewFields(
            role = m["role"]?.toString().orEmpty(),
            reaction = m["reaction"]?.toString().orEmpty(),
            quote = m["quote"]?.toString().orEmpty()
        )
    } catch (_: Exception) {
        null
    }
}
