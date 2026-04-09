package com.designspark.ui.screens.projectdetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.designspark.domain.model.RiskLevel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    projectId: String,
    onNavigateBack: () -> Unit,
    viewModel: ProjectDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Personas", "Methods", "Assumptions", "Recruit")

    val expandedAnnotations = remember { mutableStateMapOf<String, Boolean>() }
    val annotationDrafts = remember { mutableStateMapOf<String, String>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.projectTitle.ifEmpty { "Project" }) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            ScrollableTabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            if (uiState.isLoading) {
                ShimmerInsightList()
                return@Scaffold
            }

            when (selectedTab) {
                0 -> InsightList(
                    items = uiState.personas,
                    expandedAnnotations = expandedAnnotations,
                    annotationDrafts = annotationDrafts,
                    onSaveAnnotation = viewModel::saveAnnotation
                ) { PersonaCardContent(it.data) }

                1 -> InsightList(
                    items = uiState.methodCards,
                    expandedAnnotations = expandedAnnotations,
                    annotationDrafts = annotationDrafts,
                    onSaveAnnotation = viewModel::saveAnnotation
                ) { MethodCardContent(it.data) }

                2 -> InsightList(
                    items = uiState.assumptions,
                    expandedAnnotations = expandedAnnotations,
                    annotationDrafts = annotationDrafts,
                    onSaveAnnotation = viewModel::saveAnnotation
                ) { AssumptionCardContent(it) }

                3 -> {
                    val brief = uiState.recruitBrief
                    if (brief != null) {
                        InsightList(
                            items = listOf(brief),
                            expandedAnnotations = expandedAnnotations,
                            annotationDrafts = annotationDrafts,
                            onSaveAnnotation = viewModel::saveAnnotation
                        ) { RecruitBriefContent(it.data) }
                    } else {
                        EmptyInsightsState()
                    }
                }
            }
        }
    }
}

@Composable
private fun ShimmerInsightList() {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha))
            )
        }
    }
}

@Composable
private fun EmptyInsightsState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "No insights yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Try regenerating from the home screen",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun <T : InsightCardUi> InsightList(
    items: List<T>,
    expandedAnnotations: MutableMap<String, Boolean>,
    annotationDrafts: MutableMap<String, String>,
    onSaveAnnotation: (String, String) -> Unit,
    content: @Composable (T) -> Unit
) {
    if (items.isEmpty()) {
        EmptyInsightsState()
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items, key = { it.id }) { item ->
            InsightCard(
                item = item,
                isAnnotationExpanded = expandedAnnotations[item.id] == true,
                annotationDraft = annotationDrafts[item.id] ?: "",
                onToggleAnnotation = {
                    expandedAnnotations[item.id] = !(expandedAnnotations[item.id] ?: false)
                },
                onDraftChange = { annotationDrafts[item.id] = it },
                onSaveAnnotation = { onSaveAnnotation(item.id, annotationDrafts[item.id] ?: "") }
            ) { content(item) }
        }
    }
}

@Composable
private fun InsightCard(
    item: InsightCardUi,
    isAnnotationExpanded: Boolean,
    annotationDraft: String,
    onToggleAnnotation: () -> Unit,
    onDraftChange: (String) -> Unit,
    onSaveAnnotation: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    item.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                item.riskLevel?.let { RiskBadge(it) }
            }
            Spacer(Modifier.height(8.dp))
            content()
            Spacer(Modifier.height(4.dp))
            TextButton(onClick = onToggleAnnotation) {
                Text(if (isAnnotationExpanded) "Hide note" else "+ Add note")
            }
            AnimatedVisibility(visible = isAnnotationExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = annotationDraft,
                        onValueChange = onDraftChange,
                        label = { Text("Note") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                    Button(
                        onClick = onSaveAnnotation,
                        enabled = annotationDraft.isNotBlank(),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
private fun RiskBadge(risk: RiskLevel) {
    val (label, color) = when (risk) {
        RiskLevel.HIGH -> "HIGH" to MaterialTheme.colorScheme.errorContainer
        RiskLevel.MEDIUM -> "MEDIUM" to MaterialTheme.colorScheme.tertiaryContainer
        RiskLevel.LOW -> "LOW" to MaterialTheme.colorScheme.secondaryContainer
    }
    Surface(color = color, shape = MaterialTheme.shapes.small) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun PersonaCardContent(data: PersonaUi) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Age: ${data.age}", style = MaterialTheme.typography.bodySmall)
        Text("Role: ${data.role}", style = MaterialTheme.typography.bodySmall)
        Text("Goal: ${data.goal}", style = MaterialTheme.typography.bodySmall)
        Text("Frustration: ${data.frustration}", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun MethodCardContent(data: MethodCardUi) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(data.whyThisFits, style = MaterialTheme.typography.bodySmall)
        Text("Time: ${data.estimatedTime}", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun AssumptionCardContent(item: InsightCardUi.Assumption) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(item.data.assumption, style = MaterialTheme.typography.bodySmall)
        Text("Rationale: ${item.data.rationale}", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun RecruitBriefContent(data: RecruitBriefUi) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Who to find: ${data.whoToFind}", style = MaterialTheme.typography.bodySmall)
        Text("Screen for: ${data.screenFor}", style = MaterialTheme.typography.bodySmall)
        Text("Exclude: ${data.exclude}", style = MaterialTheme.typography.bodySmall)
    }
}
