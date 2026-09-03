# Nagomi Mobile

**Plan. Focus. Progress.**

Nagomi Mobile is a native Android productivity app that combines Pomodoro timing,
study planning, task-based focus sessions and progress statistics in one calm,
customizable workspace.

The app is built with Kotlin and Jetpack Compose as the native successor to the
original KivyMD NagomiMobile application. It works locally, requires no account
or internet connection, and is designed for both phones and tablets.

## Features

### Pomodoro Timer

- Timestamp-based focus, short-break and long-break countdowns
- Start, pause, reset and skip controls
- Configurable focus and break durations
- Configurable long-break interval and number of focus sessions per cycle
- Optional automatic transition from focus to break and from break to focus
- Persistent timer state across screen rotation, app recreation and process restarts
- Completed Pomodoro focus sessions recorded in local statistics

### Focus Timer

- Runs an individual task or an entire Study Plan queue
- Shows the active task, planned focus duration and current phase
- Start/pause, reset, complete task, skip break and Stop Plan controls
- Per-task break durations and automatic progression to the next queued task
- Collapsible **Up next** task queue
- Optional cumulative away-time tracking while the app is in the background
- Persistent active task, phase, countdown and pause state
- Pomodoro and Focus Timer cannot run simultaneously

### Study Plan

- Add, edit, duplicate, reorder and delete tasks
- Set a subject, focus duration, break duration and priority for each task
- Mark tasks complete or reopen them
- Filter by All, Pending, Active and Completed
- Start one task directly or run the full plan as a queue
- Clear completed tasks or clear the entire plan with confirmation

### Subjects

- Create and delete custom subjects
- Duplicate-name validation
- Custom subject colors
- Safe reassignment of related tasks and sessions to **Other** when a subject is deleted

### Statistics

- Today's focused time, completed sessions and away time
- Study Plan and Pomodoro focus breakdown
- Daily focus-goal progress
- Subject-filtered seven-day overview
- Weekly subject distribution
- Five most recent sessions for the current day
- Local history clearing with confirmation

### Settings and personalization

- Dark and light appearance modes
- Eight Nagomi color palettes
- Six bundled alarm tones: Analog, Beep, Birdy, Buzz, Dance and Galaxy
- Alarm sound preview and vibration controls
- Daily focus goal and first day of the week
- Queue and away-time visibility controls
- In-page settings panels for both timers
- System-access status and shortcuts for notification and exact-alarm permissions
- In-app Privacy Policy
- Persistent English, Turkish, German, French and Spanish language selection

## Native Android integration

- `AlarmManager` scheduling for Pomodoro and Focus Timer
- Exact-alarm access request on Android 12 and later, with a safe inexact fallback
- Notification permission flow on Android 13 and later
- Public lock-screen countdown notifications using Android's countdown chronometer
- Looping alarm audio and vibration through a foreground media-playback service
- **Stop Alarm** action in notifications, timer screens and the app-wide alarm card
- Direct navigation from an active alarm to the relevant timer
- Background timer completion and optional automatic focus/break chaining
- Immersive layout with transient system navigation controls

## Phone and tablet support

- Phone portrait layout with a navigation drawer
- Tablet portrait and landscape layouts with an adaptive navigation rail
- Height-aware timer sizing designed to avoid scrolling on timer screens
- Theme-consistent full-screen backgrounds in dark and light mode
- Current page and queue state preserved during screen rotation
- Responsive splash artwork on `#1E163D` with the `SKLabs®` signature
- Timer settings actions positioned in the shared page header

## Privacy

Nagomi Mobile stores subjects, tasks, timer settings and focus-session history
locally on the device. It does not include advertising, analytics or tracking
services, and the Android manifest does not request internet access.

Notification, exact-alarm, vibration, wake-lock and foreground-service permissions
are used only to keep timers reliable and deliver their alarms. The full Privacy
Policy is available from the application's Settings page.

## Languages

- English
- Turkish
- German
- French
- Spanish

Navigation, screens, settings, status messages, weekday labels and timer/alarm
notifications follow the selected language.

## Technology

- Kotlin
- Jetpack Compose and Material 3
- Room 2.8
- Kotlin coroutines and Flow
- Android `AlarmManager`, notifications and foreground services
- JUnit timer-engine tests

## Requirements

- Android 6.0 or later (`minSdk = 23`)
- Target SDK 35
- JDK 17 for development
- Android Studio with Gradle support

## Open in Android Studio

1. Open the `NagomiNative` folder as an Android Studio project.
2. Wait for Gradle Sync to finish.
3. Select the `app` run configuration.
4. Run the app on an Android phone, tablet or emulator.

Debug builds use the package ID `com.sklabs.nagomi.native`, allowing them to be
installed beside an existing release build. Release builds use
`com.sklabs.nagomi`.

## Install the APK

Download the current APK from the repository's **Releases** section and install
it on an Android device. Android may ask you to allow installation from the app
used to download the APK.

## Updating from the legacy KivyMD app

The native app can perform a one-time import of compatible legacy
`app_data.json` content, including subjects, tasks, sessions, settings and saved
timer state.

Migration requires all of the following:

- The native release must use the same package ID: `com.sklabs.nagomi`.
- It must be installed as an update over the existing app.
- It must be signed with the same signing key.
- The existing app must not be uninstalled before the update.

Debug builds cannot access the release app's private data because they use a
different package ID.

## Current version

`v0.8.1`

Developed by **SKLabs®**.
