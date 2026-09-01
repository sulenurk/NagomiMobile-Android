package com.sklabs.nagomi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sklabs.nagomi.data.local.FocusSessionEntity
import com.sklabs.nagomi.ui.statistics.DailyFocusBar
import com.sklabs.nagomi.ui.statistics.StatisticsSubjectOption
import com.sklabs.nagomi.ui.statistics.StatisticsViewModel
import com.sklabs.nagomi.ui.statistics.SubjectFocusStat
import com.sklabs.nagomi.ui.localization.LocalNagomiStrings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showClearDialog by rememberSaveable { mutableStateOf(false) }
    val strings = LocalNagomiStrings.current

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Spacer(Modifier.height(10.dp))
            Text(strings.text("statistics", "Statistics"), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                strings.text("statistics_subtitle", "Track your focus time and study routine."),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MetricCard(strings.text("focused_time", "Focus today"), formatHoursMinutes(state.todayFocusSeconds), Modifier.weight(1f))
                MetricCard(strings.text("sessions", "Sessions"), state.todayCompletedSessions.toString(), Modifier.weight(1f))
                MetricCard(strings.text("away", "Away"), formatHoursMinutes(state.todayAwaySeconds), Modifier.weight(1f), Color(0xFFF59E0B))
            }
        }

        item {
            StatisticsCard(strings.text("today_focus_distribution", "Today's focus distribution")) {
                DistributionRow(strings.text("study_plan", "Study Plan"), formatHoursMinutes(state.studyPlanSeconds), MaterialTheme.colorScheme.primary)
                DistributionRow(strings.text("pomodoro", "Pomodoro"), formatHoursMinutes(state.pomodoroSeconds), Color(0xFFF59E0B))
                DistributionRow(strings.text("total", "Total"), formatHoursMinutes(state.totalTodaySeconds), Color(0xFF22C55E), bold = true)
            }
        }

        item {
            StatisticsCard(strings.text("daily_goal", "Daily goal")) {
                Text(
                    "${(state.goalProgress * 100).toInt()}% · " +
                        "${formatHoursMinutes(state.todayFocusSeconds)} / ${formatHoursMinutes(state.dailyGoalMinutes * 60)}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                LinearProgressIndicator(
                    progress = { state.goalProgress },
                    modifier = Modifier.fillMaxWidth().height(10.dp),
                )
            }
        }

        item {
            StatisticsCard(strings.text("weekly_overview", "Weekly overview")) {
                SubjectFilter(
                    options = state.subjectOptions,
                    selectedId = state.selectedSubjectId,
                    onSelected = viewModel::selectSubject,
                )
                val selectedOption = state.subjectOptions.firstOrNull { it.id == state.selectedSubjectId }
                val selectedName = when (selectedOption?.id) {
                    "all" -> strings.text("all_subjects", "All subjects")
                    "subject_other" -> strings.text("other_subject", "Other")
                    else -> selectedOption?.name ?: strings.text("all_subjects", "All subjects")
                }
                Text(
                    strings.format("this_week_minutes_subject", "This week: {minutes} min · {subject}", "minutes" to state.weeklyTotalMinutes, "subject" to selectedName),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                WeeklyBarChart(state.weeklyBars)
            }
        }

        item {
            StatisticsCard(strings.text("subject_distribution", "Subject distribution")) {
                val totalSeconds = state.subjectStats.sumOf { it.seconds }
                Text(
                    strings.format("this_week_minutes", "This week: {minutes} min", "minutes" to totalSeconds / 60),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (state.subjectStats.isEmpty()) {
                    Text(strings.text("no_subject_statistics_this_week", "No subject statistics available this week."))
                } else {
                    state.subjectStats.forEach { subject ->
                        SubjectDistributionRow(subject, totalSeconds)
                    }
                }
            }
        }

        item {
            StatisticsCard(strings.text("today_recent_sessions", "Today's recent sessions")) {
                if (state.recentSessions.isEmpty()) {
                    Text(strings.text("no_completed_focus_sessions_today", "No completed focus sessions today."))
                }
            }
        }

        items(state.recentSessions, key = { it.id }) { session ->
            RecentSessionCard(session)
        }

        item {
            OutlinedButton(
                onClick = { showClearDialog = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(strings.text("clear_statistics", "Clear statistics"), color = MaterialTheme.colorScheme.error)
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(strings.text("clear_statistics", "Clear statistics")) },
            text = { Text(strings.text("clear_statistics_confirmation", "All focus history and statistics records will be permanently deleted.")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearDialog = false
                        viewModel.clearStatistics()
                    },
                ) { Text(strings.text("clear", "Clear"), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text(strings.text("cancel", "Cancel")) }
            },
        )
    }
}

@Composable
private fun MetricCard(
    label: String,
    value: String,
    modifier: Modifier,
    valueColor: Color = MaterialTheme.colorScheme.primary,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}

@Composable
private fun StatisticsCard(title: String, content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

@Composable
private fun DistributionRow(label: String, value: String, color: Color, bold: Boolean = false) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, modifier = Modifier.weight(1f), fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
        Text(value, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SubjectFilter(
    options: List<StatisticsSubjectOption>,
    selectedId: String,
    onSelected: (String) -> Unit,
) {
    val strings = LocalNagomiStrings.current
    var expanded by rememberSaveable { mutableStateOf(false) }
    val selectedOption = options.firstOrNull { it.id == selectedId }
    val selected = when (selectedOption?.id) {
        "all" -> strings.text("all_subjects", "All subjects")
        "subject_other" -> strings.text("other_subject", "Other")
        else -> selectedOption?.name ?: strings.text("all_subjects", "All subjects")
    }
    Box(Modifier.fillMaxWidth()) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(selected)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            when (option.id) {
                                "all" -> strings.text("all_subjects", "All subjects")
                                "subject_other" -> strings.text("other_subject", "Other")
                                else -> option.name
                            },
                        )
                    },
                    onClick = {
                        expanded = false
                        onSelected(option.id)
                    },
                )
            }
        }
    }
}

