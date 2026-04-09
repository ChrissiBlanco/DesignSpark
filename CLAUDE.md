# DesignSpark — Claude Code instructions

## Project overview
DesignSpark is an Android app that helps UX designers and HCI practitioners kick-start
projects. The user enters a project idea, user group, context, and stage. The app calls
the Anthropic API and returns structured HCI insights: personas, method cards,
assumptions to test, and a recruit brief. All data is persisted offline-first in Room.

---

## Architecture — non-negotiable rules

- **MVVM + clean architecture, always.** Three layers: UI → Domain → Data. Never skip the
  domain layer, even for simple operations.
- **UI layer**: Jetpack Compose only. No XML layouts. No business logic in composables —
  composables observe state and emit events, nothing more.
- **ViewModels**: expose a single `UiState` data class via `StateFlow`. Use
  `collectAsStateWithLifecycle()` in composables, never `collectAsState()`.
- **Domain layer**: pure Kotlin — zero Android imports. One use case per action.
  Use cases are the only callers of the repository interface.
- **Data layer**: `ProjectRepository` is the single source of truth. UI never touches
  Room DAOs or the Retrofit service directly — always go through the repository.
- **Dependency injection**: Hilt throughout. No manual DI, no service locators.

---

## Offline-first strategy

Room is the source of truth. The flow for AI generation is always:

1. Call Anthropic API via Retrofit (coroutine, `Dispatchers.IO`)
2. Parse JSON response into domain models
3. Write all insights to Room in a single transaction
4. UI observes Room via `Flow` — it never reads directly from the API response

Never return API data to the UI without persisting it first.

---

## Tech stack — use these and nothing else unless asked

| Concern | Library |
|---|---|
| UI | Jetpack Compose + Material 3 |
| Navigation | Compose Navigation |
| DI | Hilt |
| Async | Kotlin Coroutines + Flow |
| Local DB | Room |
| HTTP | Retrofit + OkHttp |
| JSON | Gson |
| Image loading | Coil |
| Testing | JUnit 4, MockK, Turbine (Flow testing) |

Do not introduce new dependencies without being explicitly asked.

---

## Code style

- **Kotlin idioms only.** No Java-style code.
- `Flow` over `LiveData` everywhere.
- Sealed classes or sealed interfaces for UI state variants (loading, success, error).
- Data classes for all models — domain models and Room entities are separate classes,
  never the same class.
- Extension functions are fine; keep them in a dedicated `Extensions.kt` per feature.
- No `!!` (non-null assertion). Use `?:`, `?.let`, or explicit null checks.
- Coroutine scope in ViewModels: always `viewModelScope`. Never `GlobalScope`.
- Mark all `suspend` functions that do I/O with `withContext(Dispatchers.IO)` inside
  the repository — ViewModels and use cases should not specify dispatchers.

---

## Package structure

```
com.designspark
├── di/                   # Hilt modules
├── domain/
│   ├── model/            # Pure Kotlin data classes (Project, Insight, etc.)
│   ├── repository/       # Repository interfaces
│   └── usecase/          # One file per use case
├── data/
│   ├── local/
│   │   ├── dao/          # Room DAOs
│   │   ├── entity/       # Room entities
│   │   └── AppDatabase.kt
│   ├── remote/
│   │   ├── api/          # Retrofit service interfaces
│   │   └── dto/          # API response data classes
│   └── repository/       # Repository implementations
└── ui/
    ├── navigation/        # NavGraph, routes
    ├── screens/           # One folder per screen: Screen.kt + ViewModel.kt
    ├── components/        # Shared composables
    └── theme/             # Color, Type, Theme
```

When adding a new feature, always create files in all three layers — never add UI
without the corresponding domain and data layer code.

---

## Domain models

