# MascotasPerdidas — Fase 2 Implementation Plan (Pantallas 6–23)

## Context

The app MVP (Splash, Profile, OTP, Permissions, Feed, Settings) is done and wired to **real Firebase**. The `docs/` folder now specifies **Fase 2**: 12 new screens (Pantallas 6–23) grouped as Map, ReportDetail, a 4-step creation Wizard, ReportConfirmed, MyReports, and SightingsForPet — plus a navigation overhaul (bottom `NavigationBar`) and a domain extension (3-value `ReportType`, ~18 new `PetReport` fields, multi-image create, OSMDroid maps).

`docs/INSTRUCTIONS.md` is the declared **fuente de verdad** for this phase and **takes precedence** over the per-screen TechDocs and over `TECHNICAL_CONTEXT.md` on conflict. The per-screen docs (`Pantalla_*.md`) flagged open questions (Google Maps vs none, FOUND boolean vs enum, 1 vs N images); **INSTRUCTIONS.md already resolves them all**: OSMDroid for every map, `ReportType` = `{LOST, FOUND_SIGHTING, FOUND_IN_CARE}`, `CreateReport(report, List<ByteArray>)`, new `care` color token, bottom-nav `MainScaffold`.

Goal: a **functional MVP with everything implemented** (not hardened/complex), built in dependency order with a compile+preview checkpoint and a commit after each phase.

### Decisions (from user)
- **Backing:** Firebase directly (keep `RepositoryModule` bound to Firestore adapters; Fake repos only need to keep compiling).
- **Avisos tab:** placeholder `Próximamente` (`NotificationsPlaceholderScreen`). **SightingsForPet IS built** and reached from MyReports / ReportDetail, linking sightings to a LOST pet via a **species + zone heuristic** (no new model relation).
- **Verification:** `./gradlew :app:compileDebugKotlin` (or `assembleDebug`) + `detekt` + a `@Preview` per new screen, **after each phase**.
- **Auto commit + push** authorized after every green phase (record in `AGENTS.md`).

### Source of truth per area
- Order, nav code, components list, strings, seed coords → `docs/INSTRUCTIONS.md` (§2–§9).
- Screen detail/behavior → `docs/Pantalla_6_7_8…`, `…_12_13_14…`, `…_15_16_17_18…`, `…_19_21_22_23…`.
- OSMDroid usage → `docs/OSMdroid.md` + INSTRUCTIONS §1.
- Architecture rules (hexagonal, M3, no hardcoded color/string) → `CLAUDE.md` + `docs/TECHNICAL_CONTEXT.md`. **These remain inviolable.**

---

## Phase 0 — Prep & build setup

**0.1 Record auto-commit authorization** in `AGENTS.md` (one line: "Authorized to auto `git commit` + `git push` after each green phase per Fase 2 plan.").

**0.2 Add OSMDroid + AndroidX Preference deps**
- `gradle/libs.versions.toml`: add `osmdroid = "6.1.20"` (and `osmdroid = { group = "org.osmdroid", name = "osmdroid-android", version.ref = "osmdroid" }`); add `androidx.preference:preference-ktx` (needed by `Configuration.load`).
- `app/build.gradle.kts`: `implementation(libs.osmdroid)` + `implementation("androidx.preference:preference-ktx:1.2.1")`.

**0.3 OSMDroid init** in `app/MascotasPerdidasApp.kt` `onCreate()` per INSTRUCTIONS §1.2 (`Configuration.getInstance().load(...)`, `userAgentValue = packageName`).

**0.4 Manifest** (`app/src/main/AndroidManifest.xml`): INTERNET + ACCESS_FINE/COARSE_LOCATION already present. Add `WRITE_EXTERNAL_STORAGE` `maxSdkVersion="32"` for OSMDroid tile cache.

**Checkpoint:** project syncs/compiles. **Commit:** `chore: add osmdroid + preference deps, init map config`.

---

## Phase 2A — Domain & cascade (prerequisite of everything)

Order is strict; each sub-step breaks compilation until the cascade is finished, so do 2A as one unit, then compile.

**2A.1 `ReportType`** (`domain/model/ReportType.kt`) → `{ LOST, FOUND_SIGHTING, FOUND_IN_CARE }` (INSTRUCTIONS §2A.1).

