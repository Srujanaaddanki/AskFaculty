# 🎓 AskFaculty — Faculty & Staff Portal (Android)

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-purple.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-blue.svg)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20%2F%20MVVM-green.svg)](https://developer.android.com/topic/architecture)
[![Firebase](https://img.shields.io/badge/Firebase-Auth%20%7C%20Firestore%20%7C%20Storage%20%7C%20FCM-orange.svg)](https://firebase.google.com)
[![License](https://img.shields.io/badge/License-Academic-lightgrey.svg)](#license)

**AskFaculty** is a university-grade Android application designed specifically for professors, faculty members, and academic mentors. Built with **Jetpack Compose**, **Material 3**, and **Clean Architecture**, it provides faculty with an organized, trustworthy platform to address student doubts, manage academic communities, and share learning resources.

---

## 📱 Screenshots & Overview

The application features a centralized, high-contrast academic design system with a deliberate dark mode, fluid micro-interactions, and real-time state synchronization.

* **Startup & Onboarding**: Smooth splash transition and a 3-step onboarding walkthrough explaining the mentorship workflow.
* **Institutional Authentication**: Strict 5-digit Faculty ID login with Firestore role validation.
* **Home Dashboard**: Executive overview with pending KPIs, quick actions, weekly resolution chart, and high-priority doubt cards.
* **Doubts Management**: Searchable, tab-filtered doubt queue with badge counts, shimmer loading states, and filter bottom sheet.
* **Interactive Doubt Thread**: Full chat thread, verified "✓ Official Faculty Answer" badges, AI assistant suggestion drafts, and document/image attachment previews.
* **Academic Spaces (Communities)**: Course-specific spaces with cycling gradient banners, member counters, and discussion channels.
* **Notifications**: Grouped chronologically (*Today*, *Yesterday*, *Earlier*) with swipe-to-dismiss and unread indicators.
* **Profile & Settings**: Faculty identity card, real-time availability toggle (*Available*, *Busy*, *Away*), scheduled office hours, and DataStore-persisted theme selection (*System Default*, *Light*, *Dark*).

---

## 🏗️ Architecture & Tech Stack

The project adheres to **Clean Architecture** and **MVVM** principles with unidirectional data flow (UDF):

```
app/
├── core/
│   ├── firebase/          # Firebase DI module, FCM service
│   ├── network/           # Network connectivity monitor
│   ├── ui/components/     # Reusable design system component library
│   └── util/              # Constants, date formatters, extensions
├── data/
│   ├── model/             # DTO data transfer objects
│   └── repository/        # Repository implementations with demo fallbacks
├── domain/
│   ├── model/             # Domain entities and UiState models
│   ├── repository/        # Repository abstractions/interfaces
│   └── usecase/           # Domain use cases (Auth, Doubts, Communities, Profile)
├── navigation/            # Routes, NavGraph, and Bottom navigation bar
├── presentation/          # Compose UI screens and ViewModel pairs
│   ├── auth/              # LoginScreen & ForgotPasswordScreen
│   ├── communities/       # CommunitiesScreen, Detail, Create Spaces
│   ├── doubts/            # DoubtsScreen & DoubtDetailScreen
│   ├── home/              # HomeScreen dashboard
│   ├── notifications/     # NotificationsScreen
│   ├── onboarding/        # OnboardingScreen
│   ├── profile/           # ProfileScreen & EditProfileScreen
│   ├── search/            # SearchScreen
│   ├── settings/          # SettingsScreen (Theme, Alerts)
│   └── splash/            # SplashScreen
└── ui/theme/              # Design tokens: Spacing, Dimensions, Color, Type, Theme
```

### Technology Highlights

| Layer | Technologies |
|---|---|
| **Language & Core** | Kotlin 2.0+, Kotlin Coroutines, StateFlow / Flow |
| **UI Framework** | Jetpack Compose, Material 3, Navigation Compose |
| **Dependency Injection**| Dagger Hilt with KSP |
| **Backend & Cloud** | Firebase Authentication, Cloud Firestore, Cloud Storage, Firebase Cloud Messaging (FCM) |
| **Local Storage** | Jetpack DataStore Preferences (Theme & Onboarding state) |
| **Image Loading** | Coil Compose |
| **Minimum SDK** | Android 7.0 (API Level 24) |
| **Target / Compile SDK**| Android 15 / 16 (API Level 35/37) |

---

## 🎨 Centralized Design System

AskFaculty uses a cohesive design token system rather than ad-hoc styling:

* **Color Palette** (`Color.kt`):
  * **Primary**: Indigo (`#4F46E5`)
  * **Secondary**: Violet (`#7C3AED`)
  * **Accent / Tertiary**: Sky-Teal (`#0EA5E9`)
  * **Dark Theme**: Deliberate deep midnight (`#0B1020`), navy surfaces (`#121A2A`), elevated (`#182235`)
  * **Semantic Status**: Pending (`#D97706`), In Progress (`#4F46E5`), Answered (`#16A34A`), Resolved (`#7C3AED`)
* **Spacing Tokens** (`AskFacultySpacing`): 8-point harmonic scale (`xs=4.dp`, `sm=8.dp`, `md=12.dp`, `lg=16.dp`, `xl=24.dp`, `xxl=32.dp`, `xxxl=48.dp`, `xxxxl=64.dp`).
* **Dimension Tokens** (`AskFacultyDimensions`): Standardized control heights (`buttonHeight=52.dp`, `textFieldHeight=56.dp`), avatar sizes (`32.dp` - `96.dp`), touch targets (`minTouchTarget=48.dp`).
* **Shapes & Radius** (`AskFacultyRadius`): `xs=4.dp`, `sm=8.dp`, `md=12.dp`, `lg=16.dp`, `xl=20.dp`, `xxl=24.dp`, `pill=50.dp`.
* **Component Library** (`CommonComponents.kt`): `AskFacultyButton`, `AskFacultyOutlinedButton`, `AskFacultyTextField`, `AskFacultySearchBar`, `AskFacultyTopBar`, `AskFacultyStatusChip`, `AskFacultyPriorityChip`, `AskFacultyAvatar`, `AskFacultySkeleton`, `AskFacultyEmptyState`, `AskFacultyErrorState`, `DoubtCard`, `StatCard`, `QuickActionCard`.

---

## 🔑 Demo & Test Credentials

For quick evaluation without provisioning institutional credentials in Firebase Auth:

| Field | Demo Credential |
|---|---|
| **Faculty ID** | `30456` |
| **Password** | `Gow@1415` |

> **Note**: Entering ID `30456` automatically accesses the demo fallback dataset containing priority doubts, active community spaces, notifications, and profile details.

---

## 🚀 Getting Started

### Prerequisites

* **Android Studio** Ladybug (2024.2+) or Meerkat
* **JDK 17** or higher
* **Android SDK** API 35 or 37 installed

### Setup & Run

1. **Clone the repository**:
   ```bash
   git clone https://github.com/Gowthami1214/AskFaculty.git
   cd AskFaculty
   ```

2. **Add Firebase configuration**:
   Ensure `app/google-services.json` is present in the `app/` directory with your Firebase project credentials.

3. **Build Debug APK**:
   ```powershell
   ./gradlew assembleDebug
   ```
   The generated APK will be available at:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

4. **Run Unit Tests**:
   ```powershell
   ./gradlew testDebugUnitTest
   ```

5. **Install on Connected Device / Emulator**:
   ```powershell
   ./gradlew installDebug
   ```

---

## 🔒 Security & Firestore Rules

* Role-based access ensures that only users with `role == "FACULTY"` can post official answers, change doubt resolution status, or administer academic spaces.
* Cloud Firestore security rules are defined in `firestore.rules` and indexed via `firestore.indexes.json`.

---

## 📄 License

Academic / Institutional use. Developed for university and college mentorship networks.
