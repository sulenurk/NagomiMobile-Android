package com.sklabs.nagomi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sklabs.nagomi.notifications.AlarmSoundCatalog
import com.sklabs.nagomi.ui.settings.SettingsViewModel
import com.sklabs.nagomi.ui.theme.NagomiPalettes
import com.sklabs.nagomi.ui.localization.LocalNagomiStrings
import com.sklabs.nagomi.ui.localization.NagomiStrings

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val status by viewModel.status.collectAsStateWithLifecycle()
    val systemAccess by viewModel.systemAccess.collectAsStateWithLifecycle()
    var dailyGoal by rememberSaveable { mutableStateOf(settings.dailyFocusGoalMinutes.toString()) }
    var focusMinutes by rememberSaveable { mutableStateOf(settings.regularFocusMinutes.toString()) }
    var shortBreakMinutes by rememberSaveable { mutableStateOf(settings.regularShortBreakMinutes.toString()) }
    var longBreakMinutes by rememberSaveable { mutableStateOf(settings.regularLongBreakMinutes.toString()) }
    var longBreakAfter by rememberSaveable { mutableStateOf(settings.regularLongBreakAfter.toString()) }
    var focusCount by rememberSaveable { mutableStateOf(settings.regularFocusCount.toString()) }
    var showResetDialog by rememberSaveable { mutableStateOf(false) }
    var showPrivacyPolicy by rememberSaveable { mutableStateOf(false) }
    val strings = LocalNagomiStrings.current

    LaunchedEffect(settings.dailyFocusGoalMinutes) {
        dailyGoal = settings.dailyFocusGoalMinutes.toString()
    }
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

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Spacer(Modifier.height(10.dp))
            Text(strings.text("settings", "Settings"), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                strings.text("settings_description", "Customize timers, alarms, appearance and statistics."),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        item {
            SettingsCard(strings.text("auto_start", "Auto start")) {
                SettingSwitchRow(
                    title = strings.text("auto_start_focus_sessions", "Auto-start focus sessions"),
                    description = strings.text("auto_start_focus_sessions_description", "Starts the next focus session when a break ends."),
                    checked = settings.autoStartFocus,
                    onCheckedChange = viewModel::setAutoStartFocus,
                )
                SettingSwitchRow(
                    title = strings.text("auto_start_breaks", "Auto-start breaks"),
                    description = strings.text("auto_start_breaks_description", "Starts the break when a focus session is completed."),
                    checked = settings.autoStartBreak,
                    onCheckedChange = viewModel::setAutoStartBreak,
                )
            }
        }

        item {
            SettingsCard(strings.text("sound_and_vibration", "Sound and vibration")) {
                SettingSwitchRow(
                    title = strings.text("sound_notifications", "Sound notifications"),
                    description = strings.text("sound_notifications_description", "Play a looping alarm when a timer finishes."),
                    checked = settings.soundEnabled,
                    onCheckedChange = viewModel::setSoundEnabled,
                )
                SettingSwitchRow(
                    title = strings.text("vibration", "Vibration"),
                    description = strings.text("vibration_description", "Vibrate while the timer alarm is active."),
                    checked = settings.vibrationEnabled,
                    onCheckedChange = viewModel::setVibrationEnabled,
                )
                SettingsDropdown(
                    label = strings.text("alarm_tone", "Alarm tone"),
                    selectedKey = settings.alarmSound,
                    options = AlarmSoundCatalog.options.map { it.key to it.label },
                    enabled = settings.soundEnabled,
                    onSelected = viewModel::setAlarmSound,
                )
            }
        }

        item {
            SettingsCard(strings.text("pomodoro_settings", "Pomodoro timer")) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    NumericSettingField(strings.text("focus_duration_minutes", "Focus min"), focusMinutes, { focusMinutes = it }, Modifier.weight(1f))
                    NumericSettingField(strings.text("short_break_minutes", "Short break"), shortBreakMinutes, { shortBreakMinutes = it }, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    NumericSettingField(strings.text("long_break_minutes", "Long break"), longBreakMinutes, { longBreakMinutes = it }, Modifier.weight(1f))
                    NumericSettingField(strings.text("long_break_interval", "After focus"), longBreakAfter, { longBreakAfter = it }, Modifier.weight(1f))
                }
                NumericSettingField(
                    label = strings.text("total_focus_cycles", "Focus sessions per cycle (0 = unlimited)"),
                    value = focusCount,
                    onValueChange = { focusCount = it },
                    modifier = Modifier.fillMaxWidth(),
                )
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
                ) { Text(strings.text("save_pomodoro_settings", "Save Pomodoro settings")) }
            }
        }

        item {
            SettingsCard(strings.text("appearance", "Appearance")) {
                SettingsDropdown(
                    label = strings.text("theme", "Theme"),
                    selectedKey = settings.appearanceMode,
                    options = listOf("dark" to strings.text("dark_mode", "Dark"), "light" to strings.text("light_mode", "Light")),
                    onSelected = viewModel::setAppearanceMode,
                )
                SettingsDropdown(
                    label = strings.text("color_palette", "Color palette"),
                    selectedKey = settings.colorPalette,
                    options = paletteOptions(strings),
                    onSelected = viewModel::setColorPalette,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                ) {
                    val palette = NagomiPalettes[settings.colorPalette]
                    listOfNotNull(palette?.primary, palette?.darkSurface, palette?.lightBackground).forEach { color ->
                        Box(Modifier.size(28.dp).background(color, CircleShape))
                    }
                }
                SettingsDropdown(
                    label = strings.text("language", "Language"),
                    selectedKey = settings.language,
                    options = NagomiStrings.SUPPORTED_LANGUAGES,
                    onSelected = viewModel::setLanguage,
                )
            }
        }

        item {
            SettingsCard(strings.text("plan_and_statistics", "Plan and statistics")) {
                OutlinedTextField(
                    value = dailyGoal,
                    onValueChange = { dailyGoal = it.filter(Char::isDigit) },
                    label = { Text(strings.text("daily_focus_goal", "Daily focus goal (min)")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                SettingSwitchRow(
                    title = strings.text("show_queue_progress", "Show queue progress"),
                    description = strings.text("show_queue_progress_description", "Display the upcoming tasks in Focus Timer."),
                    checked = settings.showQueueProgress,
                    onCheckedChange = viewModel::setShowQueueProgress,
                )
                SettingSwitchRow(
                    title = strings.text("show_total_away_time", "Show total away time"),
                    description = strings.text("show_away_time_description", "Display cumulative background time during focus."),
                    checked = settings.showCumulativeAwayTime,
                    onCheckedChange = viewModel::setShowCumulativeAwayTime,
                )
                SettingsDropdown(
                    label = strings.text("first_day_of_week", "First day of week"),
                    selectedKey = settings.weekStartDay,
                    options = listOf("monday" to strings.text("monday", "Monday"), "sunday" to strings.text("sunday", "Sunday")),
                    onSelected = viewModel::setWeekStartDay,
                )
                Button(
                    onClick = { viewModel.saveDailyGoal(dailyGoal) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(strings.text("save", "Save"))
                }
                if (status.isNotBlank()) {
                    Text(strings.status(status), color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        item {
            SettingsCard(strings.text("system_access", "System access")) {
                SystemAccessRow(
                    title = strings.text("notifications", "Notifications"),
                    icon = Icons.Default.Notifications,
                    enabled = systemAccess.notificationsEnabled,
                    iconColor = MaterialTheme.colorScheme.primary,
                    onOpen = viewModel::openNotificationSettings,
                )
                SystemAccessRow(
                    title = strings.text("exact_alarms", "Exact alarms"),
                    icon = Icons.Default.Alarm,
                    enabled = systemAccess.exactAlarmsEnabled,
                    iconColor = Color(0xFFF59E0B),
                    onOpen = viewModel::openExactAlarmSettings,
                )
            }
        }

        item {
            SettingsCard(strings.text("privacy_policy", "Privacy Policy")) {
                Text(
                    strings.text(
                        "privacy_policy_summary",
                        "Learn how Nagomi Mobile handles your data and device permissions.",
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedButton(
                    onClick = { showPrivacyPolicy = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(strings.text("read_privacy_policy", "Read Privacy Policy"))
                }
            }
        }

        item {
            OutlinedButton(
                onClick = { showResetDialog = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(strings.text("reset_settings", "Reset settings"), color = MaterialTheme.colorScheme.error)
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }

    if (showPrivacyPolicy) {
        AlertDialog(
            onDismissRequest = { showPrivacyPolicy = false },
            title = { Text(strings.text("privacy_policy", "Privacy Policy")) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text(strings.text("privacy_policy_text", DEFAULT_PRIVACY_POLICY))
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyPolicy = false }) {
                    Text(strings.text("close", "Close"))
                }
            },
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(strings.text("reset_settings", "Reset settings")) },
            text = { Text(strings.text("reset_settings_confirmation", "All settings will return to their Nagomi defaults.")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetDialog = false
                        viewModel.resetSettings()
                    },
                ) { Text(strings.text("reset", "Reset"), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text(strings.text("cancel", "Cancel")) }
            },
        )
    }
}

private const val DEFAULT_PRIVACY_POLICY = """Effective date: September 3, 2026

Nagomi Mobile stores your timer settings, subjects, tasks, study plans and session statistics locally on your device.

SKLabs does not collect, transmit, sell or share your personal data. The app does not require an internet connection and does not include advertising, analytics or tracking services.

Notification and exact-alarm permissions are used only to display timer countdowns and play alarms at the requested time.

Your data remains on your device until you remove it from the app or uninstall the app. Uninstalling the app may permanently delete locally stored data.

For privacy questions, contact SKLabs through the support address shown on the application's distribution page."""

@Composable
private fun SettingsCard(title: String, content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

@Composable
private fun SettingSwitchRow(
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingsDropdown(
    label: String,
    selectedKey: String,
    options: List<Pair<String, String>>,
    enabled: Boolean = true,
    onSelected: (String) -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.first == selectedKey }?.second ?: selectedKey
    Box(Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expanded = true },
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                Text(label, style = MaterialTheme.typography.labelSmall)
                Text(selectedLabel)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (key, display) ->
                DropdownMenuItem(
                    text = { Text(display) },
                    onClick = {
                        expanded = false
                        onSelected(key)
                    },
                )
            }
        }
    }
}

@Composable
private fun NumericSettingField(
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

@Composable
private fun SystemAccessRow(
    title: String,
    icon: ImageVector,
    enabled: Boolean,
    iconColor: Color,
    onOpen: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        androidx.compose.material3.Icon(
            icon,
            contentDescription = null,
            tint = iconColor,
        )
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(
                if (enabled) LocalNagomiStrings.current.text("allowed", "Allowed")
                else LocalNagomiStrings.current.text("permission_needed", "Permission needed for reliable timers"),
                color = if (enabled) Color(0xFF22C55E) else MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        TextButton(onClick = onOpen) { Text(LocalNagomiStrings.current.text("open", "Open")) }
    }
}

private fun paletteOptions(strings: com.sklabs.nagomi.ui.localization.NagomiStrings) = listOf(
    "purple" to strings.text("palette_purple", "Purple"),
    "pinky" to strings.text("palette_pinky", "Pinky"),
    "ocean" to strings.text("palette_ocean_blue", "Ocean"),
    "forest" to strings.text("palette_forest_green", "Forest"),
    "monochrome" to strings.text("palette_monochrome", "Monochrome"),
    "slate" to strings.text("palette_slate", "Slate"),
    "amber" to strings.text("palette_sunset_amber", "Amber"),
    "mint" to strings.text("palette_nordic_mint", "Mint"),
)
