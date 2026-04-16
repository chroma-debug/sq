# THE WARDEN

> **A high-stakes, brutalist Android focus and lockdown application.**

![Build Status](https://github.com/YOUR_USERNAME/TheWarden/actions/workflows/android.yml/badge.svg)

---

## OVERVIEW

The Warden is an uncompromising Android productivity enforcer. Once a session begins, it monitors every app you open and every URL you visit. If you stray into a blacklisted app or website, a full-screen black overlay appears with a single message: **GET BACK TO WORK.** The only escape is to go home — or prove you've been studying by photographing your handwritten notes.

---

## FEATURES

| Feature | Description |
|---|---|
| **App Blacklist** | Toggle any installed app to be blocked during sessions |
| **URL Blacklist** | Block specific domains in Chrome, Firefox, and Brave |
| **AccessibilityService** | Monitors foreground app and URL bar in real time |
| **Full-Screen Overlay** | Non-dismissible black lockscreen when blocked app is opened |
| **Smart Scheduling** | Weekly schedule builder with per-day start/end times |
| **Break Mode** | Automatic breaks at configurable intervals |
| **Proof of Work** | Camera unlock: photograph handwritten notes for AI verification |
| **Encrypted API Keys** | API keys stored in `EncryptedSharedPreferences` (AES-256-GCM) |
| **Brutalist UI** | Pure black/white, bold all-caps typography, zero rounded corners |
| **Auto-Build CI** | GitHub Actions builds a Debug APK on every push to `main` |

---

## SETUP

### 1. Required Permissions

After installing, open **THE WARDEN → SETTINGS** and grant all three permissions:

1. **Overlay Permission** — Required to show the lock screen over other apps
2. **Accessibility Service** — Required to monitor foreground apps and URL bars
3. **Usage Stats** — Required for additional app usage monitoring

### 2. Configure AI API Key (for Proof of Work)

In **SETTINGS**, enter your OpenAI-compatible API key. The key is stored encrypted on-device using Android Keystore + AES-256-GCM. It is never transmitted except to the API endpoint you configure.

- **Default endpoint:** `https://api.openai.com/v1`
- **Compatible with:** OpenAI, Azure OpenAI, any OpenAI-compatible API

### 3. Set Up Your Blacklists

- **APP BLACKLIST** — Toggle apps you want blocked (e.g., Instagram, TikTok, YouTube)
- **URL BLACKLIST** — Add domains (e.g., `reddit.com`, `twitter.com`, `youtube.com`)

### 4. Configure Schedule (Optional)

In **SCHEDULE**, enable schedule enforcement and set active hours for each day of the week. Configure break intervals and durations. The Warden will only enforce blocks during scheduled hours.

### 5. Start a Session

Tap **START SESSION** on the main screen. The Warden foreground service activates and begins monitoring.

---

## HOW BLOCKING WORKS

```
User opens blocked app
        ↓
AccessibilityService detects TYPE_WINDOW_STATE_CHANGED
        ↓
Checks: Session active? Break mode? Schedule window?
        ↓
Package in App Blacklist? → YES → Show LockOverlayActivity
        ↓
User opens Chrome → URL bar scraped → Domain in URL Blacklist? → YES → Show LockOverlayActivity
```

The lock overlay is a full-screen Activity launched with `FLAG_ACTIVITY_NEW_TASK`. It intercepts the back button and redirects to the home screen. It cannot be dismissed by the user — only by exiting the blocked app or providing Proof of Work.

---

## PROOF OF WORK UNLOCK

1. On the lock screen, tap **UNLOCK WITH PROOF OF WORK**
2. The camera opens — photograph your handwritten study notes
3. The image is sent to your configured AI API (vision model)
4. The AI evaluates whether genuine handwritten study content is visible
5. If accepted: **5-minute break** is granted and the lock is dismissed
6. If rejected: **"INSUFFICIENT PROOF. KEEP WORKING."**

---

## BUILDING

### Prerequisites

- Android Studio Hedgehog or newer
- JDK 17
- Android SDK 34

### Build Debug APK

```bash
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

### GitHub Actions

Every push to `main` automatically:
1. Builds a Debug APK
2. Uploads it as a build artifact (retained 30 days)
3. Runs unit tests

---

## PROJECT STRUCTURE

```
TheWarden/
├── .github/workflows/android.yml     # CI/CD pipeline
├── app/src/main/
│   ├── AndroidManifest.xml
│   ├── java/com/warden/app/
│   │   ├── WardenApplication.kt
│   │   ├── data/
│   │   │   ├── db/                   # Room DAOs and Database
│   │   │   ├── model/                # Entity models
│   │   │   └── repository/           # Repository + Preferences
│   │   ├── service/
│   │   │   ├── WardenAccessibilityService.kt   # Core blocking engine
│   │   │   ├── WardenForegroundService.kt      # Session persistence
│   │   │   ├── ScheduleChecker.kt              # Schedule logic
│   │   │   ├── BootReceiver.kt
│   │   │   └── ScheduleAlarmReceiver.kt
│   │   └── ui/
│   │       ├── main/                 # Main hub screen
│   │       ├── apps/                 # App blacklist screen
│   │       ├── urls/                 # URL blacklist screen
│   │       ├── schedule/             # Weekly schedule builder
│   │       ├── settings/             # API keys + permissions
│   │       ├── unlock/               # Camera proof-of-work
│   │       └── overlay/              # Lock screen overlay
│   └── res/
│       ├── layout/                   # All XML layouts
│       ├── values/                   # Colors, strings, themes
│       ├── xml/                      # Accessibility service config
│       └── drawable/                 # Vector assets
```

---

## AESTHETIC

The Warden follows strict brutalist design principles:

- **Background:** `#000000` (pure black)
- **Text:** `#FFFFFF` (pure white)
- **Accent:** `#FF0000` (danger/blocked), `#00FF00` (active/success)
- **Typography:** `sans-serif-black`, all-caps, bold, tracked
- **Corners:** 0dp (no rounding)
- **Icons:** None
- **Animations:** None

---

## LICENSE

MIT License. Use it. Modify it. Ship it.
