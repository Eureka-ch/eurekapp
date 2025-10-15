package ch.eureka.eurekapp.model.data.user

import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Test suite for User model.
 *
 * Note: Some of these tests were co-authored by Claude Code.
 */
class UserTest {

  @Test
  fun user_defaultConstructor_createsEmptyUser() {
    val user = User()

    assertEquals("", user.uid)
    assertEquals("", user.displayName)
    assertEquals("", user.email)
    assertEquals("", user.photoUrl)
    assertEquals(Timestamp(0, 0), user.lastActive)
  }

  @Test
  fun user_withParameters_setsCorrectValues() {
    val timestamp = Timestamp(1000, 0)
    val user =
        User(
            uid = "user123",
            displayName = "John Doe",
            email = "john@example.com",
            photoUrl = "https://example.com/photo.jpg",
            lastActive = timestamp)

    assertEquals("user123", user.uid)
    assertEquals("John Doe", user.displayName)
    assertEquals("john@example.com", user.email)
    assertEquals("https://example.com/photo.jpg", user.photoUrl)
    assertEquals(timestamp, user.lastActive)
  }

  @Test
  fun user_copy_createsNewInstance() {
    val user = User(uid = "user123", displayName = "John Doe", email = "john@example.com")
    val copiedUser = user.copy(displayName = "Jane Doe")

    assertEquals("user123", copiedUser.uid)
    assertEquals("Jane Doe", copiedUser.displayName)
    assertEquals("john@example.com", copiedUser.email)
  }

  @Test
  fun user_equals_comparesCorrectly() {
    val user1 = User(uid = "user123", displayName = "John Doe", email = "john@example.com")
    val user2 = User(uid = "user123", displayName = "John Doe", email = "john@example.com")
    val user3 = User(uid = "user456", displayName = "Jane Doe", email = "jane@example.com")

    assertEquals(user1, user2)
    assertNotEquals(user1, user3)
  }

  @Test
  fun user_hashCode_isConsistent() {
    val user1 = User(uid = "user123", displayName = "John Doe", email = "john@example.com")
    val user2 = User(uid = "user123", displayName = "John Doe", email = "john@example.com")

    assertEquals(user1.hashCode(), user2.hashCode())
  }

  @Test
  fun user_toString_containsAllFields() {
    val user = User(uid = "user123", displayName = "John Doe", email = "john@example.com")
    val userString = user.toString()

    assert(userString.contains("user123"))
    assert(userString.contains("John Doe"))
    assert(userString.contains("john@example.com"))
  }
}
