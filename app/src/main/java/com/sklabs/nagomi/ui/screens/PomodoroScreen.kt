package com.sklabs.nagomi.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sklabs.nagomi.timer.PomodoroViewModel
import com.sklabs.nagomi.timer.TimerMode
import com.sklabs.nagomi.data.model.AppSettings
import com.sklabs.nagomi.notifications.AlarmPlaybackService
import com.sklabs.nagomi.ui.localization.NagomiStrings
import com.sklabs.nagomi.ui.settings.SettingsViewModel
import androidx.compose.runtime.remember

@Composable
fun PomodoroScreen(
    viewModel: PomodoroViewModel = viewModel(),
    onTimerStartRequested: () -> Unit = {},
    settings: AppSettings,
    settingsViewModel: SettingsViewModel,
    settingsRequestKey: Int = 0,
    onSettingsRequestConsumed: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snapshot = state.snapshot
    val configuration = LocalConfiguration.current
    val context = LocalContext.current
    val strings = remember(context, settings.language) { NagomiStrings.load(context, settings.language) }
    val alarmState by AlarmPlaybackService.state.collectAsStateWithLifecycle()
    val isTablet = configuration.smallestScreenWidthDp >= 600
    var showSettings by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(settingsRequestKey) {
        if (settingsRequestKey > 0) {
            showSettings = true
            onSettingsRequestConsumed()
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val compactHeight = isTablet && maxHeight < 700.dp
        val timerSize = if (isTablet) {
            minOf(maxWidth - 40.dp, maxHeight - if (compactHeight) 190.dp else 220.dp)
                .coerceIn(180.dp, 410.dp)
        } else {
            270.dp
        }
        val timerFont = when {
            !isTablet -> 48.sp
            timerSize < 300.dp -> 48.sp
            timerSize < 360.dp -> 60.sp
            else -> 76.sp
        }
        val sectionGap = if (compactHeight) 10.dp else if (isTablet) 24.dp else 24.dp
        val verticalPadding = if (compactHeight) 6.dp else 16.dp
        val mainButtonSize = if (compactHeight) 56.dp else 64.dp
        val mainIconSize = if (compactHeight) 28.dp else 32.dp

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = verticalPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(timerSize)) {
                val track = MaterialTheme.colorScheme.surfaceVariant
                val progressColor = when (snapshot.mode) {
                    TimerMode.FOCUS -> MaterialTheme.colorScheme.primary
                    TimerMode.SHORT_BREAK -> androidx.compose.ui.graphics.Color(0xFF22C55E)
                    TimerMode.LONG_BREAK -> androidx.compose.ui.graphics.Color(0xFF3B82F6)
                }
                Canvas(Modifier.fillMaxSize()) {
                    val stroke = if (isTablet && !compactHeight) 18.dp.toPx() else 12.dp.toPx()
                    val inset = stroke / 2
                    drawArc(
                        color = track,
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
                    Text(formatTime(snapshot.remainingSeconds), fontSize = timerFont, fontWeight = FontWeight.Bold)
                    Text(modeText(snapshot.mode, strings), style = MaterialTheme.typography.titleMedium)
                    Text(
                        if (state.focusCount > 0) {
                            "#${(snapshot.completedFocusCount + 1).coerceAtMost(state.focusCount)} / ${state.focusCount}"
                        } else {
                            "#${snapshot.completedFocusCount + 1}"
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(sectionGap))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = viewModel::reset) {
                    Icon(Icons.Default.Refresh, contentDescription = strings.text("reset", "Reset"))
                }
                Spacer(Modifier.width(16.dp))
                FilledIconButton(
                    onClick = {
                        if (!snapshot.isRunning) onTimerStartRequested()
                        viewModel.startOrPause()
                    },
                    modifier = Modifier.size(mainButtonSize),
                ) {
                    Icon(
                        if (snapshot.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (snapshot.isRunning) strings.text("pause", "Pause") else strings.text("start", "Start"),
                        modifier = Modifier.size(mainIconSize),
                    )
                }
                Spacer(Modifier.width(16.dp))
                IconButton(onClick = viewModel::skip) {
                    Icon(Icons.Default.SkipNext, contentDescription = strings.text("skip", "Skip"))
                }
            }
            Spacer(Modifier.height(if (compactHeight) 8.dp else 18.dp))
            if (alarmState.active) {
                StopAlarmButton(
                    label = strings.text("stop_alarm", "Stop alarm"),
                    onClick = { AlarmPlaybackService.stop(context) },
                )
            } else {
                Text(strings.status(state.status), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }

    if (showSettings) {
        PomodoroSettingsPanel(
            settings = settings,
            viewModel = settingsViewModel,
            onDismiss = { showSettings = false },
        )
    }
}

private fun formatTime(totalSeconds: Int): String = "%02d:%02d".format(
    totalSeconds / 60,
    totalSeconds % 60,
)

private fun modeText(mode: TimerMode, strings: NagomiStrings): String = when (mode) {
    TimerMode.FOCUS -> strings.text("focus_mode", "Focus")
    TimerMode.SHORT_BREAK -> strings.text("short_break_mode", "Short Break")
    TimerMode.LONG_BREAK -> strings.text("long_break_mode", "Long Break")
}
