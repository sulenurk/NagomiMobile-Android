package com.sklabs.nagomi

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Topic
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sklabs.nagomi.data.model.AppSettings
import com.sklabs.nagomi.data.migration.LegacyDataMigrator
import com.sklabs.nagomi.notifications.AlarmPlaybackService
import com.sklabs.nagomi.notifications.NativeTimerKind
import com.sklabs.nagomi.ui.focus.FocusViewModel
import com.sklabs.nagomi.ui.localization.NagomiStrings
import com.sklabs.nagomi.ui.localization.LocalNagomiStrings
import com.sklabs.nagomi.ui.settings.SettingsViewModel
import com.sklabs.nagomi.timer.PomodoroViewModel
import com.sklabs.nagomi.ui.screens.PomodoroScreen
import com.sklabs.nagomi.ui.screens.FocusScreen
import com.sklabs.nagomi.ui.screens.SettingsScreen
import com.sklabs.nagomi.ui.screens.StatisticsScreen
import com.sklabs.nagomi.ui.screens.StudyPlanScreen
import com.sklabs.nagomi.ui.screens.SubjectsScreen
import com.sklabs.nagomi.ui.theme.NagomiTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    private val requestedTimer = MutableStateFlow<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        AppRuntimeState.hasUiProcess = true
        if (savedInstanceState == null) {
            runBlocking(Dispatchers.IO) {
                LegacyDataMigrator(applicationContext).migrateIfAvailable()
            }
        }
        requestedTimer.value = intent.getStringExtra(com.sklabs.nagomi.notifications.NativeTimerScheduler.EXTRA_TIMER_KIND)

        if (resources.configuration.smallestScreenWidthDp < 600) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        }

        enableEdgeToEdge()
        hideSystemNavigationBar()
        setContent {
            NagomiRoot(requestedTimer) { requestedTimer.value = null }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        requestedTimer.value = intent.getStringExtra(
            com.sklabs.nagomi.notifications.NativeTimerScheduler.EXTRA_TIMER_KIND,
        )
    }

    override fun onDestroy() {
        if (isFinishing) AppRuntimeState.hasUiProcess = false
        super.onDestroy()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemNavigationBar()
    }

    private fun hideSystemNavigationBar() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}

private enum class Destination(val title: String, val icon: ImageVector) {
    POMODORO("Pomodoro", Icons.Default.Timer),
    FOCUS("Focus Timer", Icons.Default.School),
    SUBJECTS("Subjects", Icons.Default.Topic),
    STUDY_PLAN("Study Plan", Icons.Default.Checklist),
    STATISTICS("Statistics", Icons.Default.BarChart),
    SETTINGS("Settings", Icons.Default.Settings),
}

private val DestinationSaver = Saver<Destination, String>(
    save = { it.name },
    restore = { savedName ->
        Destination.entries.firstOrNull { it.name == savedName } ?: Destination.POMODORO
    },
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NagomiRoot(
    requestedTimer: kotlinx.coroutines.flow.StateFlow<String?>,
    onTimerRequestConsumed: () -> Unit,
) {
    val settingsViewModel: SettingsViewModel = viewModel()
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val strings = remember(context, settings.language) { NagomiStrings.load(context, settings.language) }
    var showBrandSplash by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(900)
        showBrandSplash = false
    }

    NagomiTheme(
        darkTheme = settings.appearanceMode != "light",
        paletteKey = settings.colorPalette,
    ) {
        if (showBrandSplash) {
            NagomiBrandSplash()
        } else {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground,
            ) {
                CompositionLocalProvider(LocalNagomiStrings provides strings) {
                    NagomiApp(settingsViewModel, settings, requestedTimer, onTimerRequestConsumed)
                }
            }
        }
    }
}

