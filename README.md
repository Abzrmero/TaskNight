# TaskNight

An Android app for planning tasks in the evening and reviewing them the next day.

Users set tasks for tomorrow, check them off during the day, and complete an evening log with reflections and a mood rating. A history screen shows past days on a calendar.

## Tech
- Kotlin, Jetpack Compose
- Firebase Authentication and Firestore
- Hilt for dependency injection, Clean Architecture (data / domain / presentation)

## Running it
1. Open the project in Android Studio.
2. Create a Firebase project, add an Android app with package name `com.example.tasknight`, and download your own `google-services.json` into `app/`. The one in this repo is a placeholder.
3. Build and run on an emulator or device (min SDK as set in `app/build.gradle.kts`).
