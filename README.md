# Notes v2

Offline-first personal notes app seeded from JSONPlaceholder, with create, edit and delete.

## Requirements
- JDK 17+, Android SDK (API 37), Android Studio (latest stable)

## Run
```bash
./gradlew :app:installStageDebug   # stage environment; prod: installProdDebug
```

## Configuration
Each environment flavor (`stage`, `prod`) has its own API values. Override them in `~/.gradle/gradle.properties`
or with `-P` in CI (never commit secrets):
```properties
api.stage.baseUrl=https://stage-api.example.com/v1/
api.prod.baseUrl=https://api.example.com/v1/
api.prod.certPins=sha256/AAAA...,sha256/BBBB...
```
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
| `core/network` | Retrofit/OkHttp, `safeApiCall` with backoff |
| `core/testing` | Test rules and fakes |
| `feature/*` | One module per feature |

See `docs/SPEC.md` for features and `docs/audit/` for the latest code audit.

**What is left to do** (Firebase files, GitHub secrets, store listing, keys…) is in `docs/HANDOVER.xlsx`. Mark items
as Done in the Status column; `python3 tools/handover_check.py` re-checks what it can and keeps your statuses.