@Composable
private fun NagomiBrandSplash() {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().background(Color(0xFF1E163D)),
        contentAlignment = Alignment.Center,
    ) {
        val logoSize = minOf(maxWidth * 0.66f, maxHeight * 0.58f, 360.dp)

        Image(
            painter = painterResource(R.drawable.nagomi_splash),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(logoSize),
        )
        Text(
            text = "SKLabs®",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            color = Color.White.copy(alpha = 0.64f),
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NagomiApp(
    settingsViewModel: SettingsViewModel,
    settings: AppSettings,
    requestedTimer: kotlinx.coroutines.flow.StateFlow<String?>,
    onTimerRequestConsumed: () -> Unit,
) {
    var destination by rememberSaveable(stateSaver = DestinationSaver) {
        mutableStateOf(Destination.POMODORO)
    }
    val focusViewModel: FocusViewModel = viewModel()
    val pomodoroViewModel: PomodoroViewModel = viewModel()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isTablet = configuration.smallestScreenWidthDp >= 600
    val strings = remember(context, settings.language) { NagomiStrings.load(context, settings.language) }
    val alarmState by AlarmPlaybackService.state.collectAsStateWithLifecycle()
    val requestedKind by requestedTimer.collectAsStateWithLifecycle()
    var timerSettingsRequestKey by remember { mutableIntStateOf(0) }
    val openTimerSettings: () -> Unit = { timerSettingsRequestKey += 1 }
    val canOpenTimerSettings = destination == Destination.POMODORO || destination == Destination.FOCUS

    LaunchedEffect(requestedKind) {
        when (requestedKind) {
            NativeTimerKind.POMODORO.key -> {
                destination = Destination.POMODORO
                AlarmPlaybackService.stop(context)
            }
            NativeTimerKind.FOCUS.key -> {
                destination = Destination.FOCUS
                AlarmPlaybackService.stop(context)
            }
        }
        if (requestedKind != null) onTimerRequestConsumed()
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) {
        requestExactAlarmAccess(context)
        focusViewModel.refreshNativeTimer()
        pomodoroViewModel.refreshNativeTimer()
    }
    val ensureTimerPermissions: () -> Unit = {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            requestExactAlarmAccess(context)
            focusViewModel.refreshNativeTimer()
            pomodoroViewModel.refreshNativeTimer()
        }
    }
    val onPomodoroStartRequested: () -> Unit = {
        focusViewModel.pauseIfRunning()
        ensureTimerPermissions()
    }
    val onFocusStartRequested: () -> Unit = {
        pomodoroViewModel.pauseIfRunning()
        ensureTimerPermissions()
    }

    DisposableEffect(lifecycleOwner, focusViewModel, pomodoroViewModel, settingsViewModel) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> focusViewModel.onAppForegrounded()
                Lifecycle.Event.ON_STOP -> focusViewModel.onAppBackgrounded()
                else -> Unit
            }
            if (event == Lifecycle.Event.ON_START) pomodoroViewModel.onAppForegrounded()
            if (event == Lifecycle.Event.ON_START) settingsViewModel.refreshSystemAccess()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        if (isTablet) Row(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            NavigationRail {
                Destination.entries.forEach { item ->
                    NavigationRailItem(
                        selected = destination == item,
                        onClick = { destination = item },
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(destinationTitle(item, strings)) },
                    )
                }
                Spacer(Modifier.weight(1f))
                TabletRailQuickSettings(
                    settings = settings,
                    viewModel = settingsViewModel,
                    strings = strings,
                )
            }
            Column(Modifier.weight(1f).fillMaxSize()) {
                TabletPageHeader(
                    destination = destination,
                    strings = strings,
                    showSettings = canOpenTimerSettings,
                    onOpenSettings = openTimerSettings,
                )
                ScreenContent(
                    destination = destination,
                    modifier = Modifier.weight(1f),
                    onNavigate = { destination = it },
                    focusViewModel = focusViewModel,
                    pomodoroViewModel = pomodoroViewModel,
                    onPomodoroStartRequested = onPomodoroStartRequested,
                    onFocusStartRequested = onFocusStartRequested,
                    settingsViewModel = settingsViewModel,
                    settings = settings,
                    timerSettingsRequestKey = timerSettingsRequestKey,
                    onTimerSettingsRequestConsumed = { timerSettingsRequestKey = 0 },
                )
            }
        } else ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    "Nagomi",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(24.dp),
                )
                Destination.entries.forEach { item ->
                    NavigationDrawerItem(
                        selected = destination == item,
                        onClick = {
                            destination = item
                            scope.launch { drawerState.close() }
                        },
                        icon = { Icon(item.icon, contentDescription = null) },
                        label = { Text(destinationTitle(item, strings)) },
                        modifier = Modifier.padding(horizontal = 12.dp),
                    )
                }
                HorizontalDivider(Modifier.padding(horizontal = 16.dp, vertical = 10.dp))
                DrawerQuickSettings(
                    settings = settings,
                    viewModel = settingsViewModel,
                    strings = strings,
                )
            }
        },
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(destinationTitle(destination, strings)) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = strings.text("menu", "Menu"))
                        }
                    },
                    actions = {
                        if (canOpenTimerSettings) {
                            IconButton(onClick = openTimerSettings) {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = if (destination == Destination.POMODORO) {
                                        strings.text("pomodoro_settings", "Pomodoro settings")
                                    } else {
                                        strings.text("focus_settings", "Focus settings")
                                    },
                                )
                            }
                        }
                    },
                )
            },
        ) { padding ->
            ScreenContent(
                destination = destination,
                modifier = Modifier.padding(padding),
                onNavigate = { destination = it },
                focusViewModel = focusViewModel,
                pomodoroViewModel = pomodoroViewModel,
                onPomodoroStartRequested = onPomodoroStartRequested,
                onFocusStartRequested = onFocusStartRequested,
                settingsViewModel = settingsViewModel,
                settings = settings,
                timerSettingsRequestKey = timerSettingsRequestKey,
                onTimerSettingsRequestConsumed = { timerSettingsRequestKey = 0 },
            )
        }
        }

        val alarmDestination = when (alarmState.timerKind) {
            NativeTimerKind.FOCUS.key -> Destination.FOCUS
            NativeTimerKind.POMODORO.key -> Destination.POMODORO
            else -> null
        }
        if (alarmState.active && alarmDestination != null && destination != alarmDestination) {
            GlobalAlarmCard(
                title = alarmState.title,
                strings = strings,
                onStop = { AlarmPlaybackService.stop(context) },
                onGoToTimer = {
                    AlarmPlaybackService.stop(context)
                    destination = alarmDestination
                },
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
            )
        }
    }
}

