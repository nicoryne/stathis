# 🌐 Stathis Mobile

Welcome to the **Stathis** mobile directory! This directory contains the **Kotlin** mobile application, designed to provide most of Stathis' core features such as motion recognition, vitals tracking, and the gamified learning.

## 📁 Directory Structure

```
/stathis-mobile
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/cit/edu/stathis/mobile       # Kotlin source files (packages, activities, viewmodels)
│   │   │   ├── res/                              # UI resources (layouts, drawables, strings, etc.)
│   │   │   └── AndroidManifest.xml               # App manifest
│   └── build.gradle                              # App-level Gradle config
├── build.gradle                                  # Project-level Gradle config
├── gradle.properties                             # Gradle properties
├── gradlew                                       # Gradle wrapper
├── gradlew.bat                                   # Gradle wrapper (Windows)
├── settings.gradle                               # Gradle settings
├── .env.example                                  # Environment variable template
├── README.md                                     # Mobile documentation

```

## 🛠️ Setup & Installation

> **Prerequisites:**
>
> - Android Studio installed
> - JDK 11+
> - Kotlin 1.8+

### 1️⃣ Clone the Repository

```sh
git clone https://github.com/nicoryne/stathis.git
cd stathis/mobile
```

### 2️⃣ Open in Android Studio

- Launch Android Studio and select Open an existing project

- Navigate to the cloned stathis-mobile directory and open it

### 3️⃣ Configure Environment Variables

```sh
cp .env.example .env
```

- Fill in the required Supabase credentials (API keys, database URL, etc.)

### 4️⃣ Build & Run the App

- Connect a device or start an emulator

- Click Run (▶️) in Android Studio or use:

```sh
./gradlew installDebug
```

- Open `http://localhost:3000` in your browser.

## 🎨 UI & Architecture

- **Language:** Kotlin
- **UI Toolkit:** XML Layouts
- **Architecture:** MVVM with Single-Activity Architecture
- **Navigation:** Jetpack Navigation Component
- **Dependency Injection:** Dagger-Hilt
- **Backend:** Supabase

## 🛡️ Security & Best Practices

- Avoid committing secrets or keys — use .env or local properties.
- Use ViewModel to separate logic from UI.
- Follow modern Android development guidelines (Jetpack, Coroutines, etc.).
- Handle permissions and user data responsibly.

## 📌 Contributing

We welcome contributions! Feel free to open issues, submit pull requests, or reach out to the team.

## ✅ Testing

Run JVM unit tests:

```sh
./gradlew test
```

Optional: real backend smoke test (opt-in via env vars). If not set, the test skips.

```sh
export STATHIS_API_BASE_URL="https://api.example.com/"  # must end with '/'
export STATHIS_TEST_EMAIL="test@example.com"
export STATHIS_TEST_PASSWORD="password123"

./gradlew :app:test --tests citu.edu.stathis.mobile.integration.BackendSmokeTest
```
