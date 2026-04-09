# DesignSpark

An Android app that helps UX designers and HCI practitioners kick-start research projects. Describe your project idea, user group, and design stage — DesignSpark calls the Anthropic API and returns structured HCI insights: user personas, research method cards, assumptions to test, and a recruit brief. All data is persisted offline-first in Room so insights are always available without a network connection.

---

## Architecture

MVVM + Clean Architecture in three layers:

```
UI (Jetpack Compose)
  ↓ events / state
ViewModels (Hilt, StateFlow)
  ↓ use cases
Domain (pure Kotlin — no Android deps)
  ↓ repository interface
Data (Room + Retrofit)
```

- **UI layer** — Compose screens observe a single `UiState` data class via `collectAsStateWithLifecycle()`. No business logic in composables.
- **Domain layer** — one use case per action (`CreateProjectUseCase`, `GenerateInsightsUseCase`, etc.). Repository interface defined here; implementation lives in the data layer.
- **Data layer** — `ProjectRepositoryImpl` is the single source of truth. Room is always written to before any data reaches the UI. Retrofit calls are made on `Dispatchers.IO`; the UI always reads from Room `Flow`s.
- **DI** — Hilt throughout (`@HiltViewModel`, `@InstallIn(SingletonComponent::class)`).

---

## Setup

### API key

1. Copy `local.properties.example` to `local.properties` (or create it if absent).
2. Add your Anthropic API key:
   ```
   ANTHROPIC_API_KEY=sk-ant-...
   ```
3. The key is injected at build time via `BuildConfig.ANTHROPIC_API_KEY` and sent in the `x-api-key` header. It is never committed to version control (`.gitignore` excludes `local.properties`).

### Build

Open the project in Android Studio Meerkat (or newer) and sync Gradle. The app targets API 35 with a minimum of API 26.

```
# From the project root:
./gradlew assembleDebug
./gradlew test                  # unit tests
./gradlew connectedAndroidTest  # instrumented tests (requires a device/emulator)
```

---

## Offline behaviour

Room is the source of truth. If insights have already been generated, all screens load instantly without a network connection. The **Generate Insights** action is the only operation that requires internet — the app shows a clear "No internet connection" error and a Retry button if the device is offline at that point.

---

## Screenshot

_Add a screenshot here once the app is running._

![App screenshot placeholder](docs/screenshot.png)