**2A.2 `PetReport`** (`domain/model/PetReport.kt`) → add the ~18 new fields, **all with defaults** (INSTRUCTIONS §2A.2): `ownerName`, `species`, `size`, `gender`, `color`, `collarColor`, `ageApprox`, `microchip`, `hasCollarPlate`, `hasMicrochip`, `physicalStatus: List<String>`, `behaviors: List<String>`, `notes`, `additionalPhotos: List<String>`, `urgency`, `stillInArea`, `latitude: Double?`, `longitude: Double?`, `statuses: List<String>`.

**2A.3 `CreateReport` multi-image** (INSTRUCTIONS §2A.3) — cascade:
- `domain/port/in/CreateReport.kt` → `invoke(report, imageBytesList: List<ByteArray>)`.
- `domain/usecase/CreateReportImpl.kt` → pass through list.
- `domain/port/out/PetReportRepository.kt` → `createReport(report, imageBytesList: List<ByteArray>)`.
- `data/firebase/FirestorePetReportRepository.kt` → loop-upload list, first URL → `imageUrl`, rest → `additionalPhotos`.
- `data/fake/FakePetReportRepository.kt` → match signature (keeps compiling; not the active binding).

**2A.4 DTO + Mapper** (`data/dto/PetReportDto.kt`, `data/mapper/PetReportMapper.kt`):
- DTO: add all new fields with Firestore-safe defaults (`Double?` → store `null`/numbers; lists default `emptyList()`; booleans default `false`).
- Mapper `toDomain`: replace `if (type == "FOUND")` with safe parse → `LOST` / `FOUND_SIGHTING` / `FOUND_IN_CARE`, **legacy `"FOUND"` → `FOUND_SIGHTING`** (existing prod docs). Map every new field. `toDto`: serialize every new field; `type = type.name`.