@Composable
private fun WeeklyBarChart(values: List<DailyFocusBar>) {
    val maxValue = values.maxOfOrNull { it.minutes }?.coerceAtLeast(1) ?: 1
    Row(
        modifier = Modifier.fillMaxWidth().height(190.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        values.forEach { value ->
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
            ) {
                Text(value.minutes.toString(), style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier.weight(1f).width(24.dp),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(value.minutes.toFloat() / maxValue)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)),
                    )
                }
                Spacer(Modifier.height(5.dp))
                Text(value.label, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun SubjectDistributionRow(subject: SubjectFocusStat, totalSeconds: Int) {
    val strings = LocalNagomiStrings.current
    val ratio = if (totalSeconds <= 0) 0f else subject.seconds.toFloat() / totalSeconds
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(10.dp).background(subject.color.toComposeColor(), CircleShape))
            Text(
                if (subject.id == "subject_other") strings.text("other_subject", "Other") else subject.name,
                modifier = Modifier.weight(1f).padding(start = 8.dp),
                fontWeight = FontWeight.SemiBold,
            )
            Text(strings.format("subject_distribution_value", "{minutes} min · {percent}%", "minutes" to subject.seconds / 60, "percent" to (ratio * 100).toInt()))
        }
        LinearProgressIndicator(
            progress = { ratio },
            modifier = Modifier.fillMaxWidth().height(7.dp),
            color = subject.color.toComposeColor(),
        )
    }
}

@Composable
private fun RecentSessionCard(session: FocusSessionEntity) {
    val strings = LocalNagomiStrings.current
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = RoundedCornerShape(15.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(13.dp)) {
            Row(Modifier.fillMaxWidth()) {
                Text(
                    if (session.source == "regular_pomodoro") strings.text("pomodoro", "Pomodoro") else session.taskTitle,
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Bold,
                )
                Text(formatSessionTime(session.completedAtMillis), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                strings.format(
                    "recent_session_detail",
                    "{source} · {focus} min focus · {away} min away",
                    "source" to if (session.source == "regular_pomodoro") strings.text("pomodoro", "Pomodoro") else strings.text("study_plan", "Study Plan"),
                    "focus" to session.durationSeconds / 60,
                    "away" to session.awaySeconds / 60,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

private fun formatHoursMinutes(seconds: Int): String {
    val totalMinutes = seconds.coerceAtLeast(0) / 60
    return "%02d:%02d".format(totalMinutes / 60, totalMinutes % 60)
}

private fun formatSessionTime(timeMillis: Long): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timeMillis))
