package ch.eureka.eurekapp.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Test suite for Route companion object registries.
 *
 * These tests verify that the automatic route registries (using sealedSubclasses) work correctly.
 * The registries are now automatic - when you add a new route to a section, it's automatically
 * included via Kotlin's built-in sealedSubclasses property.
 *
 * Co-Authored-By: Claude <noreply@anthropic.com>
 */
class RouteRegistryTest {

  @Test
  fun tasksSectionRoutes_containsExpectedRoutes() {
    val registeredRoutes = Route.TasksSection.routes

    // Verify known routes are present
    assertTrue(
        "TasksSection should contain Tasks", registeredRoutes.any { it.simpleName == "Tasks" })
    assertTrue(
        "TasksSection should contain CreateTask",
        registeredRoutes.any { it.simpleName == "CreateTask" })
    assertTrue(
        "TasksSection should contain ViewTask",
        registeredRoutes.any { it.simpleName == "ViewTask" })
    assertTrue(
        "TasksSection should contain EditTask",
        registeredRoutes.any { it.simpleName == "EditTask" })
    assertTrue(
        "TasksSection should contain AutoTaskAssignment",
        registeredRoutes.any { it.simpleName == "AutoTaskAssignment" })
    assertTrue(
        "TasksSection should contain TaskDependence",
        registeredRoutes.any { it.simpleName == "TaskDependence" })

    // Verify we have the expected number of routes (6 as of now)
    assertEquals("TasksSection should have 6 routes", 6, registeredRoutes.size)
  }

  @Test
  fun ideasSectionRoutes_containsExpectedRoutes() {
    val registeredRoutes = Route.IdeasSection.routes

    assertTrue(
        "IdeasSection should contain Ideas", registeredRoutes.any { it.simpleName == "Ideas" })
    assertTrue(
        "IdeasSection should contain CreateIdeas",
        registeredRoutes.any { it.simpleName == "CreateIdeas" })

    assertEquals("IdeasSection should have 2 routes", 2, registeredRoutes.size)
  }

  @Test
  fun meetingsSectionRoutes_containsExpectedRoutes() {
    val registeredRoutes = Route.MeetingsSection.routes

    assertTrue(
        "MeetingsSection should contain Meetings",
        registeredRoutes.any { it.simpleName == "Meetings" })
    assertTrue(
        "MeetingsSection should contain MeetingsOverview",
        registeredRoutes.any { it.simpleName == "MeetingsOverview" })
    assertTrue(
        "MeetingsSection should contain CreateMeeting",
        registeredRoutes.any { it.simpleName == "CreateMeeting" })
    assertTrue(
        "MeetingsSection should contain MeetingDetail",
        registeredRoutes.any { it.simpleName == "MeetingDetail" })
    assertTrue(
        "MeetingsSection should contain AudioRecording",
        registeredRoutes.any { it.simpleName == "AudioRecording" })
    assertTrue(
        "MeetingsSection should contain AudioTranscript",
        registeredRoutes.any { it.simpleName == "AudioTranscript" })
    assertTrue(
        "MeetingsSection should contain DateTimeVotes",
        registeredRoutes.any { it.simpleName == "DateTimeVotes" })

    assertEquals("MeetingsSection should have 7 routes", 7, registeredRoutes.size)
  }

  @Test
  fun projectSelectionSectionRoutes_containsExpectedRoutes() {
    val registeredRoutes = Route.ProjectSelectionSection.routes

    assertTrue(
        "ProjectSelectionSection should contain CreateProject",
        registeredRoutes.any { it.simpleName == "CreateProject" })

    assertEquals("ProjectSelectionSection should have 1 route", 1, registeredRoutes.size)
  }

  @Test
  fun overviewProjectSectionRoutes_containsExpectedRoutes() {
    val registeredRoutes = Route.OverviewProjectSection.routes

    assertTrue(
        "OverviewProjectSection should contain CreateInvitation",
        registeredRoutes.any { it.simpleName == "CreateInvitation" })

    assertEquals("OverviewProjectSection should have 1 route", 1, registeredRoutes.size)
  }

  @Test
  fun tasksSectionRegistry_isNotEmpty() {
    assertTrue(
        "TasksSection should have at least one route registered",
        Route.TasksSection.routes.isNotEmpty())
  }

  @Test
  fun ideasSectionRegistry_isNotEmpty() {
    assertTrue(
        "IdeasSection should have at least one route registered",
        Route.IdeasSection.routes.isNotEmpty())
  }

  @Test
  fun meetingsSectionRegistry_isNotEmpty() {
    assertTrue(
        "MeetingsSection should have at least one route registered",
        Route.MeetingsSection.routes.isNotEmpty())
  }

  @Test
  fun routeRegistries_doNotContainDuplicates() {
    val allSections =
        mapOf(
            "TasksSection" to Route.TasksSection.routes,
            "IdeasSection" to Route.IdeasSection.routes,
            "MeetingsSection" to Route.MeetingsSection.routes,
            "ProjectSelectionSection" to Route.ProjectSelectionSection.routes,
            "OverviewProjectSection" to Route.OverviewProjectSection.routes)

    allSections.forEach { (sectionName, routes) ->
      assertEquals(
          "$sectionName should not contain duplicate routes", routes.size, routes.toSet().size)
    }
  }
}