**2A.5 `FakePetReportRepository` type-filter bug** — current `observeReports` returns **all** reports ignoring `type`; the new 3-type `combine` screens depend on correct filtering. Fix to `_reports.map { it.filter { r -> r.type == type } }`. (Firebase adapter already filters via `whereEqualTo("type", …)`, so it's correct.)

**2A.6 Seed data (Firestore)** — `FirestorePetReportRepository.seedReports`: change Luna → `FOUND_SIGHTING`, add a 3rd `FOUND_IN_CARE` seed (Rocky→in-care or new), add **Cúcuta lat/lng** to all (INSTRUCTIONS §6: `7.89..,-72.50..`), populate a few new fields (species, gender, color, statuses, urgency). Mirror in `FakePetReportRepository.seed` for parity.

**2A.7 Color token "care"** (`app/theme/Color.kt` + `app/theme/Theme.kt`) per INSTRUCTIONS §2A.4: add `Care`, `CareContainer`, `OnCare`, `OnCareContainer`; expose via `CompositionLocal` (`LocalCareColor`, `LocalCareContainerColor`) since M3 `ColorScheme` has no slot. Provide them in `MascotasPerdidasTheme`.

**2A.8 `StatusChip`** (`app/ui/components/StatusChip.kt`) → 3-branch `when`: `LOST`→`chip_perdido`(error), `FOUND_SIGHTING`→`chip_avistado`(tertiary), `FOUND_IN_CARE`→`chip_resguardo`(care/tertiary). Update its two previews.

**2A.9 `UserAvatar`** (`app/ui/components/UserAvatar.kt`) → add `containerColor: Color = MaterialTheme.colorScheme.primaryContainer` param. New helper `app/util/AvatarColorUtils.kt#colorFromUid(uid, colorScheme)` (INSTRUCTIONS §2A.5 / Pantalla_19_21_22_23 §7.2).

**2A.10 Fix existing call sites that won't compile** from the 2-value→3-value change:
- `FeedScreen.kt` imports `ReportType.FOUND` + `CreateReportDialog` uses `LOST/FOUND` toggle → will be replaced in Phase 2D-2; for now make it compile (Avistadas tab handling).
- `FeedViewModel.kt` `createReport(report, null)` → `createReport(report, emptyList())`; Avistadas tab must merge `FOUND_SIGHTING + FOUND_IN_CARE` (combine two flows) since the tab is binary but the enum is ternary.
- Any `when(type)` on `ReportType` elsewhere (PetCard, StatusChip) → exhaustive over 3.

**Checkpoint:** `:app:compileDebugKotlin` green; StatusChip/UserAvatar previews render. **Commit:** `feat(domain): extend ReportType + PetReport, multi-image create, care token`.

---

## Phase 2B — Navigation overhaul (prerequisite of screens)

Per INSTRUCTIONS §3 + Pantalla_19_21_22_23 §7.5.

**2B.1 `Routes.kt`** — rewrite to the full set (INSTRUCTIONS §3.2): auth (`Splash`, `Otp`, `Permissions`), main container + 4 tab routes (`Main`, `Feed`=`main/feed`, `Map`, `Notifications`, `Profile`), stack routes (`Settings`, `MyReports`), wizard (`NewReport`, `FoundSubType`, `LostReportForm`, `SightingReportForm`, `InCareReportForm`), and arg routes (`ReportDetail{reportId}/{reportType}`, `ReportConfirmed{reportId}`, `SightingsForPet{petReportId}`) with `route(...)` builders.

**2B.2 `AppBottomNavigationBar.kt`** (new, `app/ui/components/`) — 4 `NavigationBarItem`s (Inicio/Mapa/Avisos/Perfil) per INSTRUCTIONS §3.5.

**2B.3 `MainScaffold.kt`** (new, `app/ui/screens/main/`) — nested `NavHost` (`bottomNavController`) with `Scaffold(bottomBar = AppBottomNavigationBar)`; tabs: Feed, Map, NotificationsPlaceholder, Profile. Passes up nav lambdas (`onNavigateToSettings/NewReport/ReportDetail/SightingsForPet/MyReports`). INSTRUCTIONS §3.4.

**2B.4 `AppNavHost.kt`** — rewrite to root `NavHost`: auth screens + `Main` container + stack/wizard/arg routes (INSTRUCTIONS §3.3). Wizard back-stack: `ReportConfirmed` uses `popUpTo(Main)`. Keep the existing Google-Sign-In launcher logic from Splash.

**2B.5 DrawerShell demotion** — Feed/Profile no longer wrap in `DrawerShell`; drawer items overlapping tabs removed. Settings/Permissions reachable from Profile overflow / Settings route (INSTRUCTIONS §3.6). Keep `DrawerShell` file (used by secondary screens or remove if unused).

**2B.6 `NotificationsPlaceholderScreen.kt`** (new) — `Scaffold` + centered `Text("Próximamente")`.

**2B.7 Feed/Profile signature updates** — `FeedScreen` gains `onNavigateToNewReport`, `onNavigateToReportDetail(id,type)`, `onNavigateToSettings`; `ProfileScreen` gains `onNavigateToMyReports`, `onNavigateToSettings`. Wire FAB + card taps (full impl in 2D).

**Checkpoint:** app boots Splash→(auth)→Main with working bottom nav; Feed/Profile render in tabs; Map/Avisos show placeholders. **Commit:** `feat(nav): bottom NavigationBar + MainScaffold + full route graph`.

---

## Phase 2C — Shared components (prerequisite of screens)

All in `app/ui/components/` unless noted. Build before the screens that use them (INSTRUCTIONS §4). Each gets a `@Preview`.

| File | Used by | Notes |
|---|---|---|
| `OsmMapView.kt` | Map, MiniMap, LocationPicker | `AndroidView(MapView)` + `DisposableEffect` lifecycle (INSTRUCTIONS §1.3/§1.5). `data class OsmMarker`. |
| `MapPinUtils.kt` (`app/util/`) | Map, MiniMap | `createPinDrawable(ctx, type)` tinted vector (`ic_location_pin` drawable + `pin_lost`/`pin_found` colors). |
| `MiniMapView.kt` | ReportDetail (P12/13) | read-only `OsmMapView`, single pin. |
| `LocationMapPicker.kt` | Sighting/InCare forms | `OsmMapView(onMapClick=…)` + helper text. |
| `SingleSelectChipGroup.kt` / `MultiSelectChipGroup.kt` | P18/P19/Detail | `FilterChip` (Flow)Row. |
| `ColorSwatchSelector.kt` | P18 | circular color swatches, single-select. |
| `PhotoPickerRow.kt` | P17/18/19 | `LazyRow` thumbnails + `+` tile; backed by `PickMultipleVisualMedia`. |
| `FormSectionHeader.kt` / `SectionLabel.kt` | forms / detail | titleMedium / caps label. |
| `HeroImage.kt` / `AttributesGrid.kt` | detail / confirmed | Coil image; 2-col label+value grid. |
| `SelectionWizardStep.kt` | P15/P16 | instruction + N `WizardOption` cards. |
| `UrgencySelector.kt` | P19 | 4 `UrgencyOption` radio-cards, care color when selected. |
| `StickyContactFooter.kt` | detail | `bottomBar` "CONTACTAR" (tertiary). |
| `MapBottomBar.kt` | Map | floating pill, 3 icons. |
| `MyReportItem.kt` / `SightingItem.kt` | P22 / P23 | compact `ListItem`. |

**2C.1 strings.xml** — add the ~60 new strings (INSTRUCTIONS §7): tabs, report types, chips, form labels, urgency, errors, discard dialog. Fix "Desapariciones" (not "Desaparaciones").

**2C.2 drawables/colors** — add `ic_location_pin` vector + `pin_lost`/`pin_found` color resources for tinted markers.

**Checkpoint:** all component previews render (including `OsmMapView` showing Cúcuta tiles). **Commit:** `feat(ui): shared Fase 2 components + map wrappers + strings`.

---

## Phase 2D — Screens, in dependency order (INSTRUCTIONS §5/§9)

Each screen = stateless `Screen(state, onEvent)` + `@HiltViewModel` + `UiState` + `UiEvent` + `@Preview`. ViewModels depend **only on puertos in** (existing: `ObserveReports`, `ObserveCurrentUser`, `CreateReport`, `DeleteReport`, `SearchReports`). No new ports needed for MVP (use Flow-filter-by-id, per docs §7.2). Commit after each green sub-phase.

**2D-1 `ReportDetailScreen`** (P12/13/14) — `app/ui/screens/report/detail/`. Doc: `Pantalla_12_13_14`. Load report by id via `combine(observeReports(LOST), (FOUND_SIGHTING), (FOUND_IN_CARE)).find{id}` from `SavedStateHandle`. Conditional content per `type` (gallery+map for LOST, map+reporter for SIGHTING, physical-status+notes, no map for IN_CARE). `isOwner` from `ObserveCurrentUser`; `StickyContactFooter` when `!isOwner`; owner delete via `DeleteReport`. **Unblocks the empty `ReportClicked` stub:** `FeedScreen`/`PetCard` "Más Información" → `onNavigateToReportDetail(id, type)`. CONTACTAR → `Intent.ACTION_DIAL`/WhatsApp. **Commit.**

**2D-2 Creation Wizard** (P15/16/17/18/19) — `app/ui/screens/report/creation/`. Doc: `Pantalla_15_16_17_18` + `…_19_…`. 
- **First:** refactor Feed FAB → `onNavigateToNewReport()`; **delete `CreateReportDialog`** + its `FeedUiEvent`/state fields + `PRESET_IMAGES` usage (INSTRUCTIONS §5 2D-2, rule 20).
- `NewReportTypeScreen` (P15) + `FoundSubTypeScreen` (P16) — no ViewModel, `SelectionWizardStep`.
- `LostReportFormScreen` (P17), `SightingReportFormScreen` (P18), `InCareReportFormScreen` (P19) — each with ViewModel; real photo upload via `PickMultipleVisualMedia` → `uri→ByteArray` → `createReport(report, list)`. `CareUrgency` enum lives in **app layer** (`creation/CareUrgency.kt`), not domain (rule 24). MD3 `DatePicker`/`TimePicker`. LocationMapPicker writes `lat/lng`. Validation + discard `AlertDialog` (`BackHandler`).
- On success → navigate `ReportConfirmed(reportId)`. **Commit.**

**2D-3 `ReportConfirmedScreen`** (P21) — `app/ui/screens/report/confirmed/`. Doc: `…_19_21_22_23`. Loads report by id; ✅ + hero + description + "Ir al feed". `BackHandler`→feed; entered with `popUpTo(Main)` so back never returns to the form (rule 21). **Commit.**

**2D-4 `MapScreen`** (P6/7/8) — `app/ui/screens/map/` (+ `MapFiltersSheet`, `PinPreviewSheet`). Doc: `Pantalla_6_7_8`. `OsmMapView` full-screen; markers from reports with non-null coords; `combine` 3 types. `MapBottomSheetState` sealed (None/Filters/PinPreview). `MapBottomBar` (add/filter/search) as `Box` overlay (not `Scaffold.bottomBar`). Filters local (lost/found, no GPS). Pin tap → PinPreview → ReportDetail. Tab in `MainScaffold`. **Commit.**

**2D-5 `MyReportsScreen`** (P22) — `app/ui/screens/myreports/`. Doc: `…_19_21_22_23`. `combine` 3 types + `ObserveCurrentUser`, filter `ownerUid == uid`, group Hallazgos / Desapariciones. Item tap → ReportDetail; `⋮` → delete (`DeleteReport` + confirm). Entry: `ProfileScreen` "Ver mis reportes". **Commit.**

**2D-6 `SightingsForPetScreen`** (P23) — `app/ui/screens/sightings/`. Doc: `…_19_21_22_23`. From `petReportId`: load LOST pet, list `FOUND_SIGHTING` by **species + zone heuristic** (no model relation). OSMDroid header map (220dp) with sighting pins + back overlay; `SightingItem` list with `UserAvatar(containerColor = colorFromUid)`. "Ver mapa completo" → Map. Entry: from MyReports (tap a LOST) / ReportDetail (owner). Avisos tab stays placeholder. **Commit.**

---

## Critical files (most-touched)
- Domain: `domain/model/{ReportType,PetReport}.kt`, `domain/port/in/CreateReport.kt`, `domain/port/out/PetReportRepository.kt`, `domain/usecase/CreateReportImpl.kt`.
- Data: `data/dto/PetReportDto.kt`, `data/mapper/PetReportMapper.kt`, `data/firebase/FirestorePetReportRepository.kt`, `data/fake/FakePetReportRepository.kt`.
- Nav: `app/navigation/{Routes,AppNavHost}.kt`, new `app/ui/screens/main/MainScaffold.kt`, `app/ui/components/AppBottomNavigationBar.kt`.
- Theme/components: `app/theme/{Color,Theme}.kt`, `app/ui/components/{StatusChip,UserAvatar,PetCard}.kt`.
- Feed touch points: `app/ui/screens/feed/{FeedScreen,FeedViewModel,FeedUiState,FeedUiEvent}.kt`.
- Build: `gradle/libs.versions.toml`, `app/build.gradle.kts`, `app/MascotasPerdidasApp.kt`, `AndroidManifest.xml`, `res/values/strings.xml`.

## Architecture guardrails (do not break)
- `domain/` imports nothing from `data/`/`app/`/`androidx`/`firebase` (only kotlin stdlib + coroutines-core + javax.inject). `CareUrgency`/`SightingWithUser` are **app-layer**, not domain.
- ViewModels → puertos in only; use cases → puertos out only. Swap Fake↔Firebase = `RepositoryModule` only.
- No hardcoded colors (`MaterialTheme.colorScheme.*` / care `CompositionLocal`) or strings (`stringResource`). Material 3 only. OSMDroid only for maps.

## Risks / watch-outs
- **Legacy Firestore docs** with `type="FOUND"` → mapper must fall back to `FOUND_SIGHTING`, else they vanish from queries.
- **Composite indexes:** keep `ownerUid`/multi-type filtering **client-side** (no `whereEqualTo+orderBy`) to avoid Firestore index errors — current code already sorts client-side; preserve that.
- **Feed Avistadas tab** is binary but enum is ternary → merge `FOUND_SIGHTING + FOUND_IN_CARE`.
- **OSMDroid lifecycle:** must call `onResume/onPause` via `DisposableEffect` or map leaks/blanks.
- **Multi-photo upload** failure mid-way leaves orphan Storage files — acceptable for MVP; note as TODO, no rollback.

## Verification (per phase)
1. `./gradlew :app:compileDebugKotlin` green (or `assembleDebug`).
2. `./gradlew detekt` clean (autoCorrect on).
3. New `@Preview`(s) render for every screen/component added in the phase.
4. Smoke path after 2D: Splash → Main → FAB → wizard → publish → Confirmed → Feed shows it → tap card → Detail → Map shows pin → Profile → Mis reportes → delete.
5. **On green:** `git add -A && git commit` with the phase message, then `git push` (authorized).
