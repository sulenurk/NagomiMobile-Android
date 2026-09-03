package com.sklabs.nagomi.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sklabs.nagomi.data.local.TaskWithSubject
import com.sklabs.nagomi.data.model.AppSettings
import com.sklabs.nagomi.notifications.AlarmPlaybackService
import com.sklabs.nagomi.ui.focus.FocusPhase
import com.sklabs.nagomi.ui.focus.FocusUiState
import com.sklabs.nagomi.ui.focus.FocusViewModel
import com.sklabs.nagomi.ui.localization.NagomiStrings
import com.sklabs.nagomi.ui.localization.LocalNagomiStrings
import com.sklabs.nagomi.ui.settings.SettingsViewModel

@Composable
fun FocusScreen(
    onOpenStudyPlan: () -> Unit,
    viewModel: FocusViewModel,
    onTimerStartRequested: () -> Unit,
    settings: AppSettings,
    settingsViewModel: SettingsViewModel,
    settingsRequestKey: Int = 0,
    onSettingsRequestConsumed: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val configuration = LocalConfiguration.current
    val context = LocalContext.current
    val strings = remember(context, settings.language) { NagomiStrings.load(context, settings.language) }
    val alarmState by AlarmPlaybackService.state.collectAsStateWithLifecycle()
    val isTablet = configuration.smallestScreenWidthDp >= 600
    val timerSize = if (isTablet) 360.dp else 250.dp
    val timerFont = if (isTablet) 68.sp else 46.sp
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var queueExpanded by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(settingsRequestKey) {
        if (settingsRequestKey > 0) {
            showSettings = true
            onSettingsRequestConsumed()
        }
    }

    if (!state.loading && state.currentTask == null) {
        EmptyFocusState(
            completed = state.planCompleted,
            status = state.status,
            onOpenStudyPlan = onOpenStudyPlan,
            alarmActive = alarmState.active,
            stopAlarmLabel = strings.text("stop_alarm", "Stop alarm"),
            onStopAlarm = { AlarmPlaybackService.stop(context) },
        )
    } else BoxWithConstraints(Modifier.fillMaxSize()) {
        val isTabletLandscape = isTablet && maxWidth > maxHeight
        if (isTabletLandscape) {
            TabletLandscapeFocusContent(
                state = state,
                settings = settings,
                strings = strings,
                alarmActive = alarmState.active,
                onStopAlarm = { AlarmPlaybackService.stop(context) },
                onReset = viewModel::resetPhase,
                onStartOrPause = {
                    if (!state.isRunning) onTimerStartRequested()
                    viewModel.startOrPause()
                },
                onCompleteOrSkip = viewModel::completeOrSkipPhase,
                onStopPlan = viewModel::stopPlan,
                availableHeight = maxHeight,
                queueExpanded = queueExpanded,
                onToggleQueue = { queueExpanded = !queueExpanded },
            )
        } else LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (state.phase == FocusPhase.FOCUS) strings.text("focus_session", "Focus Session") else strings.text("break_mode", "Break Time"),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        item {
            state.currentTask?.let { task -> CurrentTaskCard(task, state.phase) }
        }

        item {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(timerSize)) {
                val trackColor = MaterialTheme.colorScheme.surfaceVariant
                val progressColor = if (state.phase == FocusPhase.FOCUS) {
                    MaterialTheme.colorScheme.primary
                } else {
                    Color(0xFF22C55E)
                }
                Canvas(Modifier.fillMaxSize()) {
                    val stroke = if (isTablet) 17.dp.toPx() else 12.dp.toPx()
                    val inset = stroke / 2
                    drawArc(
                        color = trackColor,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = Size(size.width - stroke, size.height - stroke),
                        style = Stroke(stroke, cap = StrokeCap.Round),
                    )
                    drawArc(
                        color = progressColor,
                        startAngle = -90f,
                        sweepAngle = 360f * state.progress,
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = Size(size.width - stroke, size.height - stroke),
                        style = Stroke(stroke, cap = StrokeCap.Round),
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(formatFocusTime(state.remainingSeconds), fontSize = timerFont, fontWeight = FontWeight.Bold)
                    Text(
                        if (state.phase == FocusPhase.FOCUS) strings.text("focus_mode", "Focus") else strings.text("break_mode", "Break"),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    if (
                        state.phase == FocusPhase.FOCUS &&
                        state.awaySeconds > 0 &&
                        settings.showCumulativeAwayTime
                    ) {
                        Text(
                            "${strings.text("away", "Away")} ${formatFocusTime(state.awaySeconds)}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = viewModel::resetPhase) {
                    Icon(Icons.Default.Refresh, contentDescription = strings.text("reset", "Reset phase"))
                }
                Spacer(Modifier.width(16.dp))
                FilledIconButton(
                    onClick = {
                        if (!state.isRunning) onTimerStartRequested()
                        viewModel.startOrPause()
                    },
                    modifier = Modifier.size(64.dp),
                ) {
                    Icon(
                        if (state.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (state.isRunning) strings.text("pause", "Pause") else strings.text("start", "Start"),
                        modifier = Modifier.size(32.dp),
                    )
                }
                Spacer(Modifier.width(16.dp))
                IconButton(onClick = viewModel::completeOrSkipPhase) {
                    Icon(
                        if (state.phase == FocusPhase.FOCUS) Icons.Default.Check else Icons.Default.SkipNext,
                        contentDescription = if (state.phase == FocusPhase.FOCUS) strings.text("complete_task", "Complete task") else strings.text("skip_break", "Skip break"),
                    )
                }
            }
        }

        item {
            if (state.status.isNotBlank()) {
                Text(strings.status(state.status), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        if (alarmState.active) {
            item {
                StopAlarmButton(
                    label = strings.text("stop_alarm", "Stop alarm"),
                    onClick = { AlarmPlaybackService.stop(context) },
                )
            }
        }

        item {
            OutlinedButton(onClick = viewModel::stopPlan, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Close, contentDescription = null)
                Text(" ${strings.text("stop_plan", "Stop Plan")}")
            }
        }

        val nextTasks = state.queue.filter { it.task.id != state.currentTask?.task?.id }
        if (settings.showQueueProgress && nextTasks.isNotEmpty()) {
            item {
                UpNextHeader(
                    expanded = queueExpanded,
                    strings = strings,
                    onToggle = { queueExpanded = !queueExpanded },
                )
            }
            if (queueExpanded) {
                items(nextTasks, key = { it.task.id }) { task -> QueueTaskCard(task) }
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
        }
    }

    if (showSettings) {
        FocusSettingsPanel(
            settings = settings,
            viewModel = settingsViewModel,
            onDismiss = { showSettings = false },
        )
    }
}

@Composable
private fun TabletLandscapeFocusContent(
    state: FocusUiState,
    settings: AppSettings,
    strings: NagomiStrings,
    alarmActive: Boolean,
    onStopAlarm: () -> Unit,
    onReset: () -> Unit,
    onStartOrPause: () -> Unit,
    onCompleteOrSkip: () -> Unit,
    onStopPlan: () -> Unit,
    availableHeight: androidx.compose.ui.unit.Dp,
    queueExpanded: Boolean,
    onToggleQueue: () -> Unit,
) {
    val pageEdgeGap = 12.dp
    val taskToStopPlanGap = 8.dp
    val circleContentGap = 12.dp
    val statusToControlsGap = 6.dp
    val contentVerticalLift = 4.dp
    val timerReservedSpace = 242.dp
    val timerSize = (availableHeight - timerReservedSpace).coerceIn(160.dp, 340.dp)
    val timerFont = when {
        timerSize < 225.dp -> 40.sp
        timerSize < 285.dp -> 52.sp
        else -> 64.sp
    }
    val nextTasks = state.queue.filter { it.task.id != state.currentTask?.task?.id }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(availableHeight)
                    .padding(horizontal = 20.dp, vertical = pageEdgeGap)
                    .offset(y = -contentVerticalLift),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                state.currentTask?.let { task ->
                    Text(
                        text = task.task.title,
                        modifier = Modifier.fillMaxWidth(0.82f),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = strings.format(
                            "focus_minutes_detail",
                            "{minutes} min focus",
                            "minutes" to task.task.focusDurationMinutes,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(taskToStopPlanGap))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (alarmActive) {
                            StopAlarmButton(
                                label = strings.text("stop_alarm", "Stop alarm"),
                                onClick = onStopAlarm,
                            )
                        }
                        OutlinedButton(onClick = onStopPlan) {
                            Icon(Icons.Default.Close, contentDescription = null)
                            Text(" ${strings.text("stop_plan", "Stop Plan")}")
                        }
                    }
                }

                Spacer(Modifier.height(circleContentGap))
                FocusTimerDial(
                    state = state,
                    settings = settings,
                    strings = strings,
                    timerSize = timerSize,
                    timerFont = timerFont,
                    strokeWidth = if (timerSize < 240.dp) 11.dp else 15.dp,
                )
                Spacer(Modifier.height(circleContentGap))
                Text(
                    text = strings.status(state.status).ifBlank {
                        strings.text("focus_ready", "Ready to focus")
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(Modifier.height(statusToControlsGap))
                FocusTimerControls(
                    state = state,
                    strings = strings,
                    onReset = onReset,
                    onStartOrPause = onStartOrPause,
                    onCompleteOrSkip = onCompleteOrSkip,
                    mainButtonSize = if (timerSize < 225.dp) 52.dp else 58.dp,
                )
            }
        }

        if (settings.showQueueProgress && nextTasks.isNotEmpty()) {
            item {
                UpNextHeader(
                    expanded = queueExpanded,
                    strings = strings,
                    onToggle = onToggleQueue,
                )
            }
            if (queueExpanded) {
                items(nextTasks, key = { it.task.id }) { task -> QueueTaskCard(task) }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun UpNextHeader(
    expanded: Boolean,
    strings: NagomiStrings,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            strings.text("up_next", "Up next"),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        IconButton(onClick = onToggle, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp
                else Icons.Default.KeyboardArrowDown,
                contentDescription = if (expanded) {
                    strings.text("collapse_queue", "Collapse queue")
                } else {
                    strings.text("expand_queue", "Expand queue")
                },
            )
        }
    }
}

@Composable
private fun FocusTimerDial(
    state: FocusUiState,
    settings: AppSettings,
    strings: NagomiStrings,
    timerSize: androidx.compose.ui.unit.Dp,
    timerFont: androidx.compose.ui.unit.TextUnit,
    strokeWidth: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
) {
    Box(contentAlignment = Alignment.Center, modifier = modifier.size(timerSize)) {
        val trackColor = MaterialTheme.colorScheme.surfaceVariant
        val progressColor = if (state.phase == FocusPhase.FOCUS) {
            MaterialTheme.colorScheme.primary
        } else {
            Color(0xFF22C55E)
        }
        Canvas(Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val inset = stroke / 2
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = Size(size.width - stroke, size.height - stroke),
                style = Stroke(stroke, cap = StrokeCap.Round),
            )
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360f * state.progress,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = Size(size.width - stroke, size.height - stroke),
                style = Stroke(stroke, cap = StrokeCap.Round),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(formatFocusTime(state.remainingSeconds), fontSize = timerFont, fontWeight = FontWeight.Bold)
            Text(
                if (state.phase == FocusPhase.FOCUS) strings.text("focus_mode", "Focus")
                else strings.text("break_mode", "Break"),
                style = MaterialTheme.typography.titleMedium,
            )
            if (
                state.phase == FocusPhase.FOCUS &&
                state.awaySeconds > 0 &&
                settings.showCumulativeAwayTime
            ) {
                Text(
                    "${strings.text("away", "Away")} ${formatFocusTime(state.awaySeconds)}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun FocusTimerControls(
    state: FocusUiState,
    strings: NagomiStrings,
    onReset: () -> Unit,
    onStartOrPause: () -> Unit,
    onCompleteOrSkip: () -> Unit,
    mainButtonSize: androidx.compose.ui.unit.Dp,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onReset) {
            Icon(Icons.Default.Refresh, contentDescription = strings.text("reset", "Reset phase"))
        }
        Spacer(Modifier.width(14.dp))
        FilledIconButton(onClick = onStartOrPause, modifier = Modifier.size(mainButtonSize)) {
            Icon(
                if (state.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (state.isRunning) strings.text("pause", "Pause")
                else strings.text("start", "Start"),
                modifier = Modifier.size(30.dp),
            )
        }
        Spacer(Modifier.width(14.dp))
        IconButton(onClick = onCompleteOrSkip) {
            Icon(
                if (state.phase == FocusPhase.FOCUS) Icons.Default.Check else Icons.Default.SkipNext,
                contentDescription = if (state.phase == FocusPhase.FOCUS) {
                    strings.text("complete_task", "Complete task")
                } else {
                    strings.text("skip_break", "Skip break")
                },
            )
        }
    }
}

@Composable
private fun EmptyFocusState(
    completed: Boolean,
    status: String,
    onOpenStudyPlan: () -> Unit,
    alarmActive: Boolean,
    stopAlarmLabel: String,
    onStopAlarm: () -> Unit,
) {
    val strings = LocalNagomiStrings.current
    Column(
        modifier = Modifier.fillMaxSize().padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            if (completed) Icons.Default.Check else Icons.Default.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            if (completed) strings.text("study_plan_completed", "Study plan completed") else strings.text("no_active_task", "No active task"),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            strings.status(status).ifBlank { strings.text("no_active_task_description", "Choose a task or start the full queue from Study Plan.") },
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(20.dp))
        Button(onClick = onOpenStudyPlan) { Text(strings.text("open_study_plan", "Open Study Plan")) }
        if (alarmActive) {
            StopAlarmButton(label = stopAlarmLabel, onClick = onStopAlarm)
        }
    }
}

@Composable
private fun CurrentTaskCard(task: TaskWithSubject, phase: FocusPhase) {
    val strings = LocalNagomiStrings.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(18.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(16.dp).background((task.subjectColor ?: "#A78BFA").toComposeColor(), CircleShape))
            Column(Modifier.padding(start = 12.dp)) {
                Text(
                    if (phase == FocusPhase.FOCUS) task.task.title
                    else strings.format("next_task", "Next: {task}", "task" to task.task.title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "${if (task.subjectIsDefault == true) strings.text("other_subject", "Other") else task.subjectName.orEmpty()} · " +
                        strings.format("focus_minutes_detail", "{minutes} min focus", "minutes" to task.task.focusDurationMinutes),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun QueueTaskCard(task: TaskWithSubject) {
    val strings = LocalNagomiStrings.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = RoundedCornerShape(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(11.dp).background((task.subjectColor ?: "#A78BFA").toComposeColor(), CircleShape))
            Text(task.task.title, modifier = Modifier.padding(start = 10.dp), fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            Text(
                strings.format("minutes_short", "{minutes} min", "minutes" to task.task.focusDurationMinutes),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

private fun formatFocusTime(totalSeconds: Int): String = "%02d:%02d".format(
    totalSeconds / 60,
    totalSeconds % 60,
)
