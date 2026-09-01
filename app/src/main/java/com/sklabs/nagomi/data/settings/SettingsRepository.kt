package com.sklabs.nagomi.data.settings

import android.content.Context
import com.sklabs.nagomi.data.model.AppSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {
    private val applicationContext = context.applicationContext

    val settings: StateFlow<AppSettings>
        get() = holder(applicationContext).state.asStateFlow()

    fun update(transform: (AppSettings) -> AppSettings) {
        val holder = holder(applicationContext)
        synchronized(holder) {
            val updated = transform(holder.state.value)
            write(applicationContext, updated)
            holder.state.value = updated
        }
    }

    fun reset() {
        val defaults = AppSettings()
        write(applicationContext, defaults)
        holder(applicationContext).state.value = defaults
    }

    companion object {
        const val PREFERENCES_NAME = "nagomi_settings"

        @Volatile
        private var settingsHolder: SettingsHolder? = null

        fun read(context: Context): AppSettings {
            val preferences = context.applicationContext.getSharedPreferences(
                PREFERENCES_NAME,
                Context.MODE_PRIVATE,
            )
            return AppSettings(
                autoStartBreak = preferences.getBoolean("auto_start_break", false),
                autoStartFocus = preferences.getBoolean("auto_start_focus", false),
                soundEnabled = preferences.getBoolean("sound_enabled", true),
                vibrationEnabled = preferences.getBoolean("vibration_enabled", true),
                alarmSound = preferences.getString("alarm_sound", "beep") ?: "beep",
                dailyFocusGoalMinutes = preferences.getInt("daily_focus_goal_minutes", 300),
                regularFocusMinutes = preferences.getInt("regular_focus_minutes", 25),
                regularShortBreakMinutes = preferences.getInt("regular_short_break_minutes", 5),
                regularLongBreakMinutes = preferences.getInt("regular_long_break_minutes", 15),
                regularLongBreakAfter = preferences.getInt("regular_long_break_after", 4),
                regularFocusCount = preferences.getInt("regular_focus_count", 4),
                showQueueProgress = preferences.getBoolean("show_queue_progress", true),
                showCumulativeAwayTime = preferences.getBoolean("show_cumulative_away_time", true),
                weekStartDay = preferences.getString("week_start_day", "monday") ?: "monday",
                appearanceMode = preferences.getString("appearance_mode", "dark") ?: "dark",
                colorPalette = preferences.getString("color_palette", "purple") ?: "purple",
                language = preferences.getString("language", "en") ?: "en",
            ).sanitized()
        }

        private fun holder(context: Context): SettingsHolder =
            settingsHolder ?: synchronized(this) {
                settingsHolder ?: SettingsHolder(MutableStateFlow(read(context))).also {
                    settingsHolder = it
                }
            }

        private fun write(context: Context, settings: AppSettings) {
            context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean("auto_start_break", settings.autoStartBreak)
                .putBoolean("auto_start_focus", settings.autoStartFocus)
                .putBoolean("sound_enabled", settings.soundEnabled)
                .putBoolean("vibration_enabled", settings.vibrationEnabled)
                .putString("alarm_sound", settings.alarmSound)
                .putInt("daily_focus_goal_minutes", settings.dailyFocusGoalMinutes)
                .putInt("regular_focus_minutes", settings.regularFocusMinutes)
                .putInt("regular_short_break_minutes", settings.regularShortBreakMinutes)
                .putInt("regular_long_break_minutes", settings.regularLongBreakMinutes)
                .putInt("regular_long_break_after", settings.regularLongBreakAfter)
                .putInt("regular_focus_count", settings.regularFocusCount)
                .putBoolean("show_queue_progress", settings.showQueueProgress)
                .putBoolean("show_cumulative_away_time", settings.showCumulativeAwayTime)
                .putString("week_start_day", settings.weekStartDay)
                .putString("appearance_mode", settings.appearanceMode)
                .putString("color_palette", settings.colorPalette)
                .putString("language", settings.language)
                .apply()
        }

        private fun AppSettings.sanitized() = copy(
            alarmSound = alarmSound.takeIf { it in ALARM_KEYS } ?: "beep",
            dailyFocusGoalMinutes = dailyFocusGoalMinutes.coerceAtLeast(1),
            regularFocusMinutes = regularFocusMinutes.coerceAtLeast(1),
            regularShortBreakMinutes = regularShortBreakMinutes.coerceAtLeast(1),
            regularLongBreakMinutes = regularLongBreakMinutes.coerceAtLeast(1),
            regularLongBreakAfter = regularLongBreakAfter.coerceAtLeast(1),
            regularFocusCount = regularFocusCount.coerceAtLeast(0),
            weekStartDay = weekStartDay.takeIf { it == "monday" || it == "sunday" } ?: "monday",
            appearanceMode = appearanceMode.takeIf { it == "dark" || it == "light" } ?: "dark",
            colorPalette = colorPalette.takeIf { it in PALETTE_KEYS } ?: "purple",
            language = language.takeIf { it in LANGUAGE_KEYS } ?: "en",
        )

        private val ALARM_KEYS = setOf("analog", "beep", "birdy", "buzz", "dance", "galaxy")
        private val PALETTE_KEYS = setOf("purple", "pinky", "ocean", "forest", "monochrome", "slate", "amber", "mint")
        private val LANGUAGE_KEYS = setOf("en", "tr", "de", "fr", "es")
    }
}

private data class SettingsHolder(
    val state: MutableStateFlow<AppSettings>,
)
