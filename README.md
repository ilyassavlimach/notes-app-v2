# Notes v2

Offline-first personal notes app seeded from JSONPlaceholder, with create, edit and delete.

## Requirements
- JDK 17 (the Gradle daemon runs on 17, see `gradle/gradle-daemon-jvm.properties`), Android SDK (API 37),
  Android Studio (latest stable). Robolectric tests use a JDK 21 toolchain, provisioned automatically.

## Features
- **Notes list:** seeded from JSONPlaceholder `GET /posts` into Room, which is the source of truth. Pull to refresh;
  notes you created, edited or deleted are never overwritten or brought back by a refresh. Add button, "Note
  deleted · Undo" snackbar, and a notification-permission card once you have notes of your own.
- **Note detail:** read a note, edit it, delete it with confirmation.
- **Note editor:** create or edit (title required, max 100; text max 5 000), unsaved-changes guard, typed text
  survives process death.
- **App gate:** Firebase Remote Config `min_version_code` / `maintenance_mode` show a force-update or maintenance
  screen.
- **Deep links:** `https://<host>/notes/{id}` (stage: `stage.notes.example.com`, prod: `notes.example.com` —
  placeholders) and notification taps open the note.
- **Integrations:** Crashlytics (release), Firebase Analytics + Adjust + AppsFlyer (events `note_opened`,
  `note_created`, `note_edited`, `note_deleted`), FCM push.
- **Languages:** English (default) and Turkish, per-app language on Android 13+.

## Run
```bash
./gradlew :app:installStageDebug   # stage environment; prod: installProdDebug
```

## Configuration
Each environment flavor (`stage`, `prod`) has its own API values. Override them in `~/.gradle/gradle.properties`
or with `-P` in CI (never commit secrets):
```properties
api.stage.baseUrl=https://jsonplaceholder.typicode.com/
api.prod.baseUrl=https://jsonplaceholder.typicode.com/
api.prod.certPins=sha256/…,sha256/…          # intermediate + root + backup roots; empty = pinning off
analytics.adjust.appToken=…                  # blank = Adjust off
analytics.adjust.eventTokens=note_opened:abc123,…
analytics.appsflyer.devKey=…                 # blank = AppsFlyer off
```
Re-check the pins whenever the host changes its certificate authority (commands in `docs/SPEC.md` → Hardening).
`app/google-services.json` is a placeholder until the real Firebase project exists (`docs/HANDOVER.xlsx`).
Debug builds include Chucker (network inspector, notification "Recording HTTP activity") and LeakCanary; release
builds contain neither.

## Quality checks
```bash
./gradlew assembleDebug detekt ktlintCheck lint testDebugUnitTest testStageDebugUnitTest koverVerify
python3 tools/check_rules.py .
```

## Release
Push a `v*` tag. CI builds a signed AAB using the `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`
and `KEY_PASSWORD` repository secrets. Locally, export the same variables plus `KEYSTORE_PATH`.

## Modules
| Module | Purpose |
|---|---|
| `app` | Application, MainActivity, navigation host |
| `core/common` | `AppError`/`AppResult`, dispatcher qualifiers |
| `core/model` | Pure Kotlin domain models |
| `core/designsystem` | Theme, tokens, generic components |
| `core/ui` | Shared app components (loading/empty/error states) |
| `core/i18n` | All user-facing strings, one file per feature |
| `core/network` | Retrofit/OkHttp, `NotesApi`, `safeApiCall` with backoff, certificate pinning, Chucker (debug) |
| `core/database` | Room `NotesDatabase`, `NoteDao` (merge that protects user changes), exported schemas |
| `core/data` | `NotesRepository` (offline-first), injected `Clock` |
| `core/analytics` | `AnalyticsTracker` composite with Firebase, Adjust and AppsFlyer providers |
| `core/notifications` | FCM service, channels, `rememberNotificationPermissionState` |
| `core/config` | Remote Config defaults and the force-update / maintenance gate |
| `core/testing` | Test rules and fakes (`FakeNotesRepository`, `FakeAnalyticsTracker`) |
| `feature/noteslist` | Notes list, undo, notification rationale |
| `feature/notedetail` | Note detail, delete |
| `feature/noteeditor` | Create / edit note |

See `docs/SPEC.md` for features and `docs/audit/` for the latest code audit.

**What is left to do** (Firebase files, GitHub secrets, store listing, keys…) is in `docs/HANDOVER.xlsx`. Mark items
as Done in the Status column; `python3 tools/handover_check.py` re-checks what it can and keeps your statuses.
