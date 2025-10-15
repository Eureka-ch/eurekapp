package ch.eureka.eurekapp.model.data

import org.junit.Assert.assertEquals
import org.junit.Test

class StoragePathsTest {

  @Test
  fun userFilePath_generatesCorrectPath() {
    val userId = "user123"
    val filename = "profile.jpg"

    val result = StoragePaths.userFilePath(userId, filename)

    assertEquals("users/user123/profile.jpg", result)
  }

  @Test
  fun userFilePath_handlesSpecialCharactersInFilename() {
    val userId = "user123"
    val filename = "my document (v2).pdf"

    val result = StoragePaths.userFilePath(userId, filename)

    assertEquals("users/user123/my document (v2).pdf", result)
  }

  @Test
  fun projectFilePath_generatesCorrectPath() {
    val projectId = "project456"
    val filename = "requirements.pdf"

    val result = StoragePaths.projectFilePath(projectId, filename)

    assertEquals("projects/project456/requirements.pdf", result)
  }

  @Test
  fun projectFilePath_handlesLongFilenames() {
    val projectId = "project456"
    val filename = "very_long_filename_with_many_characters_to_test_path_generation.docx"

    val result = StoragePaths.projectFilePath(projectId, filename)

    assertEquals(
        "projects/project456/very_long_filename_with_many_characters_to_test_path_generation.docx",
        result)
  }

  @Test
  fun taskAttachmentPath_generatesCorrectPath() {
    val projectId = "project789"
    val taskId = "task123"
    val filename = "screenshot.png"

    val result = StoragePaths.taskAttachmentPath(projectId, taskId, filename)

    assertEquals("projects/project789/tasks/task123/screenshot.png", result)
  }

  @Test
  fun taskAttachmentPath_handlesMultipleFileExtensions() {
    val projectId = "project789"
    val taskId = "task123"
    val filename = "archive.tar.gz"

    val result = StoragePaths.taskAttachmentPath(projectId, taskId, filename)

    assertEquals("projects/project789/tasks/task123/archive.tar.gz", result)
  }

  @Test
  fun meetingAttachmentPath_generatesCorrectPath() {
    val projectId = "projectABC"
    val meetingId = "meetingXYZ"
    val filename = "notes.txt"

    val result = StoragePaths.meetingAttachmentPath(projectId, meetingId, filename)

    assertEquals("projects/projectABC/meetings/meetingXYZ/notes.txt", result)
  }

  @Test
  fun meetingAttachmentPath_handlesFileWithoutExtension() {
    val projectId = "projectABC"
    val meetingId = "meetingXYZ"
    val filename = "README"

    val result = StoragePaths.meetingAttachmentPath(projectId, meetingId, filename)

    assertEquals("projects/projectABC/meetings/meetingXYZ/README", result)
  }

  @Test
  fun allPaths_handleEmptyStrings() {
    // Test that empty strings don't break path generation
    val emptyUser = StoragePaths.userFilePath("", "file.txt")
    val emptyProject = StoragePaths.projectFilePath("", "file.txt")
    val emptyTask = StoragePaths.taskAttachmentPath("", "", "file.txt")
    val emptyMeeting = StoragePaths.meetingAttachmentPath("", "", "file.txt")

    assertEquals("users//file.txt", emptyUser)
    assertEquals("projects//file.txt", emptyProject)
    assertEquals("projects//tasks//file.txt", emptyTask)
    assertEquals("projects//meetings//file.txt", emptyMeeting)
  }

  @Test
  fun allPaths_maintainConsistentStructure() {
    // Verify that nested paths maintain the projects/{projectId}/* structure
    val taskPath = StoragePaths.taskAttachmentPath("proj1", "task1", "file.txt")
    val meetingPath = StoragePaths.meetingAttachmentPath("proj1", "meet1", "file.txt")

    assert(taskPath.startsWith("projects/proj1/"))
    assert(meetingPath.startsWith("projects/proj1/"))
  }
}
