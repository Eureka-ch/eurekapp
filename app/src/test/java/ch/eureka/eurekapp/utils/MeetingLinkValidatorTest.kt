package ch.eureka.eurekapp.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MeetingLinkValidatorTest {

  // ========== validateMeetingLink() Tests ==========

  @Test
  fun validateMeetingLink_nullUrl_returnsInvalid() {
    val (isValid, error) = MeetingLinkValidator.validateMeetingLink(null)

    assertFalse(isValid)
    assertEquals("Meeting link is required", error)
  }

  @Test
  fun validateMeetingLink_emptyUrl_returnsInvalid() {
    val (isValid, error) = MeetingLinkValidator.validateMeetingLink("")

    assertFalse(isValid)
    assertEquals("Meeting link is required", error)
  }

  @Test
  fun validateMeetingLink_blankUrl_returnsInvalid() {
    val (isValid, error) = MeetingLinkValidator.validateMeetingLink("   ")

    assertFalse(isValid)
    assertEquals("Meeting link is required", error)
  }

  @Test
  fun validateMeetingLink_invalidUrlFormat_returnsInvalid() {
    val (isValid, error) = MeetingLinkValidator.validateMeetingLink("not-a-url")

    assertFalse(isValid)
    assertEquals("Invalid URL format", error)
  }

  @Test
  fun validateMeetingLink_urlWithoutProtocol_returnsInvalid() {
    val (isValid, error) = MeetingLinkValidator.validateMeetingLink("zoom.us/j/123456")

    assertFalse(isValid)
    assertEquals("Invalid URL format", error)
  }

  @Test
  fun validateMeetingLink_validZoomUrl_returnsValid() {
    val (isValid, error) = MeetingLinkValidator.validateMeetingLink("https://zoom.us/j/1234567890")

    assertTrue(isValid)
    assertNull(error)
  }

  @Test
  fun validateMeetingLink_validZoomUrlWithPassword_returnsValid() {
    val (isValid, error) =
        MeetingLinkValidator.validateMeetingLink("https://zoom.us/j/1234567890?pwd=abcd1234")

    assertTrue(isValid)
    assertNull(error)
  }

  @Test
  fun validateMeetingLink_validGoogleMeetUrl_returnsValid() {
    val (isValid, error) =
        MeetingLinkValidator.validateMeetingLink("https://meet.google.com/abc-defg-hij")

    assertTrue(isValid)
    assertNull(error)
  }

  @Test
  fun validateMeetingLink_validMicrosoftTeamsUrl_returnsValid() {
    val (isValid, error) =
        MeetingLinkValidator.validateMeetingLink(
            "https://teams.microsoft.com/l/meetup-join/19%3ameeting_123")

    assertTrue(isValid)
    assertNull(error)
  }

  @Test
  fun validateMeetingLink_validWebexUrl_returnsValid() {
    val (isValid, error) =
        MeetingLinkValidator.validateMeetingLink("https://webex.com/meet/username")

    assertTrue(isValid)
    assertNull(error)
  }

  @Test
  fun validateMeetingLink_nonWhitelistedDomain_returnsValidWithWarning() {
    val (isValid, error) = MeetingLinkValidator.validateMeetingLink("https://jitsi.org/meeting123")

    assertTrue(isValid)
    assertEquals("Warning: This link is not from a trusted platform", error)
  }

  @Test
  fun validateMeetingLink_httpUrl_acceptsHttp() {
    val (isValid, error) = MeetingLinkValidator.validateMeetingLink("http://zoom.us/j/1234567890")

    assertTrue(isValid)
    assertNull(error)
  }

  // ========== detectPlatform() Tests ==========

  @Test
  fun detectPlatform_nullUrl_returnsUnknown() {
    val platform = MeetingLinkValidator.detectPlatform(null)

    assertEquals(MeetingPlatform.UNKNOWN, platform)
  }

  @Test
  fun detectPlatform_emptyUrl_returnsUnknown() {
    val platform = MeetingLinkValidator.detectPlatform("")

    assertEquals(MeetingPlatform.UNKNOWN, platform)
  }

  @Test
  fun detectPlatform_invalidUrl_returnsUnknown() {
    val platform = MeetingLinkValidator.detectPlatform("not-a-url")

    assertEquals(MeetingPlatform.UNKNOWN, platform)
  }

  @Test
  fun detectPlatform_zoomUsUrl_returnsZoom() {
    val platform = MeetingLinkValidator.detectPlatform("https://zoom.us/j/1234567890")

    assertEquals(MeetingPlatform.ZOOM, platform)
    assertEquals("Zoom", platform.displayName)
  }

  @Test
  fun detectPlatform_zoomComUrl_returnsZoom() {
    val platform = MeetingLinkValidator.detectPlatform("https://zoom.com/j/1234567890")

    assertEquals(MeetingPlatform.ZOOM, platform)
  }

  @Test
  fun detectPlatform_googleMeetUrl_returnsGoogleMeet() {
    val platform = MeetingLinkValidator.detectPlatform("https://meet.google.com/abc-defg-hij")

    assertEquals(MeetingPlatform.GOOGLE_MEET, platform)
    assertEquals("Google Meet", platform.displayName)
  }

  @Test
  fun detectPlatform_microsoftTeamsUrl_returnsTeams() {
    val platform =
        MeetingLinkValidator.detectPlatform("https://teams.microsoft.com/l/meetup-join/123")

    assertEquals(MeetingPlatform.MICROSOFT_TEAMS, platform)
    assertEquals("Microsoft Teams", platform.displayName)
  }

  @Test
  fun detectPlatform_teamsLiveUrl_returnsTeams() {
    val platform = MeetingLinkValidator.detectPlatform("https://teams.live.com/meet/123")

    assertEquals(MeetingPlatform.MICROSOFT_TEAMS, platform)
  }

  @Test
  fun detectPlatform_webexUrl_returnsWebex() {
    val platform = MeetingLinkValidator.detectPlatform("https://webex.com/meet/username")

    assertEquals(MeetingPlatform.WEBEX, platform)
    assertEquals("Webex", platform.displayName)
  }

  @Test
  fun detectPlatform_nonWhitelistedUrl_returnsUnknown() {
    val platform = MeetingLinkValidator.detectPlatform("https://jitsi.org/meeting123")

    assertEquals(MeetingPlatform.UNKNOWN, platform)
    assertEquals("Unknown Platform", platform.displayName)
  }

  @Test
  fun detectPlatform_caseInsensitive_detectsCorrectly() {
    val platform = MeetingLinkValidator.detectPlatform("https://ZOOM.US/j/1234567890")

    assertEquals(MeetingPlatform.ZOOM, platform)
  }
}