@Composable
private fun TabletPageHeader(
    destination: Destination,
    strings: NagomiStrings,
    showSettings: Boolean,
    onOpenSettings: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(destination.icon, contentDescription = null)
        Text(
            destinationTitle(destination, strings),
            modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        if (showSettings) {
            IconButton(onClick = onOpenSettings) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = if (destination == Destination.POMODORO) {
                        strings.text("pomodoro_settings", "Pomodoro settings")
                    } else {
                        strings.text("focus_settings", "Focus settings")
                    },
                )
            }
        }
    }
}

@Composable
private fun TabletRailQuickSettings(
    settings: AppSettings,
    viewModel: SettingsViewModel,
    strings: NagomiStrings,
) {
    var languageExpanded by remember { mutableStateOf(false) }
    RailQuickAction(
        icon = Icons.Default.Alarm,
        label = strings.text("alarm", "Alarm"),
        active = settings.soundEnabled,
        onClick = { viewModel.setSoundEnabled(!settings.soundEnabled) },
    )
    RailQuickAction(
        icon = Icons.Default.DarkMode,
        label = strings.text("dark_mode", "Dark mode"),
        active = settings.appearanceMode == "dark",
        onClick = {
            viewModel.setAppearanceMode(if (settings.appearanceMode == "dark") "light" else "dark")
        },
    )
    Box {
        RailQuickAction(
            icon = Icons.Default.Language,
            label = "${strings.text("language", "Language")} (${settings.language.uppercase()})",
            active = false,
            onClick = { languageExpanded = true },
        )
        DropdownMenu(
            expanded = languageExpanded,
            onDismissRequest = { languageExpanded = false },
        ) {
            NagomiStrings.SUPPORTED_LANGUAGES.forEach { (key, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        languageExpanded = false
                        viewModel.setLanguage(key)
                    },
                )
            }
        }
    }
}

@Composable
private fun RailQuickAction(
    icon: ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(44.dp),
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (active) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun GlobalAlarmCard(
    title: String,
    strings: NagomiStrings,
    onStop: () -> Unit,
    onGoToTimer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Row {
                TextButton(onClick = onStop) { Text(strings.text("stop_alarm", "Stop alarm")) }
                Button(onClick = onGoToTimer) { Text(strings.text("go_to_timer", "Go to timer")) }
            }
        }
    }
}

