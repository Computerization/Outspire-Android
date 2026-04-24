package com.computerization.outspire.feature.academic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.computerization.outspire.data.model.DomainScore
import com.computerization.outspire.data.remote.dto.YearOption
import com.computerization.outspire.designsystem.AppRadius
import com.computerization.outspire.designsystem.AppSpace
import com.computerization.outspire.designsystem.OutspireScreen
import com.computerization.outspire.designsystem.coloredRichCard

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AcademicScreen(
    viewModel: AcademicViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    val pullRefreshState = rememberPullRefreshState(
        refreshing = state.loading,
        onRefresh = viewModel::refresh,
    )

    OutspireScreen(
        title = "Academic",
        onRefresh = viewModel::refresh,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pullRefresh(pullRefreshState),
        ) {
            var expanded by remember { mutableStateOf(false) }
            val selectedLabel = state.yearOptions
                .firstOrNull { it.id == state.selectedYearId }?.name
                ?: "Select term"

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = AppSpace.md, vertical = AppSpace.xs),
                verticalArrangement = Arrangement.spacedBy(AppSpace.cardSpacing),
            ) {
                TermPickerCard(
                    selectedLabel = selectedLabel,
                    expanded = expanded,
                    yearOptions = state.yearOptions,
                    onExpandedChange = { expanded = !expanded },
                    onDismissRequest = { expanded = false },
                    onSelect = {
                        viewModel.selectYear(it)
                        expanded = false
                    },
                )

                when {
                    state.loading -> LoadingCard()
                    state.error != null -> ErrorCard(
                        message = state.error ?: "",
                        onRetry = viewModel::retry,
                    )
                    state.scores.isEmpty() -> EmptyCard()
                    else -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(AppSpace.cardSpacing),
                        ) {
                            items(state.scores, key = { it.subject }) { score ->
                                ScoreCard(score)
                            }
                        }
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = state.loading,
                state = pullRefreshState,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = AppSpace.xs),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TermPickerCard(
    selectedLabel: String,
    expanded: Boolean,
    yearOptions: List<YearOption>,
    onExpandedChange: () -> Unit,
    onDismissRequest: () -> Unit,
    onSelect: (String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadius.card),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpace.md, vertical = AppSpace.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpace.sm),
        ) {
            Surface(
                shape = RoundedCornerShape(AppRadius.sm),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(6.dp),
                )
            }
            Text(
                text = "Term",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { onExpandedChange() },
                modifier = Modifier.weight(1f),
            ) {
                OutlinedTextField(
                    value = selectedLabel,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium,
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = onDismissRequest,
                ) {
                    yearOptions.forEach { opt ->
                        DropdownMenuItem(
                            text = { Text(opt.name) },
                            onClick = { onSelect(opt.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadius.card),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpace.xl),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun ErrorCard(
    message: String,
    onRetry: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadius.card),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(AppSpace.cardPadding),
            verticalArrangement = Arrangement.spacedBy(AppSpace.sm),
        ) {
            Text(
                text = "Could not load scores",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(onClick = onRetry) { Text("Retry") }
        }
    }
}

@Composable
private fun EmptyCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadius.card),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(AppSpace.cardPadding),
            verticalArrangement = Arrangement.spacedBy(AppSpace.xs),
        ) {
            Text(
                text = "No scores for this term",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Try another term from the selector above.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ScoreCard(score: DomainScore) {
    val palette = remember(score.subject) { subjectPalette(score.subject) }

    Column(
        modifier = Modifier.coloredRichCard(
            colors = listOf(palette.start, palette.end),
            cornerRadius = AppRadius.card,
            shadowRadius = 10.dp,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpace.cardPadding),
            verticalArrangement = Arrangement.spacedBy(AppSpace.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(AppSpace.xxs),
                ) {
                    Text(
                        text = score.subject,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${score.terms.size} term entries",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.76f),
                    )
                }
                Surface(
                    shape = RoundedCornerShape(AppRadius.lg),
                    color = Color.White.copy(alpha = 0.14f),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = AppSpace.sm, vertical = AppSpace.xs),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppSpace.xs),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Palette,
                            contentDescription = null,
                            tint = Color.White,
                        )
                        Text(
                            text = palette.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                        )
                    }
                }
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpace.sm),
                verticalArrangement = Arrangement.spacedBy(AppSpace.sm),
                maxItemsInEachRow = 2,
            ) {
                score.terms.forEach { term ->
                    TermScoreTile(
                        term = term,
                        modifier = Modifier.weight(1f, fill = true),
                    )
                }
            }
        }
    }
}

@Composable
private fun TermScoreTile(
    term: DomainScore.TermScore,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(AppRadius.lg),
        color = Color.White.copy(alpha = 0.16f),
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(AppRadius.lg))
                .background(Color.Black.copy(alpha = 0.08f))
                .padding(AppSpace.md),
            verticalArrangement = Arrangement.spacedBy(AppSpace.sm),
        ) {
            Text(
                text = term.label,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.84f),
            )
            ScoreMetric(
                label = "Raw",
                value = term.raw.ifBlank { "-" },
            )
            ScoreMetric(
                label = "IB",
                value = term.ib.ifBlank { "-" },
            )
        }
    }
}

@Composable
private fun ScoreMetric(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.68f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
        )
    }
}

private data class SubjectPalette(
    val start: Color,
    val end: Color,
    val label: String,
)

private val SubjectPalettes = listOf(
    SubjectPalette(Color(0xFF3A7BFF), Color(0xFF74B6FF), "Sky"),
    SubjectPalette(Color(0xFF0F8C6C), Color(0xFF47C8A2), "Mint"),
    SubjectPalette(Color(0xFF8B5CF6), Color(0xFFB794F4), "Iris"),
    SubjectPalette(Color(0xFFE85D75), Color(0xFFFF9A7A), "Coral"),
    SubjectPalette(Color(0xFF9A6C1F), Color(0xFFF2B94B), "Amber"),
    SubjectPalette(Color(0xFF165D86), Color(0xFF4AB0D9), "Ocean"),
    SubjectPalette(Color(0xFF5E4AE3), Color(0xFF8A7BFF), "Indigo"),
    SubjectPalette(Color(0xFFB6476B), Color(0xFFFF8CB3), "Rose"),
)

private fun subjectPalette(subject: String): SubjectPalette {
    val index = (subject.lowercase().hashCode() and Int.MAX_VALUE) % SubjectPalettes.size
    return SubjectPalettes[index]
}
