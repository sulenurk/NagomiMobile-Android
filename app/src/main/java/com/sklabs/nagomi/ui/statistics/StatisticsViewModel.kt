package com.sklabs.nagomi.ui.statistics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sklabs.nagomi.data.local.FocusSessionEntity
import com.sklabs.nagomi.data.local.NagomiDatabase
import com.sklabs.nagomi.data.local.SubjectEntity
import com.sklabs.nagomi.data.model.AppSettings
import com.sklabs.nagomi.data.repository.NagomiRepository
import com.sklabs.nagomi.data.settings.SettingsRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class StatisticsSubjectOption(
    val id: String,
    val name: String,
)

data class DailyFocusBar(
    val label: String,
    val minutes: Int,
)

data class SubjectFocusStat(
    val id: String,
    val name: String,
    val color: String,
    val seconds: Int,
)

data class StatisticsUiState(
    val todayFocusSeconds: Int = 0,
    val todayCompletedSessions: Int = 0,
    val todayAwaySeconds: Int = 0,
    val studyPlanSeconds: Int = 0,
    val pomodoroSeconds: Int = 0,
    val dailyGoalMinutes: Int = 300,
    val weeklyBars: List<DailyFocusBar> = emptyList(),
    val weeklyTotalMinutes: Int = 0,
    val subjectStats: List<SubjectFocusStat> = emptyList(),
    val subjectOptions: List<StatisticsSubjectOption> = listOf(StatisticsSubjectOption("all", "All subjects")),
    val selectedSubjectId: String = "all",
    val recentSessions: List<FocusSessionEntity> = emptyList(),
) {
    val totalTodaySeconds: Int get() = studyPlanSeconds + pomodoroSeconds
    val goalProgress: Float
        get() = (todayFocusSeconds.toFloat() / (dailyGoalMinutes.coerceAtLeast(1) * 60f)).coerceIn(0f, 1f)
}

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = NagomiRepository(NagomiDatabase.getInstance(application))
    private val settingsRepository = SettingsRepository(application)
    private val selectedSubjectId = MutableStateFlow("all")

    val uiState = combine(
        repository.completedSessions,
        repository.subjects,
        settingsRepository.settings,
        selectedSubjectId,
    ) { sessions, subjects, settings, requestedSubjectId ->
        buildStatistics(sessions, subjects, settings, requestedSubjectId)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        StatisticsUiState(),
    )

    init {
        viewModelScope.launch { repository.ensureDefaultSubject() }
    }

    fun selectSubject(id: String) {
        selectedSubjectId.value = id
    }

    fun clearStatistics() {
        viewModelScope.launch { repository.clearStatistics() }
    }

    private fun buildStatistics(
        sessions: List<FocusSessionEntity>,
        subjects: List<SubjectEntity>,
        settings: AppSettings,
        requestedSubjectId: String,
    ): StatisticsUiState {
        val now = System.currentTimeMillis()
        val todayStart = startOfDay(now)
        val tomorrowStart = dayOffset(todayStart, 1)
        val weekStart = startOfWeek(now, settings.weekStartDay)
        val weekEnd = dayOffset(weekStart, 7)
        val focusSessions = sessions.filter { it.mode == "focus" && it.completed }
        val todaySessions = focusSessions.filter { it.completedAtMillis in todayStart until tomorrowStart }
        val weekSessions = focusSessions.filter { it.completedAtMillis in weekStart until weekEnd }

        val options = listOf(StatisticsSubjectOption("all", "All subjects")) + subjects.map {
            StatisticsSubjectOption(it.id, if (it.isDefault) "Other" else it.name ?: "Other")
        }
        val selected = requestedSubjectId.takeIf { id -> options.any { it.id == id } } ?: "all"
        val filteredWeek = if (selected == "all") weekSessions else weekSessions.filter { it.subjectId == selected }

        val weeklyBars = (0 until 7).map { index ->
            val dayStart = dayOffset(weekStart, index)
            val dayEnd = dayOffset(dayStart, 1)
            DailyFocusBar(
                label = dayLabel(dayStart, localeFor(settings.language)),
                minutes = filteredWeek
                    .filter { it.completedAtMillis in dayStart until dayEnd }
                    .sumOf { it.durationSeconds.coerceAtLeast(0) } / 60,
            )
        }

        val subjectMap = subjects.associateBy { it.id }
        val subjectStats = weekSessions
            .groupBy { it.subjectId }
            .map { (subjectId, subjectSessions) ->
                val subject = subjectMap[subjectId]
                SubjectFocusStat(
                    id = subjectId,
                    name = if (subject?.isDefault == true || subjectId == NagomiRepository.OTHER_SUBJECT_ID) {
                        "Other"
                    } else {
                        subject?.name ?: subjectSessions.firstOrNull()?.subjectName ?: "Other"
                    },
                    color = subject?.color ?: "#A78BFA",
                    seconds = subjectSessions.sumOf { it.durationSeconds.coerceAtLeast(0) },
                )
            }
            .sortedByDescending { it.seconds }

        val studyPlanSeconds = todaySessions
            .filter { it.source != "regular_pomodoro" }
            .sumOf { it.durationSeconds.coerceAtLeast(0) }
        val pomodoroSeconds = todaySessions
            .filter { it.source == "regular_pomodoro" }
            .sumOf { it.durationSeconds.coerceAtLeast(0) }

        return StatisticsUiState(
            todayFocusSeconds = todaySessions.sumOf { it.durationSeconds.coerceAtLeast(0) },
            todayCompletedSessions = todaySessions.size,
            todayAwaySeconds = todaySessions.sumOf { it.awaySeconds.coerceAtLeast(0) },
            studyPlanSeconds = studyPlanSeconds,
            pomodoroSeconds = pomodoroSeconds,
            dailyGoalMinutes = settings.dailyFocusGoalMinutes,
            weeklyBars = weeklyBars,
            weeklyTotalMinutes = weeklyBars.sumOf { it.minutes },
            subjectStats = subjectStats,
            subjectOptions = options.distinctBy { it.id },
            selectedSubjectId = selected,
            recentSessions = todaySessions.sortedByDescending { it.completedAtMillis }.take(5),
        )
    }

    private fun startOfDay(timeMillis: Long): Long = Calendar.getInstance().run {
        timeInMillis = timeMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        timeInMillis
    }

    private fun startOfWeek(timeMillis: Long, weekStartDay: String): Long {
        val calendar = Calendar.getInstance().apply { this.timeInMillis = startOfDay(timeMillis) }
        val target = if (weekStartDay == "sunday") Calendar.SUNDAY else Calendar.MONDAY
        val difference = (calendar.get(Calendar.DAY_OF_WEEK) - target + 7) % 7
        calendar.add(Calendar.DAY_OF_MONTH, -difference)
        return calendar.timeInMillis
    }

    private fun dayOffset(startMillis: Long, days: Int): Long = Calendar.getInstance().run {
        timeInMillis = startMillis
        add(Calendar.DAY_OF_MONTH, days)
        timeInMillis
    }

    private fun dayLabel(timeMillis: Long, locale: Locale): String = SimpleDateFormat("EEE", locale)
        .format(Date(timeMillis))
        .take(3)

    private fun localeFor(language: String): Locale = when (language) {
        "tr" -> Locale("tr", "TR")
        "de" -> Locale.GERMAN
        "fr" -> Locale.FRENCH
        "es" -> Locale("es", "ES")
        else -> Locale.ENGLISH
    }
}