@Composable
private fun DrawerQuickSettings(
    settings: AppSettings,
    viewModel: SettingsViewModel,
    strings: NagomiStrings,
) {
    var languageExpanded by remember { mutableStateOf(false) }
    DrawerToggleRow(
        icon = Icons.Default.Alarm,
        label = strings.text("alarm", "Alarm"),
        checked = settings.soundEnabled,
        onCheckedChange = viewModel::setSoundEnabled,
    )
    DrawerToggleRow(
        icon = Icons.Default.DarkMode,
        label = strings.text("dark_mode", "Dark mode"),
        checked = settings.appearanceMode == "dark",
        onCheckedChange = { viewModel.setAppearanceMode(if (it) "dark" else "light") },
    )
    Box(Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
        NavigationDrawerItem(
            selected = false,
            onClick = { languageExpanded = true },
            icon = { Icon(Icons.Default.Language, contentDescription = null) },
            label = {
                Column {
                    Text(strings.text("language", "Language"))
                    Text(
                        NagomiStrings.SUPPORTED_LANGUAGES.firstOrNull { it.first == settings.language }?.second
                            ?: "English",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
        )
        DropdownMenu(
            expanded = languageExpanded,
            onDismissRequest = { languageExpanded = false },
        ) {
            NagomiStrings.SUPPORTED_LANGUAGES.forEach { (key, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        languageExpanded = false
                        viewModel.setLanguage(key)
                    },
                )
            }
        }
    }
}

@Composable
private fun DrawerToggleRow(
    icon: ImageVector,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null)
        Text(label, modifier = Modifier.weight(1f).padding(horizontal = 12.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

private fun destinationTitle(destination: Destination, strings: NagomiStrings): String = when (destination) {
    Destination.POMODORO -> strings.text("pomodoro", destination.title)
    Destination.FOCUS -> strings.text("focus_timer", destination.title)
    Destination.SUBJECTS -> strings.text("subjects", destination.title)
    Destination.STUDY_PLAN -> strings.text("study_plan", destination.title)
    Destination.STATISTICS -> strings.text("statistics", destination.title)
    Destination.SETTINGS -> strings.text("settings", destination.title)
}

@Composable
private fun ScreenContent(
    destination: Destination,
    modifier: Modifier = Modifier,
    onNavigate: (Destination) -> Unit,
    focusViewModel: FocusViewModel,
    pomodoroViewModel: PomodoroViewModel,
    onPomodoroStartRequested: () -> Unit,
    onFocusStartRequested: () -> Unit,
    settingsViewModel: SettingsViewModel,
    settings: AppSettings,
    timerSettingsRequestKey: Int,
    onTimerSettingsRequestConsumed: () -> Unit,
) {
    Box(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        when (destination) {
            Destination.POMODORO -> PomodoroScreen(
                viewModel = pomodoroViewModel,
                onTimerStartRequested = onPomodoroStartRequested,
                settings = settings,
                settingsViewModel = settingsViewModel,
                settingsRequestKey = timerSettingsRequestKey,
                onSettingsRequestConsumed = onTimerSettingsRequestConsumed,
            )
            Destination.FOCUS -> FocusScreen(
                onOpenStudyPlan = { onNavigate(Destination.STUDY_PLAN) },
                viewModel = focusViewModel,
                onTimerStartRequested = onFocusStartRequested,
                settings = settings,
                settingsViewModel = settingsViewModel,
                settingsRequestKey = timerSettingsRequestKey,
                onSettingsRequestConsumed = onTimerSettingsRequestConsumed,
            )
            Destination.SUBJECTS -> SubjectsScreen()
            Destination.STUDY_PLAN -> StudyPlanScreen(
                onStartFocus = { onNavigate(Destination.FOCUS) },
            )
            Destination.STATISTICS -> StatisticsScreen()
            Destination.SETTINGS -> SettingsScreen(settingsViewModel)
        }
    }
}

private fun requestExactAlarmAccess(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
    val alarmManager = context.getSystemService(AlarmManager::class.java)
    if (alarmManager.canScheduleExactAlarms()) return
    val preferences = context.getSharedPreferences("timer_permissions", Context.MODE_PRIVATE)
    if (preferences.getBoolean("exact_alarm_requested", false)) return
    preferences.edit().putBoolean("exact_alarm_requested", true).apply()
    runCatching {
        context.startActivity(
            Intent(
                Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                Uri.parse("package:${context.packageName}"),
            ),
        )
    }
}
