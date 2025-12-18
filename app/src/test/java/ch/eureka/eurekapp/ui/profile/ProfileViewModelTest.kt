/* This code was written with help of Claude. */
package ch.eureka.eurekapp.ui.profile

import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.model.data.user.UserRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

  private lateinit var userRepository: UserRepository
  private lateinit var firebaseAuth: FirebaseAuth
  private lateinit var firebaseUser: FirebaseUser
  private lateinit var viewModel: ProfileViewModel
  private val testDispatcher = StandardTestDispatcher()

  private val testUser =
      User(
          uid = "test-uid",
          displayName = "Test User",
          email = "test@example.com",
          photoUrl = "https://example.com/photo.jpg",
          lastActive = Timestamp(1000, 0))

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)

    firebaseAuth = mockk(relaxed = true)
    firebaseUser = mockk(relaxed = true)
    every { firebaseUser.uid } returns "test-uid"
    every { firebaseAuth.currentUser } returns firebaseUser

    mockkStatic(FirebaseAuth::class)
    every { FirebaseAuth.getInstance() } returns firebaseAuth

    userRepository = mockk()
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    unmockkAll()
  }

  @Test
  fun initialState_hasNullUserAndNotEditing() {
    // Given
    every { userRepository.getUserById(any()) } returns flowOf(null)

    // When
    viewModel = ProfileViewModel(userRepository)
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    val state = viewModel.uiState.value
    assertNull(state.user)
    assertFalse(state.isEditing)
  }

  @Test
  fun loadUserProfile_loadsUserDataSuccessfully() = runTest {
    // Given
    every { userRepository.getUserById("test-uid") } returns flowOf(testUser)

    // When
    viewModel = ProfileViewModel(userRepository)
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    val state = viewModel.uiState.value
    assertEquals(testUser, state.user)
    assertFalse(state.isEditing)
    verify { userRepository.getUserById("test-uid") }
  }

  @Test(expected = IllegalStateException::class)
  fun loadUserProfile_throwsExceptionWhenFirebaseUserIsNull() = runTest {
    // Given
    every { firebaseAuth.currentUser } returns null

    // When/Then - should throw exception
    viewModel = ProfileViewModel(userRepository)
  }

  @Test
  fun loadUserProfile_updatesWhenUserDataChanges() = runTest {
    // Given
    val updatedUser = testUser.copy(displayName = "Updated Name")
    every { userRepository.getUserById("test-uid") } returns flowOf(testUser, updatedUser)

    // When
    viewModel = ProfileViewModel(userRepository)
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    val state = viewModel.uiState.value
    assertEquals("Updated Name", state.user?.displayName)
  }

  @Test
  fun setEditing_updatesIsEditingStateToTrue() = runTest {
    // Given
    every { userRepository.getUserById("test-uid") } returns flowOf(testUser)
    viewModel = ProfileViewModel(userRepository)
    testDispatcher.scheduler.advanceUntilIdle()

    // When
    viewModel.setEditing(true)
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    assertTrue(viewModel.uiState.value.isEditing)
  }

  @Test
  fun setEditing_updatesIsEditingStateToFalse() = runTest {
    // Given
    every { userRepository.getUserById("test-uid") } returns flowOf(testUser)
    viewModel = ProfileViewModel(userRepository)
    viewModel.setEditing(true)
    testDispatcher.scheduler.advanceUntilIdle()

    // When
    viewModel.setEditing(false)
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    assertFalse(viewModel.uiState.value.isEditing)
  }

  @Test
  fun updateDisplayName_savesUserWithNewDisplayName() = runTest {
    // Given
    every { userRepository.getUserById("test-uid") } returns flowOf(testUser)
    coEvery { userRepository.saveUser(any()) } returns Result.success(Unit)
    viewModel = ProfileViewModel(userRepository)
    testDispatcher.scheduler.advanceUntilIdle()

    // When
    viewModel.updateDisplayName("New Display Name")
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    coVerify {
      userRepository.saveUser(
          match { it.uid == "test-uid" && it.displayName == "New Display Name" })
    }
  }

  @Test
  fun updateDisplayName_preservesOtherUserFields() = runTest {
    // Given
    every { userRepository.getUserById("test-uid") } returns flowOf(testUser)
    coEvery { userRepository.saveUser(any()) } returns Result.success(Unit)
    viewModel = ProfileViewModel(userRepository)
    testDispatcher.scheduler.advanceUntilIdle()

    // When
    viewModel.updateDisplayName("New Name")
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    coVerify {
      userRepository.saveUser(
          match {
            it.uid == testUser.uid &&
                it.email == testUser.email &&
                it.photoUrl == testUser.photoUrl &&
                it.lastActive == testUser.lastActive
          })
    }
  }

  @Test
  fun updateDisplayName_setsIsEditingToFalseAfterSave() = runTest {
    // Given
    every { userRepository.getUserById("test-uid") } returns flowOf(testUser)
    coEvery { userRepository.saveUser(any()) } returns Result.success(Unit)
    viewModel = ProfileViewModel(userRepository)
    viewModel.setEditing(true)
    testDispatcher.scheduler.advanceUntilIdle()

    // When
    viewModel.updateDisplayName("New Name")
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    assertFalse(viewModel.uiState.value.isEditing)
  }

  @Test(expected = IllegalStateException::class)
  fun updateDisplayName_throwsExceptionWhenUserIsNull() = runTest {
    // Given
    every { userRepository.getUserById("test-uid") } returns flowOf(null)
    viewModel = ProfileViewModel(userRepository)
    testDispatcher.scheduler.advanceUntilIdle()

    // When
    viewModel.updateDisplayName("New Name")
    testDispatcher.scheduler.advanceUntilIdle()
  }

  @Test
  fun updateDisplayName_handlesEmptyString() = runTest {
    // Given
    every { userRepository.getUserById("test-uid") } returns flowOf(testUser)
    coEvery { userRepository.saveUser(any()) } returns Result.success(Unit)
    viewModel = ProfileViewModel(userRepository)
    testDispatcher.scheduler.advanceUntilIdle()

    // When
    viewModel.updateDisplayName("")
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    coVerify { userRepository.saveUser(match { it.displayName == "" }) }
  }

  @Test
  fun updateDisplayName_handlesVeryLongString() = runTest {
    // Given
    val longName = "A".repeat(1000)
    every { userRepository.getUserById("test-uid") } returns flowOf(testUser)
    coEvery { userRepository.saveUser(any()) } returns Result.success(Unit)
    viewModel = ProfileViewModel(userRepository)
    testDispatcher.scheduler.advanceUntilIdle()

    // When
    viewModel.updateDisplayName(longName)
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    coVerify { userRepository.saveUser(match { it.displayName == longName }) }
  }

  @Test
  fun updateDisplayName_handlesSpecialCharacters() = runTest {
    // Given
    val specialName = "Test@#$%^&*()_+-=[]{}|;':\",./<>?"
    every { userRepository.getUserById("test-uid") } returns flowOf(testUser)
    coEvery { userRepository.saveUser(any()) } returns Result.success(Unit)
    viewModel = ProfileViewModel(userRepository)
    testDispatcher.scheduler.advanceUntilIdle()

    // When
    viewModel.updateDisplayName(specialName)
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    coVerify { userRepository.saveUser(match { it.displayName == specialName }) }
  }

  @Test
  fun user_handledCorrectlyWithEmptyPhotoUrl() = runTest {
    // Given
    val userWithoutPhoto = testUser.copy(photoUrl = "")
    every { userRepository.getUserById("test-uid") } returns flowOf(userWithoutPhoto)

    // When
    viewModel = ProfileViewModel(userRepository)
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    val state = viewModel.uiState.value
    assertEquals("", state.user?.photoUrl)
  }

  // Error Handling Tests

  @Test
  fun updateDisplayName_handlesRepositorySaveFailure() = runTest {
    // Given
    every { userRepository.getUserById("test-uid") } returns flowOf(testUser)
    coEvery { userRepository.saveUser(any()) } returns Result.failure(Exception("Network error"))
    viewModel = ProfileViewModel(userRepository)
    viewModel.setEditing(true)
    testDispatcher.scheduler.advanceUntilIdle()

    // When
    viewModel.updateDisplayName("New Name")
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    // Even on failure, editing mode should be set to false
    assertFalse(viewModel.uiState.value.isEditing)
    coVerify { userRepository.saveUser(any()) }
  }

  @Test
  fun updateDisplayName_handlesUnicodeCharacters() = runTest {
    // Given
    val unicodeName = "Test 测试 テスト 🎉 العربية"
    every { userRepository.getUserById("test-uid") } returns flowOf(testUser)
    coEvery { userRepository.saveUser(any()) } returns Result.success(Unit)
    viewModel = ProfileViewModel(userRepository)
    testDispatcher.scheduler.advanceUntilIdle()

    // When
    viewModel.updateDisplayName(unicodeName)
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    coVerify { userRepository.saveUser(match { it.displayName == unicodeName }) }
  }

  @Test
  fun updateDisplayName_handlesMultipleRapidCalls() = runTest {
    // Given
    every { userRepository.getUserById("test-uid") } returns flowOf(testUser)
    coEvery { userRepository.saveUser(any()) } returns Result.success(Unit)
    viewModel = ProfileViewModel(userRepository)
    testDispatcher.scheduler.advanceUntilIdle()

    // When
    viewModel.updateDisplayName("Name 1")
    viewModel.updateDisplayName("Name 2")
    viewModel.updateDisplayName("Name 3")
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    coVerify(atLeast = 3) { userRepository.saveUser(any()) }
  }

  @Test
  fun setEditing_handlesMultipleTimesInQuickSuccession() = runTest {
    // Given
    every { userRepository.getUserById("test-uid") } returns flowOf(testUser)
    viewModel = ProfileViewModel(userRepository)
    testDispatcher.scheduler.advanceUntilIdle()

    // When
    viewModel.setEditing(true)
    viewModel.setEditing(false)
    viewModel.setEditing(true)
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    assertTrue(viewModel.uiState.value.isEditing)
  }

  @Test
  fun updateDisplayName_preservesTimestampAccurately() = runTest {
    // Given
    val specificTimestamp = Timestamp(9999999, 12345)
    val userWithTimestamp = testUser.copy(lastActive = specificTimestamp)
    every { userRepository.getUserById("test-uid") } returns flowOf(userWithTimestamp)
    coEvery { userRepository.saveUser(any()) } returns Result.success(Unit)
    viewModel = ProfileViewModel(userRepository)
    testDispatcher.scheduler.advanceUntilIdle()

    // When
    viewModel.updateDisplayName("New Name")
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    coVerify {
      userRepository.saveUser(
          match {
            it.lastActive.seconds == specificTimestamp.seconds &&
                it.lastActive.nanoseconds == specificTimestamp.nanoseconds
          })
    }
  }

  @Test
  fun userFlow_emitsMultipleValuesRapidly() = runTest {
    // Given
    val flow =
        kotlinx.coroutines.flow.flow {
          emit(testUser)
          emit(testUser.copy(displayName = "Update 1"))
          emit(testUser.copy(displayName = "Update 2"))
          emit(testUser.copy(displayName = "Final Update"))
        }
    every { userRepository.getUserById("test-uid") } returns flow

    // When
    viewModel = ProfileViewModel(userRepository)
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    assertEquals("Final Update", viewModel.uiState.value.user?.displayName)
  }

  @Test
  fun updateDisplayName_maintainsUIDConsistency() = runTest {
    // Given
    every { userRepository.getUserById("test-uid") } returns flowOf(testUser)
    coEvery { userRepository.saveUser(any()) } returns Result.success(Unit)
    viewModel = ProfileViewModel(userRepository)
    testDispatcher.scheduler.advanceUntilIdle()
    val originalUid = testUser.uid

    // When
    viewModel.updateDisplayName("Different Name")
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    coVerify { userRepository.saveUser(match { it.uid == originalUid }) }
  }

  @Test
  fun updateDisplayName_handlesWhitespaceOnlyString() = runTest {
    // Given
    every { userRepository.getUserById("test-uid") } returns flowOf(testUser)
    coEvery { userRepository.saveUser(any()) } returns Result.success(Unit)
    viewModel = ProfileViewModel(userRepository)
    testDispatcher.scheduler.advanceUntilIdle()

    // When
    viewModel.updateDisplayName("   ")
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    coVerify { userRepository.saveUser(match { it.displayName == "   " }) }
  }

  @Test
  fun updateDisplayName_handlesNewlineCharacters() = runTest {
    // Given
    val nameWithNewlines = "Test\nUser\nName"
    every { userRepository.getUserById("test-uid") } returns flowOf(testUser)
    coEvery { userRepository.saveUser(any()) } returns Result.success(Unit)
    viewModel = ProfileViewModel(userRepository)
    testDispatcher.scheduler.advanceUntilIdle()

    // When
    viewModel.updateDisplayName(nameWithNewlines)
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    coVerify { userRepository.saveUser(match { it.displayName == nameWithNewlines }) }
  }

  @Test
  fun user_handledCorrectlyWithNullEmail() = runTest {
    // Given
    val userWithNullEmail = testUser.copy(email = "")
    every { userRepository.getUserById("test-uid") } returns flowOf(userWithNullEmail)

    // When
    viewModel = ProfileViewModel(userRepository)
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    val state = viewModel.uiState.value
    assertEquals("", state.user?.email)
  }

  @Test
  fun updateDisplayName_preservesEmailWhenUpdating() = runTest {
    // Given
    every { userRepository.getUserById("test-uid") } returns flowOf(testUser)
    coEvery { userRepository.saveUser(any()) } returns Result.success(Unit)
    viewModel = ProfileViewModel(userRepository)
    testDispatcher.scheduler.advanceUntilIdle()
    val originalEmail = testUser.email

    // When
    viewModel.updateDisplayName("New Name")
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    coVerify { userRepository.saveUser(match { it.email == originalEmail }) }
  }

  @Test
  fun setEditing_doesNotTriggerSaveWithTrue() = runTest {
    // Given
    every { userRepository.getUserById("test-uid") } returns flowOf(testUser)
    viewModel = ProfileViewModel(userRepository)
    testDispatcher.scheduler.advanceUntilIdle()

    // When
    viewModel.setEditing(true)
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    coVerify(exactly = 0) { userRepository.saveUser(any()) }
  }

  @Test
  fun setEditing_doesNotTriggerSaveWithFalse() = runTest {
    // Given
    every { userRepository.getUserById("test-uid") } returns flowOf(testUser)
    viewModel = ProfileViewModel(userRepository)
    viewModel.setEditing(true)
    testDispatcher.scheduler.advanceUntilIdle()

    // When
    viewModel.setEditing(false)
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    coVerify(exactly = 0) { userRepository.saveUser(any()) }
  }

  @Test
  fun updateDisplayName_stillTriggersSaveWithSameName() = runTest {
    // Given
    every { userRepository.getUserById("test-uid") } returns flowOf(testUser)
    coEvery { userRepository.saveUser(any()) } returns Result.success(Unit)
    viewModel = ProfileViewModel(userRepository)
    testDispatcher.scheduler.advanceUntilIdle()

    // When
    viewModel.updateDisplayName(testUser.displayName)
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    coVerify { userRepository.saveUser(any()) }
  }

  @Test
  fun userState_isNullInitiallyBeforeRepositoryReturns() {
    // Given
    every { userRepository.getUserById("test-uid") } returns flowOf(testUser)

    // When
    viewModel = ProfileViewModel(userRepository)
    // Don't advance the dispatcher

    // Then
    val state = viewModel.uiState.value
    assertNull(state.user)
  }

  @Test
  fun updateDisplayName_handlesTrailingAndLeadingWhitespace() = runTest {
    // Given
    val nameWithWhitespace = "  Test User  "
    every { userRepository.getUserById("test-uid") } returns flowOf(testUser)
    coEvery { userRepository.saveUser(any()) } returns Result.success(Unit)
    viewModel = ProfileViewModel(userRepository)
    testDispatcher.scheduler.advanceUntilIdle()

    // When
    viewModel.updateDisplayName(nameWithWhitespace)
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    coVerify { userRepository.saveUser(match { it.displayName == nameWithWhitespace }) }
  }

  @Test
  fun updateDisplayName_stillWorksAfterSetEditingFalse() = runTest {
    // Given
    every { userRepository.getUserById("test-uid") } returns flowOf(testUser)
    coEvery { userRepository.saveUser(any()) } returns Result.success(Unit)
    viewModel = ProfileViewModel(userRepository)
    viewModel.setEditing(true)
    viewModel.setEditing(false)
    testDispatcher.scheduler.advanceUntilIdle()

    // When
    viewModel.updateDisplayName("New Name")
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    coVerify { userRepository.saveUser(match { it.displayName == "New Name" }) }
    assertFalse(viewModel.uiState.value.isEditing)
  }
}
