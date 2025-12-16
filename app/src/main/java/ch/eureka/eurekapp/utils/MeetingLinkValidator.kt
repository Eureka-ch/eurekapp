package ch.eureka.eurekapp.utils

import java.net.URL

/** Supported video meeting platforms with their associated domains */
enum class MeetingPlatform(val displayName: String, val domains: List<String>) {
  ZOOM("Zoom", listOf("zoom.us", "zoom.com")),
  GOOGLE_MEET("Google Meet", listOf("meet.google.com")),
  MICROSOFT_TEAMS("Microsoft Teams", listOf("teams.microsoft.com", "teams.live.com")),
  WEBEX("Webex", listOf("webex.com")),
  UNKNOWN("Unknown Platform", emptyList())
}

/** Validates meeting links and detects video conferencing platforms */
object MeetingLinkValidator {

  private val WHITELISTED_DOMAINS =
      MeetingPlatform.values()
          .filter { it != MeetingPlatform.UNKNOWN }
          .flatMap { it.domains }
          .toSet()

  /**
   * Validates a meeting link URL
   *
   * @param url The URL to validate
   * @return Pair of (isValid, errorOrWarningMessage)
   *     - Valid whitelisted domains: (true, null)
   *     - Valid non-whitelisted domains: (true, "Warning: This link is not from a trusted
   *       platform")
   *     - Invalid format: (false, "Invalid URL format")
   */
  fun validateMeetingLink(url: String?): Pair<Boolean, String?> {
    if (url.isNullOrBlank()) {
      return Pair(false, "Meeting link is required")
    }

    if (!isValidUrlFormat(url)) {
      return Pair(false, "Invalid URL format")
    }

    val domain = extractDomain(url)
    if (domain == null) {
      return Pair(false, "Invalid URL format")
    }

    return if (isWhitelisted(domain)) {
      Pair(true, null)
    } else {
      Pair(true, "Warning: This link is not from a trusted platform")
    }
  }

  /**
   * Detects the meeting platform from a URL
   *
   * @param url The meeting URL
   * @return The detected MeetingPlatform or UNKNOWN if not recognized
   */
  fun detectPlatform(url: String?): MeetingPlatform {
    if (url.isNullOrBlank()) {
      return MeetingPlatform.UNKNOWN
    }

    val domain = extractDomain(url) ?: return MeetingPlatform.UNKNOWN

    return MeetingPlatform.values().find { platform ->
      platform.domains.any { platformDomain -> domain.contains(platformDomain) }
    } ?: MeetingPlatform.UNKNOWN
  }

  /** Checks if a URL has valid format */
  private fun isValidUrlFormat(url: String): Boolean {
    return try {
      val parsedUrl = URL(url)
      parsedUrl.protocol in listOf("http", "https") && parsedUrl.host.isNotBlank()
    } catch (e: Exception) {
      false
    }
  }

  /** Extracts the domain from a URL */
  private fun extractDomain(url: String): String? {
    return try {
      URL(url).host.lowercase()
    } catch (e: Exception) {
      null
    }
  }

  /** Checks if a domain is in the whitelist */
  private fun isWhitelisted(domain: String): Boolean {
    return WHITELISTED_DOMAINS.any { whitelistedDomain -> domain.contains(whitelistedDomain) }
  }
}
