# Home Overview Implementation Guide

## objective
Provide a lightweight “Home” hub that greets the authenticated user and surfaces their next tasks, upcoming meetings, and recently touched projects while preserving Eureka’s Compose+MVVM conventions, color system, and repository layer.

## milestoneHomeRoute
**Goal:** expose a dedicated `Route.HomeOverview` that becomes the landing surface after splash/auth.

**Steps**
1. Extend `Route` in `navigation/Navigation.kt` with `@Serializable data object HomeOverview : Route`.
2. Move `startDestination` inside `NavigationMenu` from `Route.ProjectSelection` to `Route.HomeOverview`.
3. Add a new `composable<Route.HomeOverview>` that renders the forthcoming `HomeOverviewScreen`.
4. Ensure legacy flows (e.g., project selection CTA) are reachable from the home screen instead of as the start destination.
5. Update `BottomBarNavigationComponent` so the central button navigates to `Route.HomeOverview` and highlight state keys off that route class instead of `Route.ProjectSelection`.

**Deliverable:** Commit `feat: add home overview route entry`.

## milestoneHomeViewModel
**Goal:** aggregate the three data slices behind a single `HomeOverviewViewModel` that respects MVVM boundaries.

**Steps**
1. Create package `ui/home` and add `data class HomeOverviewUiState` containing `currentUserName`, `upcomingTasks`, `upcomingMeetings`, `recentProjects`, `isLoading`, `isConnected`, `error`.
2. Inject repositories via `FirestoreRepositoriesProvider`:
   - `taskRepository.getTasksForCurrentUser()` limited to next N incomplete tasks sorted by due date.
   - `projectRepository.getProjectsForCurrentUser(skipCache = false)`.
   - `meetingRepository.getMeetingsForCurrentUser(projectId, skipCache = false)` for each active project.
   - `userRepository.getCurrentUser()` for greeting.
3. Add optional `lastUpdated: Timestamp? = null` to `Project` + Firestore mapping so we can order projects; backfill default `FieldValue.serverTimestamp()` inside `FirestoreProjectRepository` create/update methods.
4. Use `combine` to merge flows, map into `HomeOverviewUiState`, and expose as `StateFlow`.
5. Observe `ConnectivityObserverProvider.connectivityObserver.isConnected` to toggle offline states.

**Deliverable:** Commit `feat: add home overview viewmodel and state`.

## milestoneHomeUiLayout
**Goal:** compose the screen with existing components to avoid redundant UI code.

**Steps**
1. Create `HomeOverviewScreen` + preview inside `screens/HomeOverviewScreen.kt`.
2. Greet the user using `EurekaTopBar` or simple `Column` with `Text("Hello, $name")`.
3. Use `LazyColumn` to stack sections.
4. Reuse `TaskSectionHeader` for each block title (Upcoming Tasks, Next Meetings, Recent Projects) to keep visual parity.
5. Inject the viewmodel via `viewModel<HomeOverviewViewModel>()` with default factory.

**Deliverable:** Commit `feat: scaffold home overview ui`.

## milestoneHomeDataSections
**Goal:** wire each data block to existing card components.

**Steps**
1. **Upcoming tasks:** map `HomeOverviewTaskItem` into `EurekaTaskCard` (import from `ui/components`). Limit to 3 cards and provide CTA button linking to `Route.TasksSection.Tasks`.
2. **Upcoming meetings:** reuse `MeetingCardConfig` to render a condensed `MeetingCard` list (hide heavy actions, only keep `onClick`→detail and `onDirections`). Provide fallback text when none.
3. **Recent projects:** reuse `ProjectCard` body pieces by extracting a smaller `ProjectSummaryCard` in `screens/ProjectSelectionScreen.kt` (accepts `Project`, `membersCount`, `status`) and call it from both screens to avoid duplication.
4. For each section, display `EurekaInfoCard` style summary chip showing counts (e.g., “2 due today”).
5. Add loading and offline placeholders using colors from `EColors` and spacing tokens.

**Deliverable:** Commit `feat: populate home overview sections`.

## milestoneHomeInteractions
**Goal:** ensure navigation + actions stay consistent.

**Steps**
1. On task card tap, reuse existing lambdas to navigate to `Route.TasksSection.ViewTask`.
2. On meeting card tap, navigate to `Route.MeetingsSection.MeetingDetail`.
3. On project card tap, navigate to `Route.OverviewProject`.
4. Add clear CTA buttons (“View all tasks”, “Open meetings”, “Browse projects”) that point to their respective tabs for discoverability.

**Deliverable:** Commit `feat: add home overview interactions`.

## milestoneHomeTesting
**Goal:** cover the new feature with the same rigor as other screens.

**Steps**
1. Add unit tests for `HomeOverviewViewModel` under `app/src/test/.../ui/home/` using fake repositories (see `ui/tasks/MockTaskRepository.kt`) to validate filtering logic, greeting, and offline state transitions.
2. Add Compose UI tests in `app/src/androidTest/.../HomeOverviewScreenTest.kt` verifying:
   - Greeting renders user’s first name.
   - Each section shows max 3 cards and CTA buttons navigate via `TestNavHostController`.
   - Empty/Offline states display the correct copy.
3. Update regression suites:
   - `NavigationMenuTest` to assert `Route.HomeOverview` is start destination.
   - `BottomBarNavigationComponentTest` to ensure center button test tag triggers home route.

**Deliverable:** Commit `test: cover home overview screen`.



## milestoneHomePolish

1. Run `./gradlew ktfmtFormat` + `./gradlew lintDebug`.

**Deliverable:** Commit `chore: polish home overview`.


