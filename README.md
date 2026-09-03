# Nagomi Mobile

**Plan. Focus. Progress.**

Nagomi Mobile is a native Android productivity app that combines Pomodoro timing, study planning, task-based focus sessions, and progress statistics in one calm, customizable workspace.

The app is built with Kotlin and Jetpack Compose as the native successor to the original KivyMD NagomiMobile application. It works locally, requires no account or internet connection, and is designed for both phones and tablets.

## Features

### Pomodoro Timer

* Timestamp-based focus, short-break, and long-break countdowns
* Start, pause, reset, and skip controls
* Configurable focus and break durations
* Configurable long-break interval and number of focus sessions per cycle
* Optional automatic transition from focus to break and from break to focus
* Persistent timer state across screen rotation, app recreation, and process restarts
* Completed Pomodoro focus sessions recorded in local statistics

### Focus Timer

* Runs an individual task or an entire Study Plan queue
* Shows the active task, planned focus duration, and current phase
* Start/pause, reset, complete task, skip break, and Stop Plan controls
* Per-task break durations and automatic progression to the next queued task
* Collapsible **Up next** task queue
* Optional cumulative away-time tracking while the app is in the background
* Persistent active task, phase, countdown, and pause state
* Pomodoro and Focus Timer cannot run simultaneously

### Study Plan

* Add, edit, duplicate, reorder, and delete tasks
* Set a subject, focus duration, break duration, and priority for each task
* Mark tasks as complete or reopen them
* Filter tasks by All, Pending, Active, and Completed
* Start one task directly or run the full plan as a queue
* Clear completed tasks or the entire plan with confirmation

### Subjects

* Create and delete custom subjects
* Duplicate-name validation
* Custom subject colors
* Safe reassignment of related tasks and sessions to **Other** when a subject is deleted

### Statistics

* Today’s focused time, completed sessions, and away time
* Study Plan and Pomodoro focus breakdown
* Daily focus-goal progress
* Subject-filtered seven-day overview
* Weekly subject distribution
* Five most recent sessions for the current day
* Local history clearing with confirmation

### Settings and Personalization

* Dark and light appearance modes
* Eight Nagomi color palettes
* Six bundled alarm tones: Analog, Beep, Birdy, Buzz, Dance, and Galaxy
* Alarm sound preview and vibration controls
* Configurable daily focus goal and first day of the week
* Queue and away-time visibility controls
* In-page settings panels for both timers
* System-access status and shortcuts for notification and exact-alarm permissions
* In-app Privacy Policy
* Persistent language selection

## Native Android Integration

* `AlarmManager` scheduling for Pomodoro and Focus Timer
* Exact-alarm access request on Android 12 and later, with a safe inexact fallback
* Notification permission flow on Android 13 and later
* Public lock-screen countdown notifications using Android’s countdown chronometer
* Looping alarm audio and vibration through a foreground media-playback service
* **Stop Alarm** action in notifications, timer screens, and the app-wide alarm card
* Direct navigation from an active alarm to the relevant timer
* Background timer completion and optional automatic focus/break chaining
* Immersive layout with transient system navigation controls

## Phone and Tablet Support

* Phone portrait layout with a navigation drawer
* Tablet portrait and landscape layouts with an adaptive navigation rail
* Height-aware timer sizing designed to avoid scrolling on timer screens
* Theme-consistent full-screen backgrounds in dark and light mode
* Current page and queue state preserved during screen rotation
* Responsive splash artwork on `#1E163D`
* `SKLabs™` brand signature on the splash screen
* Timer settings actions positioned in the shared page header

## Privacy

Nagomi Mobile stores subjects, tasks, timer settings, and focus-session history locally on the device.

The app:

* Does not require an account
* Does not request internet access
* Does not collect or transmit personal information
* Does not include advertising
* Does not include analytics or tracking services
* Does not sell or share user data

Notification, exact-alarm, vibration, wake-lock, and foreground-service permissions are used only to keep timers reliable and deliver their alarms.

The complete Privacy Policy is available from the application’s Settings page.

## Languages

Nagomi Mobile is available in:

* English
* Turkish
* German
* French
* Spanish

Navigation, screens, settings, status messages, weekday labels, and timer/alarm notifications follow the selected language.

## Technology

* Kotlin
* Jetpack Compose
* Material 3
* Room 2.8
* Kotlin Coroutines and Flow
* Android `AlarmManager`
* Android notifications and foreground services
* JUnit timer-engine tests

## Requirements

* Android 6.0 or later (`minSdk = 23`)
* Target SDK 35
* JDK 17 for development
* Android Studio with Gradle support

## Open in Android Studio

1. Download or clone the repository.
2. Open the `NagomiNative` folder as an Android Studio project.
3. Wait for Gradle Sync to finish.
4. Select the `app` run configuration.
5. Run the app on an Android phone, tablet, or emulator.

Debug builds use the following package ID:

`com.sklabs.nagomi.native`

This allows the debug build to be installed beside an existing release build.

Release builds use:

`com.sklabs.nagomi`

## Install the APK

1. Open the repository’s **Releases** section.
2. Download the latest Nagomi Mobile APK.
3. Open the downloaded APK on your Android device.
4. If requested, allow installation from the application used to download the APK.
5. Complete the installation.

## Updating From the Legacy KivyMD App

The native app can perform a one-time import of compatible legacy `app_data.json` content, including:

* Subjects
* Study tasks
* Focus-session history
* Application settings
* Active task queue
* Saved timer state

Migration requires all of the following:

* The native release must use the same package ID: `com.sklabs.nagomi`.
* It must be installed as an update over the existing application.
* It must be signed with the same signing key.
* The existing application must not be uninstalled before the update.

Debug builds cannot access the release application’s private data because they use a different package ID.

## Current Version

`v0.8.1`

Developed by **SKLabs™**.
