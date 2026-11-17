# 🎓 AskFaculty — Dual-Role University Academic Mentorship Platform (Android)

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-purple.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-blue.svg)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20%2F%20MVVM-green.svg)](https://developer.android.com/topic/architecture)
[![Firebase](https://img.shields.io/badge/Firebase-Auth%20%7C%20Firestore%20%7C%20Storage%20%7C%20FCM-orange.svg)](https://firebase.google.com)
[![License](https://img.shields.io/badge/License-Academic-lightgrey.svg)](#license)

**AskFaculty** is a production-grade, dual-role academic mentorship and doubt resolution platform built for universities, colleges, and educational institutions. Developed with modern **Jetpack Compose**, **Material 3**, **Kotlin Coroutines / StateFlow**, **Dagger Hilt**, and **Firebase**, AskFaculty bridges the gap between students and professors through structured doubt queues, collaborative academic spaces, verified faculty answers, curated learning resources, and secure server-assisted AI tools.

---

## 👥 Dual-Role System Overview

AskFaculty supports two primary user roles with dedicated interfaces, capabilities, and navigation flows:

### 🎓 1. Student Application
* **Authentication & Onboarding**:
  * Dual tabbed login: Student College ID / Institutional Email + Password.
  * Complete Student Registration with College ID, Department, and Academic Year / Semester.
  * Institutional password recovery.
  * 3-step interactive onboarding walkthrough with Next, Skip, and Get Started navigation.
* **Student Home Dashboard**:
  * Personalized greeting and student profile overview.
  * Real-time KPI metric cards: *Pending Doubts*, *Answered Doubts*, *Joined Spaces*.
  * Registered subject pills with instant doubt filtering.
  * Pinned faculty announcements and urgent exam notices.
  * Recent faculty answers showcase with one-tap access.
  * Floating Quick "Ask a Doubt" FAB.
* **Complete "Ask a Doubt" Workflow**:
  * Academic Subject & Faculty Member dropdown selector.
  * Topic and detailed question input.
  * **Code Snippet Support**: Paste code with language selection (C++, Java, Python, Kotlin, SQL) rendered in dark monospace blocks.
  * **Speech-to-Text Voice Input**: Integrated Android Speech Recognizer for voice dictation.
  * **File & Image Attachments**: Upload PDFs, documents, or screenshots via Firebase Storage.
  * **Privacy Controls**: Choose between Private submission (visible only to assigned faculty) or Space submission (visible to course community).
* **My Doubts Management & Follow-up**:
  * Filter queue by *All*, *Pending*, *Answered*, and *Resolved*.
  * Search doubts by topic or keyword.
  * Interactive discussion thread with faculty responses and student follow-ups.
  * **Student Resolution Banner**: Mark answered doubts as Resolved when satisfied.
  * **Reopen Doubt Workflow**: Submit a reopening reason if further clarification is needed.
  * **Helpful Feedback**: Tap "Helpful 👍" on faculty answers.
* **Academic Spaces & Communities**:
  * Explore All Spaces vs Joined Spaces tabs.
  * Course & subject filter chips.
  * Join and leave course spaces.
  * Read community discussions, view pinned posts, and verify faculty answers.
* **Academic Resources & Slides**:
  * Faculty-curated lecture slides, syllabus guides, PDF notes, and reference links.
  * Filter by subject or resource type (PDFs, Slides, Reference Links).
  * Direct file viewing/downloading via system intent launcher.
* **Global Search & Bookmarks**:
  * Universal search across doubts, faculty answers, community discussions, and study materials.
  * Dedicated **Saved Screen** for offline reference of bookmarked discussions.
* **AskFaculty AI Assistant (Phase 4)**:
  * **Concept Explainer**: Rapid conceptual breakdown with "Escalate to Faculty" handoff.
  * **Question Rephraser**: Improve unclear student questions before submission.
  * **Study Notes Generator**: Generate structured revision notes by subject and topic.
  * **Practice Questions**: Generate conceptual MCQs for exam preparation.
* **Student Profile & Settings**:
  * Academic identity card (College ID, Department, Year, Semester).
  * Activity metrics (Doubts Asked, Resolved, Spaces Joined).
  * Granular push notification preferences (Doubt replies, Space notices, New notes).

---

### 👨‍🏫 2. Faculty Portal
* **Authentication**: Strict 5-digit Faculty ID (`30456`) login with institutional verification.
* **Executive Dashboard**:
  * High-priority doubts counter and pending reviews.
  * Weekly resolution trends chart.
  * Urgent doubt cards with priority badges (🔴 High, 🟡 Medium, 🟢 Low).
* **Doubt Management Queue**:
  * Searchable queue with status tabs (*Pending*, *In Progress*, *Answered*, *Resolved*).
  * Priority filter bottom sheet and subject chips.
* **Official Doubt Response & Verification**:
  * Two-way discussion thread.
  * **"Mark as Official Answer"**: Star verified faculty answers with green checkmark border.
  * **AI Teaching Assistant**: Generate doubt synopsis and draft responses to review and edit before sending.
  * Status and priority adjuster sheet.
* **Community Space Administration**:
  * Create new academic spaces with custom subjects and descriptions.
  * Pin announcements and important discussion posts.
* **Faculty Availability & Office Hours**:
  * Real-time availability status (*Available*, *Busy*, *Away*).
  * Weekly scheduled office hours (Day, Start Time, End Time, Location).

---

## 🔑 Demo & Test Credentials

For quick evaluation without provisioning institutional credentials in Firebase Auth:

| Role | Identifier / ID | Password | Access / Notes |
|---|---|---|---|
| **Student** | `22CS104` (or `student@askfaculty.edu`) | `Student@123` | Full Student Application, Dashboard, Ask a Doubt, Saved, AI Assistant |
| **Faculty** | `30456` | `Gow@1415` | Full Faculty Portal, Priority Doubt Queue, Official Verification, AI Panel |

---

## 🏗️ Clean Architecture & Project Structure

The project strictly follows **Clean Architecture** and **MVVM** principles with unidirectional data flow (UDF):

```
app/src/main/java/com/example/askfaculty/
├── core/
│   ├── firebase/          # Firebase DI module, FCM service, Firestore collections
│   ├── network/           # Network connectivity monitor
│   ├── ui/components/     # Design token components (Buttons, Chips, Cards, States)
│   └── util/              # Constants, extensions, date formatters
├── data/
│   ├── model/             # DTO data transfer objects
│   ├── remote/            # AiDataSource (Cloud Function endpoints with secure fallbacks)
│   └── repository/        # Repository implementations with Firebase & demo fallbacks
├── domain/
│   ├── model/             # Domain models (User, Doubt, Community, Resource, Notification)
│   └── repository/        # Repository contracts (Auth, Doubt, Community, Profile, etc.)
├── navigation/            # Routes, BottomNavBar, App / FacultyNavGraph
├── presentation/          # Feature UI screens and ViewModels
│   ├── ai/                # AiAssistantScreen & AiAssistantViewModel
│   ├── auth/              # LoginScreen, RegisterScreen, ForgotPasswordScreen
│   ├── communities/       # CommunitiesScreen, CommunityDetailScreen, CreatePost
│   ├── doubts/            # DoubtsScreen, DoubtDetailScreen, AskDoubtScreen
│   ├── home/              # HomeScreen (Faculty Dashboard)
│   ├── notifications/     # NotificationsScreen
│   ├── onboarding/        # OnboardingScreen
│   ├── profile/           # ProfileScreen, EditProfile, Availability
│   ├── resources/         # ResourcesScreen & ResourcesViewModel
│   ├── search/            # SearchScreen & SearchViewModel
│   ├── settings/          # SettingsScreen (Theme, Alerts)
│   ├── splash/            # SplashScreen & SplashViewModel
│   └── student/           # StudentHomeScreen, StudentDoubtsScreen, SavedScreen, Profile
└── ui/theme/              # Design tokens: Spacing, Dimensions, Color, Type, Theme
```

---

## 🎨 Design System & Accessibility

* **Material 3 Token System**:
  * Primary: `#4F46E5` (Indigo)
  * Secondary: `#7C3AED` (Violet)
  * Accent: `#0EA5E9` (Sky-Teal)
  * Semantic Statuses: Pending (`#D97706`), In Progress (`#4F46E5`), Answered (`#16A34A`), Resolved (`#7C3AED`)
* **Dark Mode**: Deliberate deep midnight background (`#0B1020`), elevated card surfaces (`#182235`), and high contrast text.
* **Component Library**: Unified cards (`DoubtCard`, `StatCard`), standardized button heights (`52.dp`), minimum touch targets (`48.dp`), and responsive layouts.

---

## 🔒 Security & Firestore Rules

* **Role Verification**: Backend enforcement ensuring only `role == "FACULTY"` can post official answers, delete resources, or manage space settings.
* **Private Doubts**: Private doubts are strictly readable only by the submitting student and assigned faculty member.
* **No API Keys in APK**: All AI features communicate with server-side Cloud Functions without storing private Gemini/OpenAI API keys in the client APK.
* **Firestore Rules**: Stored in `firestore.rules` and indexed via `firestore.indexes.json`.

---

## 🚀 Build, Test & Run

### Prerequisites
* **Android Studio** Ladybug (2024.2+) or Meerkat
* **JDK 17** (matching project toolchain)
* **Android SDK** API 35 or API 37

### 1. Build Debug APK
```powershell
./gradlew assembleDebug
```
Output APK location:
```
app/build/outputs/apk/debug/app-debug.apk
```

### 2. Run Unit Tests
```powershell
./gradlew testDebugUnitTest
```

### 3. Install on Connected Device / Emulator
```powershell
./gradlew installDebug
```

---

## 📄 License
Academic / Institutional use. Developed for university and college mentorship networks.
