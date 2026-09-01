package com.sklabs.nagomi.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlarmOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sklabs.nagomi.data.model.AppSettings
import com.sklabs.nagomi.ui.localization.NagomiStrings
import com.sklabs.nagomi.ui.settings.SettingsViewModel

@Composable
internal fun StopAlarmButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError,
        ),
    ) {
        Icon(Icons.Default.AlarmOff, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(label)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PomodoroSettingsPanel(
    settings: AppSettings,
    viewModel: SettingsViewModel,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val strings = remember(context, settings.language) { NagomiStrings.load(context, settings.language) }
    val status by viewModel.status.collectAsStateWithLifecycle()
    var focusMinutes by rememberSaveable { mutableStateOf(settings.regularFocusMinutes.toString()) }
    var shortBreakMinutes by rememberSaveable { mutableStateOf(settings.regularShortBreakMinutes.toString()) }
    var longBreakMinutes by rememberSaveable { mutableStateOf(settings.regularLongBreakMinutes.toString()) }
    var longBreakAfter by rememberSaveable { mutableStateOf(settings.regularLongBreakAfter.toString()) }
    var focusCount by rememberSaveable { mutableStateOf(settings.regularFocusCount.toString()) }

    LaunchedEffect(
        settings.regularFocusMinutes,
        settings.regularShortBreakMinutes,
        settings.regularLongBreakMinutes,
        settings.regularLongBreakAfter,
        settings.regularFocusCount,
    ) {
        focusMinutes = settings.regularFocusMinutes.toString()
        shortBreakMinutes = settings.regularShortBreakMinutes.toString()
        longBreakMinutes = settings.regularLongBreakMinutes.toString()
        longBreakAfter = settings.regularLongBreakAfter.toString()
        focusCount = settings.regularFocusCount.toString()
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                strings.text("pomodoro_settings", "Pomodoro Settings"),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                strings.text("pomodoro_settings_description", "Customize Pomodoro durations and transitions."),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TimerNumberField(
                    strings.text("focus_duration_minutes", "Focus duration (min)"),
                    focusMinutes,
                    { focusMinutes = it },
                    Modifier.weight(1f),
                )
                TimerNumberField(
                    strings.text("short_break_minutes", "Short break (min)"),
                    shortBreakMinutes,
                    { shortBreakMinutes = it },
                    Modifier.weight(1f),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TimerNumberField(
                    strings.text("long_break_minutes", "Long break (min)"),
                    longBreakMinutes,
                    { longBreakMinutes = it },
                    Modifier.weight(1f),
                )
                TimerNumberField(
                    strings.text("long_break_interval", "Long break interval"),
                    longBreakAfter,
                    { longBreakAfter = it },
                    Modifier.weight(1f),
                )
            }
            TimerNumberField(
                strings.text("total_focus_cycles", "Total focus cycles"),
                focusCount,
                { focusCount = it },
                Modifier.fillMaxWidth(),
            )
            AutoStartControls(settings, viewModel, strings)
            Button(
                onClick = {
                    viewModel.savePomodoroSettings(
                        focusMinutes,
                        shortBreakMinutes,
                        longBreakMinutes,
                        longBreakAfter,
                        focusCount,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(strings.text("save", "Save")) }
            if (status.isNotBlank()) Text(strings.status(status), color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(18.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FocusSettingsPanel(
    settings: AppSettings,
    viewModel: SettingsViewModel,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val strings = remember(context, settings.language) { NagomiStrings.load(context, settings.language) }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                strings.text("focus_settings", "Focus Settings"),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                strings.text("focus_settings_description", "Customize focus and break transitions."),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AutoStartControls(settings, viewModel, strings)
            TimerSwitchRow(
                strings.text("show_total_away_time", "Show total away time"),
                strings.text("show_away_time_description", "Show cumulative background time during focus."),
                settings.showCumulativeAwayTime,
                viewModel::setShowCumulativeAwayTime,
            )
            Spacer(Modifier.height(18.dp))
        }
    }
}

@Composable
private fun AutoStartControls(
    settings: AppSettings,
    viewModel: SettingsViewModel,
    strings: NagomiStrings,
) {
    TimerSwitchRow(
        strings.text("auto_start_breaks", "Auto-start breaks"),
        strings.text("auto_start_breaks_description", "Start breaks when focus ends."),
        settings.autoStartBreak,
        viewModel::setAutoStartBreak,
    )
    TimerSwitchRow(
        strings.text("auto_start_focus_sessions", "Auto-start focus sessions"),
        strings.text("auto_start_focus_sessions_description", "Start focus when a break ends."),
        settings.autoStartFocus,
        viewModel::setAutoStartFocus,
    )
}

@Composable
private fun TimerSwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun TimerNumberField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.filter(Char::isDigit)) },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = modifier,
    )
}
