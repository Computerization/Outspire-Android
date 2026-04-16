package com.computerization.outspire.feature.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.computerization.outspire.designsystem.AppSpace
import com.computerization.outspire.designsystem.OutspireScreen
import com.computerization.outspire.designsystem.staggeredEntry
import com.computerization.outspire.feature.today.components.NoClassCard
import com.computerization.outspire.feature.today.components.QuickLinksCard
import com.computerization.outspire.feature.today.components.UnifiedScheduleCard
import com.computerization.outspire.feature.today.components.WeatherBadge
import com.computerization.outspire.feature.today.components.WeekendCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TodayScreen(
    onNavigate: (String) -> Unit = {},
    viewModel: TodayViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val refreshing by viewModel.refreshing.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var animateCards by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animateCards = true }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = viewModel::refresh,
    )

    OutspireScreen(
        title = "Today",
        snackbarHostState = snackbarHostState,
        onRefresh = viewModel::refresh,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pullRefresh(pullRefreshState),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = AppSpace.md, vertical = AppSpace.lg),
                verticalArrangement = Arrangement.spacedBy(AppSpace.lg),
            ) {
                WeatherBadge(modifier = Modifier.align(Alignment.Start))

                when (val s = state) {
                    TodayUiState.Loading -> {
                        Text("Loading…", color = MaterialTheme.colorScheme.onBackground)
                    }
                    is TodayUiState.Weekday -> {
                        UnifiedScheduleCard(
                            dayName = s.dayName,
                            classes = s.classes,
                            activeIndex = s.activeIndex,
                            nowLocal = s.now,
                            modifier = Modifier.staggeredEntry(0, animateCards),
                        )
                    }
                    is TodayUiState.DayDone -> {
                        if (s.isWeekend) {
                            WeekendCard(modifier = Modifier.staggeredEntry(0, animateCards))
                        } else {
                            NoClassCard(
                                isDimmed = s.isAfterSchool,
                                modifier = Modifier.staggeredEntry(0, animateCards),
                            )
                        }
                    }
                    is TodayUiState.Error -> {
                        Column(verticalArrangement = Arrangement.spacedBy(AppSpace.sm)) {
                            Text(
                                text = "Couldn't load timetable: ${s.message}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                            )
                            Text(
                                text = "Pull to refresh or tap the refresh icon.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                QuickLinksCard(
                    modifier = Modifier.staggeredEntry(1, animateCards),
                    onClubs = { onNavigate("cas") },
                    onDining = {
                        scope.launch { snackbarHostState.showSnackbar("Dining · coming soon") }
                    },
                    onActivities = { onNavigate("cas") },
                    onReflect = { onNavigate("cas") },
                )
            }

            PullRefreshIndicator(
                refreshing = refreshing,
                state = pullRefreshState,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = AppSpace.xs),
            )
        }
    }
}
