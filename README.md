# Endless Calendar

A native Android calendar app (Kotlin + Jetpack Compose + Room + WorkManager) with
effectively-endless scrolling in month, week, and day/agenda views.

## Features
- Infinite scroll (~2000 years past/future) in month, week, and day/agenda views
- Recurring events (daily/weekly/monthly/yearly, custom interval, BYDAY, COUNT/UNTIL)
- Multi-day and all-day events, color tags, notes and location
- Multiple reminders per event via WorkManager, restored after reboot
- Device calendar read/merge and write via CalendarContract
- Home screen widget showing today's agenda
- Full-text search across all events, past and future
- Public holiday overlay (US / UK / South Africa) computed on-device, no network
- .ics import/export
- Material 3 theming with dynamic color and dark/light modes

## Build
This app is built entirely through GitHub Actions (see `.github/workflows/android-build.yml`).
Every push to `main` produces a debug APK artifact; pushing a `v*` tag also produces a
release APK and publishes both as a GitHub Release.

No local Gradle wrapper is committed — CI provisions Gradle directly via
`gradle/actions/setup-gradle`.
