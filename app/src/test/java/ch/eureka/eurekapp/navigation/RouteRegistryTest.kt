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
 * Portions of this code were generated with the help of Claude <noreply@anthropic.com> and ChatGPT
 * (GPT-5). Co-Authored-By: Claude Sonnet 4.5
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
    assertTrue(
        "TasksSection should contain FilesManagement",
        registeredRoutes.any { it.simpleName == "FilesManagement" })
    assertTrue(
        "TasksSection should contain CreateTemplate",
        registeredRoutes.any { it.simpleName == "CreateTemplate" })

    // Verify we have the expected number of routes (8 as of now)
    assertEquals("TasksSection should have 8 routes", 8, registeredRoutes.size)
  }

  @Test
  fun meetingsSectionRoutes_containsExpectedRoutes() {
    val registeredRoutes = Route.MeetingsSection.routes

    assertTrue(
        "MeetingsSection should contain Meetings",
        registeredRoutes.any { it.simpleName == "Meetings" })
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
        "MeetingsSection should contain MeetingProposalVotes",
        registeredRoutes.any { it.simpleName == "MeetingProposalVotes" })

    assertTrue(
        "MeetingsSection should contain CreateDateTimeFormatMeetingProposalForMeeting",
        registeredRoutes.any { it.simpleName == "CreateDateTimeFormatMeetingProposalForMeeting" })

    assertTrue(
        "MeetingsSection should contain MeetingNavigation",
        registeredRoutes.any { it.simpleName == "MeetingNavigation" })

    assertTrue(
        "MeetingsSection should contain MeetingLocationSelection",
        registeredRoutes.any { it.simpleName == "MeetingLocationSelection" })

    assertEquals("MeetingsSection should have 9 routes", 9, registeredRoutes.size)
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

    assertTrue(
        "OverviewProjectSection should contain TokenEntry",
        registeredRoutes.any { it.simpleName == "TokenEntry" })

    assertTrue(
        "OverviewProjectSection should contain ProjectMembers",
        registeredRoutes.any { it.simpleName == "ProjectMembers" })

    assertEquals("OverviewProjectSection should have 3 route", 3, registeredRoutes.size)
  }

  @Test
  fun tasksSectionRegistry_isNotEmpty() {
    assertTrue(
        "TasksSection should have at least one route registered",
        Route.TasksSection.routes.isNotEmpty())
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
            "MeetingsSection" to Route.MeetingsSection.routes,
            "ProjectSelectionSection" to Route.ProjectSelectionSection.routes,
            "OverviewProjectSection" to Route.OverviewProjectSection.routes)

    allSections.forEach { (sectionName, routes) ->
      assertEquals(
          "$sectionName should not contain duplicate routes", routes.size, routes.toSet().size)
    }
  }

  @Test
  fun audioTranscriptRoute_createsWithProjectIdAndMeetingId() {
    val route =
        Route.MeetingsSection.AudioTranscript(
            projectId = "test-project", meetingId = "test-meeting")
    assertEquals("test-project", route.projectId)
    assertEquals("test-meeting", route.meetingId)
  }

  @Test
  fun audioRecordingRoute_createsWithProjectIdAndMeetingId() {
    val route =
        Route.MeetingsSection.AudioRecording(projectId = "test-project", meetingId = "test-meeting")
    assertEquals("test-project", route.projectId)
    assertEquals("test-meeting", route.meetingId)
  }

  @Test
  fun meetingsSectionRoutes_includesBothAudioRecordingAndTranscript() {
    val registeredRoutes = Route.MeetingsSection.routes

    val hasAudioRecording = registeredRoutes.any { it.simpleName == "AudioRecording" }
    val hasAudioTranscript = registeredRoutes.any { it.simpleName == "AudioTranscript" }

    assertTrue(
        "MeetingsSection should contain both AudioRecording and AudioTranscript routes",
        hasAudioRecording && hasAudioTranscript)
  }

  // ===== ROUTE INSTANTIATION TESTS (Lines 136-158) =====

  @Test
  fun meetingProposalVotesRoute_createsWithCorrectParameters() {
    val projectId = "test-project-123"
    val meetingId = "test-meeting-456"
    val route = Route.MeetingsSection.MeetingProposalVotes(projectId, meetingId)

    assertEquals("Project ID should match", projectId, route.projectId)
    assertEquals("Meeting ID should match", meetingId, route.meetingId)
  }

  @Test
  fun createDateTimeFormatMeetingProposalRoute_createsWithCorrectParameters() {
    val projectId = "proj-789"
    val meetingId = "meet-012"
    val route =
        Route.MeetingsSection.CreateDateTimeFormatMeetingProposalForMeeting(projectId, meetingId)

    assertEquals("Project ID should match", projectId, route.projectId)
    assertEquals("Meeting ID should match", meetingId, route.meetingId)
  }

  @Test
  fun meetingDetailRoute_createsWithCorrectParameters() {
    val projectId = "project-abc"
    val meetingId = "meeting-xyz"
    val route = Route.MeetingsSection.MeetingDetail(projectId, meetingId)

    assertEquals("Project ID should match", projectId, route.projectId)
    assertEquals("Meeting ID should match", meetingId, route.meetingId)
  }

  @Test
  fun meetingNavigationRoute_createsWithCorrectParameters() {
    val projectId = "nav-project-001"
    val meetingId = "nav-meeting-002"
    val route = Route.MeetingsSection.MeetingNavigation(projectId, meetingId)

    assertEquals("Project ID should match", projectId, route.projectId)
    assertEquals("Meeting ID should match", meetingId, route.meetingId)
  }

  @Test
  fun allMeetingRoutes_canBeInstantiatedWithTestData() {
    val testProjectId = "test-proj"
    val testMeetingId = "test-meet"

    // Test all route types can be created
    val proposalVotes = Route.MeetingsSection.MeetingProposalVotes(testProjectId, testMeetingId)
    val dateTimeProposal =
        Route.MeetingsSection.CreateDateTimeFormatMeetingProposalForMeeting(
            testProjectId, testMeetingId)
    val meetingDetail = Route.MeetingsSection.MeetingDetail(testProjectId, testMeetingId)
    val audioRecording = Route.MeetingsSection.AudioRecording(testProjectId, testMeetingId)
    val audioTranscript = Route.MeetingsSection.AudioTranscript(testProjectId, testMeetingId)
    val meetingNav = Route.MeetingsSection.MeetingNavigation(testProjectId, testMeetingId)

    // Verify all routes have correct IDs
    assertEquals(testProjectId, proposalVotes.projectId)
    assertEquals(testProjectId, dateTimeProposal.projectId)
    assertEquals(testProjectId, meetingDetail.projectId)
    assertEquals(testProjectId, audioRecording.projectId)
    assertEquals(testProjectId, audioTranscript.projectId)
    assertEquals(testProjectId, meetingNav.projectId)

    assertEquals(testMeetingId, proposalVotes.meetingId)
    assertEquals(testMeetingId, dateTimeProposal.meetingId)
    assertEquals(testMeetingId, meetingDetail.meetingId)
    assertEquals(testMeetingId, audioRecording.meetingId)
    assertEquals(testMeetingId, audioTranscript.meetingId)
    assertEquals(testMeetingId, meetingNav.meetingId)
  }

  @Test
  fun meetingRoutes_handleSpecialCharactersInIds() {
    val projectIdWithSpecialChars = "proj-123_test@domain"
    val meetingIdWithSpecialChars = "meet-456_special#id"

    val route =
        Route.MeetingsSection.MeetingDetail(projectIdWithSpecialChars, meetingIdWithSpecialChars)

    assertEquals(projectIdWithSpecialChars, route.projectId)
    assertEquals(meetingIdWithSpecialChars, route.meetingId)
  }

  @Test
  fun meetingRoutes_handleEmptyStringsInIds() {
    val emptyProjectId = ""
    val emptyMeetingId = ""

    val route = Route.MeetingsSection.AudioTranscript(emptyProjectId, emptyMeetingId)

    assertEquals(emptyProjectId, route.projectId)
    assertEquals(emptyMeetingId, route.meetingId)
  }

  @Test
  fun meetingRoutes_handleLongIdsCorrectly() {
    val longProjectId = "a".repeat(500)
    val longMeetingId = "b".repeat(500)

    val route = Route.MeetingsSection.MeetingNavigation(longProjectId, longMeetingId)

    assertEquals(longProjectId, route.projectId)
    assertEquals(longMeetingId, route.meetingId)
  }
}
