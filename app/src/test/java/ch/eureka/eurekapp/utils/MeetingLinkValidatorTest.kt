package ch.eureka.eurekapp.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

// This code was written with help of Claude.

class MeetingLinkValidatorTest {

  // ========== validateMeetingLink() Tests ==========

  @Test
  fun validateMeetingLink_returnsInvalidWithNullUrl() {
    val (isValid, error) = MeetingLinkValidator.validateMeetingLink(null)

    assertFalse(isValid)
    assertEquals("Meeting link is required", error)
  }

  @Test
  fun validateMeetingLink_returnsInvalidWithEmptyUrl() {
    val (isValid, error) = MeetingLinkValidator.validateMeetingLink("")

    assertFalse(isValid)
    assertEquals("Meeting link is required", error)
  }

  @Test
  fun validateMeetingLink_returnsInvalidWithBlankUrl() {
    val (isValid, error) = MeetingLinkValidator.validateMeetingLink("   ")

    assertFalse(isValid)
    assertEquals("Meeting link is required", error)
  }

  @Test
  fun validateMeetingLink_returnsInvalidWithInvalidUrlFormat() {
    val (isValid, error) = MeetingLinkValidator.validateMeetingLink("not-a-url")

    assertFalse(isValid)
    assertEquals("Invalid URL format", error)
  }

  @Test
  fun validateMeetingLink_returnsInvalidWithUrlWithoutProtocol() {
    val (isValid, error) = MeetingLinkValidator.validateMeetingLink("zoom.us/j/123456")

    assertFalse(isValid)
    assertEquals("Invalid URL format", error)
  }

  @Test
  fun validateMeetingLink_returnsValidWithValidZoomUrl() {
    val (isValid, error) = MeetingLinkValidator.validateMeetingLink("https://zoom.us/j/1234567890")

    assertTrue(isValid)
    assertNull(error)
  }

  @Test
  fun validateMeetingLink_returnsValidWithValidZoomUrlWithPassword() {
    val (isValid, error) =
        MeetingLinkValidator.validateMeetingLink("https://zoom.us/j/1234567890?pwd=abcd1234")

    assertTrue(isValid)
    assertNull(error)
  }

  @Test
  fun validateMeetingLink_returnsValidWithValidGoogleMeetUrl() {
    val (isValid, error) =
        MeetingLinkValidator.validateMeetingLink("https://meet.google.com/abc-defg-hij")

    assertTrue(isValid)
    assertNull(error)
  }

  @Test
  fun validateMeetingLink_returnsValidWithValidMicrosoftTeamsUrl() {
    val (isValid, error) =
        MeetingLinkValidator.validateMeetingLink(
            "https://teams.microsoft.com/l/meetup-join/19%3ameeting_123")

    assertTrue(isValid)
    assertNull(error)
  }

  @Test
  fun validateMeetingLink_returnsValidWithValidWebexUrl() {
    val (isValid, error) =
        MeetingLinkValidator.validateMeetingLink("https://webex.com/meet/username")

    assertTrue(isValid)
    assertNull(error)
  }

  @Test
  fun validateMeetingLink_returnsValidWithWarningWithNonWhitelistedDomain() {
    val (isValid, error) = MeetingLinkValidator.validateMeetingLink("https://jitsi.org/meeting123")

    assertTrue(isValid)
    assertEquals("Warning: This link is not from a trusted platform", error)
  }

  @Test
  fun validateMeetingLink_acceptsHttpWithHttpUrl() {
    val (isValid, error) = MeetingLinkValidator.validateMeetingLink("http://zoom.us/j/1234567890")

    assertTrue(isValid)
    assertNull(error)
  }

  // ========== detectPlatform() Tests ==========

  @Test
  fun detectPlatform_returnsUnknownWithNullUrl() {
    val platform = MeetingLinkValidator.detectPlatform(null)

    assertEquals(MeetingPlatform.UNKNOWN, platform)
  }

  @Test
  fun detectPlatform_returnsUnknownWithEmptyUrl() {
    val platform = MeetingLinkValidator.detectPlatform("")

    assertEquals(MeetingPlatform.UNKNOWN, platform)
  }

  @Test
  fun detectPlatform_returnsUnknownWithInvalidUrl() {
    val platform = MeetingLinkValidator.detectPlatform("not-a-url")

    assertEquals(MeetingPlatform.UNKNOWN, platform)
  }

  @Test
  fun detectPlatform_returnsZoomWithZoomUsUrl() {
    val platform = MeetingLinkValidator.detectPlatform("https://zoom.us/j/1234567890")

    assertEquals(MeetingPlatform.ZOOM, platform)
    assertEquals("Zoom", platform.displayName)
  }

  @Test
  fun detectPlatform_returnsZoomWithZoomComUrl() {
    val platform = MeetingLinkValidator.detectPlatform("https://zoom.com/j/1234567890")

    assertEquals(MeetingPlatform.ZOOM, platform)
  }

  @Test
  fun detectPlatform_returnsGoogleMeetWithGoogleMeetUrl() {
    val platform = MeetingLinkValidator.detectPlatform("https://meet.google.com/abc-defg-hij")

    assertEquals(MeetingPlatform.GOOGLE_MEET, platform)
    assertEquals("Google Meet", platform.displayName)
  }

  @Test
  fun detectPlatform_returnsTeamsWithMicrosoftTeamsUrl() {
    val platform =
        MeetingLinkValidator.detectPlatform("https://teams.microsoft.com/l/meetup-join/123")

    assertEquals(MeetingPlatform.MICROSOFT_TEAMS, platform)
    assertEquals("Microsoft Teams", platform.displayName)
  }

  @Test
  fun detectPlatform_returnsTeamsWithTeamsLiveUrl() {
    val platform = MeetingLinkValidator.detectPlatform("https://teams.live.com/meet/123")

    assertEquals(MeetingPlatform.MICROSOFT_TEAMS, platform)
  }

  @Test
  fun detectPlatform_returnsWebexWithWebexUrl() {
    val platform = MeetingLinkValidator.detectPlatform("https://webex.com/meet/username")

    assertEquals(MeetingPlatform.WEBEX, platform)
    assertEquals("Webex", platform.displayName)
  }

  @Test
  fun detectPlatform_returnsUnknownWithNonWhitelistedUrl() {
    val platform = MeetingLinkValidator.detectPlatform("https://jitsi.org/meeting123")

    assertEquals(MeetingPlatform.UNKNOWN, platform)
    assertEquals("Unknown Platform", platform.displayName)
  }

  @Test
  fun detectPlatform_detectsCorrectlyWithCaseInsensitive() {
    val platform = MeetingLinkValidator.detectPlatform("https://ZOOM.US/j/1234567890")

    assertEquals(MeetingPlatform.ZOOM, platform)
  }
}