```kotlin
// Project.kt
data class Project(
    val id: String,           // UUID
    val title: String,
    val userGroup: String,
    val context: String,
    val stage: ProjectStage,
    val createdAt: Long,
    val updatedAt: Long,
    val status: ProjectStatus,
    val isSynced: Boolean
)

enum class ProjectStage { NOTHING, ROUGH_IDEA, PROTOTYPE }
enum class ProjectStatus { DRAFT, GENERATED, ANNOTATED }

// GeneratedInsight.kt
data class GeneratedInsight(
    val id: String,
    val projectId: String,
    val type: InsightType,
    val title: String,
    val content: String,      // JSON string — parse per type at the UI layer
    val riskLevel: RiskLevel?, // only for ASSUMPTION type
    val orderIndex: Int,
    val generatedAt: Long
)

enum class InsightType { PERSONA, METHOD_CARD, ASSUMPTION, RECRUIT_BRIEF }
enum class RiskLevel { HIGH, MEDIUM, LOW }

// Annotation.kt
data class Annotation(
    val id: String,
    val insightId: String,
    val note: String,
    val createdAt: Long
)
```

Always use these exact models. Do not rename fields without being asked.

---

## Anthropic API integration

### Endpoint
`POST https://api.anthropic.com/v1/messages`

### Required headers
```
x-api-key: {API_KEY from BuildConfig}
anthropic-version: 2023-06-01
Content-Type: application/json
```

Store the API key in `local.properties` as `ANTHROPIC_API_KEY`. Read it via
`BuildConfig.ANTHROPIC_API_KEY`. Never hardcode it or commit it to version control.
Add `local.properties` to `.gitignore`.

### Request structure
```json
{
  "model": "claude-sonnet-4-20250514",
  "max_tokens": 1500,
  "system": "<system prompt — see below>",
  "messages": [
    {
      "role": "user",
      "content": "Project title: {title}\nUser group: {userGroup}\nContext: {context}\nStage: {stage}"
    }
  ]
}
```

### System prompt — copy exactly, do not paraphrase
```
You are a senior HCI researcher. Given a project brief, return ONLY valid JSON with
no preamble, no markdown, no code fences — raw JSON only.

Use this exact shape:
{
  "personas": [
    { "name": "", "age": 0, "role": "", "goal": "", "frustration": "" }
  ],
  "methodCards": [
    { "method": "", "whyThisFits": "", "estimatedTime": "" }
  ],
  "assumptionsToTest": [
    { "assumption": "", "risk": "HIGH|MEDIUM|LOW", "rationale": "" }
  ],
  "recruitBrief": {
    "whoToFind": "",
    "screenFor": "",
    "exclude": ""
  }
}

Rules:
- Generate exactly 3 personas, 3 method cards, 4 assumptions sorted HIGH to LOW risk,
  and 1 recruit brief.
- Tailor every output specifically to the user group, context, and stage provided.
- No generic advice. Each output must only make sense for THIS project.
- Method cards must explain WHY this method fits this specific user group and stage,
  not just name the method.
- Assumptions must identify real risks in this idea, not textbook placeholders.
```

### Parsing
Parse the response content string with Gson. Strip any accidental Markdown fences
before parsing:
```kotlin
val raw = response.content.first().text
    .replace("```json", "")
    .replace("```", "")
    .trim()
val dto = Gson().fromJson(raw, InsightResponseDto::class.java)
```

---

## Testing expectations

Every ViewModel and use case must have unit tests. Every DAO must have an
instrumented Room test using an in-memory database.

- Use `MockK` for mocking, not Mockito.
- Use `Turbine` for testing `Flow` emissions.
- Test file mirrors source file location: `test/` for unit, `androidTest/` for Room.
- Aim for 80%+ coverage on domain and data layers.
- ViewModels: always test loading → success and loading → error paths.

---

## What NOT to do

- Do not use `LiveData` anywhere.
- Do not put logic in composables — move it to the ViewModel.
- Do not access Room DAOs from ViewModels directly.
- Do not use `GlobalScope` or `runBlocking` in production code.
- Do not hardcode strings — use `strings.xml`.
- Do not add `!!` to pass compilation — fix the nullability properly.
- Do not create god-classes or god-ViewModels — one ViewModel per screen.
- Do not return API DTOs to the UI — always map to domain models first.
