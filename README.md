# Nagomi Native — Kotlin migration

This project is the native Android/Jetpack Compose migration of the existing
KivyMD NagomiMobile application.

## Included through Phase 8

- Native Nagomi splash screen using the supplied artwork on `#1E163D`.
- `SKLabs™` brand signature on the splash screen, keeping application pages uncluttered.
- Pomodoro and Focus settings actions in the page header: beside the navigation control on phones and at the page-header right edge on tablets.

- Existing package identity: `com.sklabs.nagomi`
- Phone portrait lock and tablet rotation support
- Adaptive phone drawer / tablet navigation rail
- Six application destinations
- Working timestamp-based Pomodoro engine
- Focus, short-break, long-break, skip, pause and reset behavior
- Eight Nagomi color palettes
- Kotlin models matching the existing JSON subject/task/session/settings data
- Existing icons, alarm sounds and five locale JSON files retained as migration assets
- Unit tests for countdown, long-break selection and cycle completion
- Room 2.8 database for subjects, study tasks and focus sessions
- Subjects: add, duplicate-name validation, color selection/cycling and delete
- Subject deletion safely reassigns related tasks and sessions to Other
- Study Plan: add, edit, complete, reopen, duplicate, delete and reorder tasks
- Study Plan filters: All, Pending, Active and Completed
- Start Task and Start Plan persist active/queue state before opening Focus Timer
- Focus Timer executes a single task or the full Study Plan queue
- Focus sessions and completed tasks are written to Room automatically
- Per-task breaks transition into the next queued focus session
- Focus countdown, phase, pause state and active task survive app recreation
- Timestamp-based background countdown and away-time tracking
- Focus controls: start/pause, reset phase, complete task, skip break and stop plan
- Native AlarmManager scheduling for Pomodoro and Focus Timer
- Exact-alarm access request on Android 12+ with a safe inexact fallback
- Android 13+ notification permission flow when a timer is first started
- Public lock-screen countdown notifications using Android's countdown chronometer
- Looping alarm audio and vibration from a media-playback foreground service
- Stop Alarm action directly in the alarm notification
- Reset, pause, skip and Stop Plan also silence active alarm playback
- Complete Settings screen with persistent timer, alarm, appearance and statistics preferences
- Six selectable alarm tones with immediate preview
- Dark/light appearance and eight live color palettes
- Configurable Pomodoro durations, cycle behavior and auto-start controls
- Configurable daily focus goal, first day of week, queue visibility and away-time visibility
- System access status and shortcuts for notifications and exact alarms
- Complete Statistics screen backed by Room focus-session history
- Today metrics, Study Plan/Pomodoro breakdown and daily-goal progress
- Subject-filtered seven-day bar chart and weekly subject distribution
- Five most recent daily sessions and confirmed statistics clearing
- Completed regular Pomodoro focus sessions are now recorded in Room
- In-page bottom-sheet settings for Pomodoro and Focus Timer
- Pomodoro focus, short-break, long-break and cycle editing from the timer page
- Shared Auto-start Break and Auto-start Focus switches on both timer pages
- Phone drawer and tablet rail shortcuts for alarm sound, dark mode and language
- Height-aware Pomodoro sizing that fits tablet portrait and landscape without scrolling
- Theme-consistent full-screen backgrounds and content colors on both phone and tablet layouts
- Persistent English, Turkish, German, French and Spanish language selection
- Localized navigation titles and timer settings panels backed by the retained locale files
- Contextual Stop Alarm buttons on Pomodoro and Focus Timer while alarm playback is active
- Persistent Pomodoro countdown state across app recreation and process restarts
- Background completion and auto-start chaining for Pomodoro and Study Plan timers
- Mutual exclusion between Pomodoro and Focus Timer starts
- App-wide active alarm card with Stop Alarm and direct timer navigation
- Study Plan bulk actions for clearing completed tasks or the whole plan
- Full screen-content localization in English, Turkish, German, French and Spanish
- Localized weekday labels and timer/alarm notifications
- One-time import of compatible legacy `app_data.json` data on same-package upgrades

Nagomi continues to use the six bundled local alarm sounds.

## Open in Android Studio

Open the `NagomiNative` folder as a project, wait for Gradle Sync, and run the
`app` configuration on a phone or tablet.

Debug builds use `com.sklabs.nagomi.native`, so they can be installed beside the
current Kivy app without replacing it. Release builds keep `com.sklabs.nagomi`.
Do not uninstall the current Nagomi APK if its local data must be migrated. A
same-package release update also requires signing with the same key.

Room 2.8 requires Android API 23 or newer, so the native project now uses
`minSdk = 23`.
