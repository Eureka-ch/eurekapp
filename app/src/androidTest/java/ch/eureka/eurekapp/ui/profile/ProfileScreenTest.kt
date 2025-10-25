package ch.eureka.eurekapp.ui.profile

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.model.data.user.UserRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Fake UserRepository for testing */
class FakeUserRepository : UserRepository {
  val userFlow = MutableStateFlow<User?>(null)
  var saveUserResult: Result<Unit> = Result.success(Unit)
  val savedUsers = mutableListOf<User>()

  override fun getUserById(userId: String) = userFlow

  override fun getCurrentUser() = userFlow

  override suspend fun saveUser(user: User): Result<Unit> {
    savedUsers.add(user)
    if (saveUserResult.isSuccess) {
      userFlow.value = user
    }
    return saveUserResult
  }

  override suspend fun updateLastActive(userId: String) = Result.success(Unit)
}

@RunWith(AndroidJUnit4::class)
class ProfileScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var userRepository: FakeUserRepository
  private lateinit var firebaseAuth: FirebaseAuth

  private val testUserId = "test-uid"
  private val testUser =
      User(
          uid = testUserId,
          displayName = "John Doe",
          email = "john.doe@example.com",
          photoUrl = "https://example.com/photo.jpg",
          lastActive = Timestamp(1234567890, 0))

  private val testUserWithoutPhoto =
      User(
          uid = "test-uid-2",
          displayName = "Jane Smith",
          email = "jane.smith@example.com",
          photoUrl = "",
          lastActive = Timestamp(1234567890, 0))

  @Before
  fun setup() {
    // Mock FirebaseAuth
    firebaseAuth = mockk(relaxed = true)

    // Use Fake UserRepository
    userRepository = FakeUserRepository()
    userRepository.userFlow.value = testUser
    userRepository.saveUserResult = Result.success(Unit)
  }

  @After
  fun tearDown() {
    unmockkAll()
  }

  @Test
  fun profileScreen_displaysUserInformation() {
    // Given
    val viewModel = ProfileViewModel(userRepository, testUserId)

    // When
    composeTestRule.setContent { ProfileScreen(viewModel = viewModel, firebaseAuth = firebaseAuth) }

    // Then
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.PROFILE_SCREEN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_TEXT).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_TEXT)
        .assertTextEquals("John Doe")
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.EMAIL_TEXT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.LAST_ACTIVE_TEXT).assertIsDisplayed()
  }

  @Test
  fun profileScreen_displaysProfilePictureWhenPhotoUrlExists() {
    // Given
    val viewModel = ProfileViewModel(userRepository, testUserId)

    // When
    composeTestRule.setContent { ProfileScreen(viewModel = viewModel, firebaseAuth = firebaseAuth) }

    // Then
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.PROFILE_PICTURE).assertIsDisplayed()
  }

  @Test
  fun profileScreen_displaysFallbackIconWhenPhotoUrlEmpty() {
    // Given
    userRepository.userFlow.value = testUserWithoutPhoto
    val viewModel = ProfileViewModel(userRepository, testUserId)

    // When
    composeTestRule.setContent { ProfileScreen(viewModel = viewModel, firebaseAuth = firebaseAuth) }

    // Then
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.PROFILE_PICTURE).assertIsDisplayed()
  }

  @Test
  fun profileScreen_displaysEditButton() {
    // Given
    val viewModel = ProfileViewModel(userRepository, testUserId)

    // When
    composeTestRule.setContent { ProfileScreen(viewModel = viewModel, firebaseAuth = firebaseAuth) }

    // Then
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.EDIT_BUTTON).assertIsDisplayed()
  }

  @Test
  fun profileScreen_displaysSignOutButton() {
    // Given
    val viewModel = ProfileViewModel(userRepository, testUserId)

    // When
    composeTestRule.setContent { ProfileScreen(viewModel = viewModel, firebaseAuth = firebaseAuth) }

    // Then
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.SIGN_OUT_BUTTON).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.SIGN_OUT_BUTTON)
        .assertTextContains("Sign Out")
  }

  @Test
  fun profileScreen_clickEditButton_showsEditMode() {
    // Given
    val viewModel = ProfileViewModel(userRepository, testUserId)
    composeTestRule.setContent { ProfileScreen(viewModel = viewModel, firebaseAuth = firebaseAuth) }

    // When
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.EDIT_BUTTON).performClick()

    // Then
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.SAVE_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.CANCEL_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.EDIT_BUTTON).assertDoesNotExist()
  }

  @Test
  fun profileScreen_editMode_displayNameFieldContainsCurrentName() {
    // Given
    val viewModel = ProfileViewModel(userRepository, testUserId)
    composeTestRule.setContent { ProfileScreen(viewModel = viewModel, firebaseAuth = firebaseAuth) }

    // When
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.EDIT_BUTTON).performClick()

    // Then
    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD)
        .assertTextEquals("Display Name", "John Doe")
  }

  @Test
  fun profileScreen_editMode_canTypeInDisplayNameField() {
    // Given
    val viewModel = ProfileViewModel(userRepository, testUserId)
    composeTestRule.setContent { ProfileScreen(viewModel = viewModel, firebaseAuth = firebaseAuth) }
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.EDIT_BUTTON).performClick()

    // When
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD).performTextClearance()
    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD)
        .performTextInput("New Name")

    // Then
    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD)
        .assertTextEquals("Display Name", "New Name")
  }

  @Test
  fun profileScreen_editMode_clickCancel_exitsEditMode() {
    // Given
    val viewModel = ProfileViewModel(userRepository, testUserId)
    composeTestRule.setContent { ProfileScreen(viewModel = viewModel, firebaseAuth = firebaseAuth) }
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.EDIT_BUTTON).performClick()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD).performTextClearance()
    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD)
        .performTextInput("Changed Name")

    // When
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.CANCEL_BUTTON).performClick()

    // Then
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.EDIT_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD).assertDoesNotExist()
    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_TEXT)
        .assertTextEquals("John Doe")
  }

  @Test
  fun profileScreen_editMode_clickSave_savesDisplayName() {
    // Given
    val viewModel = ProfileViewModel(userRepository, testUserId)
    composeTestRule.setContent { ProfileScreen(viewModel = viewModel, firebaseAuth = firebaseAuth) }
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.EDIT_BUTTON).performClick()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD).performTextClearance()
    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD)
        .performTextInput("Updated Name")

    // When
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.SAVE_BUTTON).performClick()

    // Then
    composeTestRule.waitForIdle()
    assert(userRepository.savedUsers.any { it.displayName == "Updated Name" })
  }

  @Test
  fun profileScreen_editMode_clickSave_exitsEditMode() {
    // Given
    val viewModel = ProfileViewModel(userRepository, testUserId)
    composeTestRule.setContent { ProfileScreen(viewModel = viewModel, firebaseAuth = firebaseAuth) }
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.EDIT_BUTTON).performClick()

    // When
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.SAVE_BUTTON).performClick()

    // Then
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.EDIT_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD).assertDoesNotExist()
  }

  @Test
  fun profileScreen_displaysErrorWhenNoUserData() {
    // Given
    userRepository.userFlow.value = null
    val viewModel = ProfileViewModel(userRepository, testUserId)

    // When
    composeTestRule.setContent { ProfileScreen(viewModel = viewModel, firebaseAuth = firebaseAuth) }

    // Then
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.ERROR_TEXT).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.ERROR_TEXT)
        .assertTextEquals("No user data available")
  }

  @Test
  fun profileScreen_noUserData_doesNotShowEditButton() {
    // Given
    userRepository.userFlow.value = null
    val viewModel = ProfileViewModel(userRepository, testUserId)

    // When
    composeTestRule.setContent { ProfileScreen(viewModel = viewModel, firebaseAuth = firebaseAuth) }

    // Then
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.EDIT_BUTTON).assertDoesNotExist()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_TEXT).assertDoesNotExist()
  }

  @Test
  fun profileScreen_clickSignOut_callsFirebaseSignOut() {
    // Given
    val viewModel = ProfileViewModel(userRepository, testUserId)
    composeTestRule.setContent { ProfileScreen(viewModel = viewModel, firebaseAuth = firebaseAuth) }

    // When
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.SIGN_OUT_BUTTON).performClick()

    // Then
    verify { firebaseAuth.signOut() }
  }

  @Test
  fun profileScreen_editMode_saveEmptyName_savesEmptyString() {
    // Given
    val viewModel = ProfileViewModel(userRepository, testUserId)
    composeTestRule.setContent { ProfileScreen(viewModel = viewModel, firebaseAuth = firebaseAuth) }
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.EDIT_BUTTON).performClick()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD).performTextClearance()

    // When
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.SAVE_BUTTON).performClick()

    // Then
    composeTestRule.waitForIdle()
    assert(userRepository.savedUsers.any { it.displayName == "" })
  }

  @Test
  fun profileScreen_editMode_saveSpecialCharacters_savesCorrectly() {
    // Given
    val specialName = "Test@#$%Name"
    val viewModel = ProfileViewModel(userRepository, testUserId)
    composeTestRule.setContent { ProfileScreen(viewModel = viewModel, firebaseAuth = firebaseAuth) }
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.EDIT_BUTTON).performClick()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD).performTextClearance()
    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD)
        .performTextInput(specialName)

    // When
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.SAVE_BUTTON).performClick()

    // Then
    composeTestRule.waitForIdle()
    assert(userRepository.savedUsers.any { it.displayName == specialName })
  }

  @Test
  fun profileScreen_userDataUpdates_displaysNewData() {
    // Given
    val viewModel = ProfileViewModel(userRepository, testUserId)
    composeTestRule.setContent { ProfileScreen(viewModel = viewModel, firebaseAuth = firebaseAuth) }

    // When
    userRepository.userFlow.value = testUser.copy(displayName = "Updated User")
    composeTestRule.waitForIdle()

    // Then
    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_TEXT)
        .assertTextEquals("Updated User")
  }

  @Test
  fun profileScreen_multipleEditCancelCycles_worksCorrectly() {
    // Given
    val viewModel = ProfileViewModel(userRepository, testUserId)
    composeTestRule.setContent { ProfileScreen(viewModel = viewModel, firebaseAuth = firebaseAuth) }

    // When & Then - first
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.EDIT_BUTTON).performClick()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.CANCEL_BUTTON).performClick()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.EDIT_BUTTON).assertIsDisplayed()

    // Second
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.EDIT_BUTTON).performClick()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.CANCEL_BUTTON).performClick()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.EDIT_BUTTON).assertIsDisplayed()
  }

  @Test
  fun profileScreen_hasWhiteBackground() {
    // Given
    val viewModel = ProfileViewModel(userRepository, testUserId)

    // When
    composeTestRule.setContent { ProfileScreen(viewModel = viewModel, firebaseAuth = firebaseAuth) }

    // Then
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.PROFILE_SCREEN).assertIsDisplayed()
  }

  @Test
  fun profileScreen_repositorySaveFails_stillExitsEditMode() {
    // Given
    userRepository.saveUserResult = Result.failure(Exception("Save failed"))
    val viewModel = ProfileViewModel(userRepository, testUserId)
    composeTestRule.setContent { ProfileScreen(viewModel = viewModel, firebaseAuth = firebaseAuth) }
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.EDIT_BUTTON).performClick()

    // When
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.SAVE_BUTTON).performClick()

    // Then
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.EDIT_BUTTON).assertIsDisplayed()
  }

  @Test
  fun profileScreen_withUnicodeCharacters_displaysCorrectly() {
    // Given
    userRepository.userFlow.value = testUser.copy(displayName = "Test 测试 テスト 🎉 العربية")
    val viewModel = ProfileViewModel(userRepository, testUserId)

    // When
    composeTestRule.setContent { ProfileScreen(viewModel = viewModel, firebaseAuth = firebaseAuth) }

    // Then
    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_TEXT)
        .assertTextEquals("Test 测试 テスト 🎉 العربية")
  }

  @Test
  fun profileScreen_editAndCancel_restoresOriginalName() {
    // Given
    val viewModel = ProfileViewModel(userRepository, testUserId)
    composeTestRule.setContent { ProfileScreen(viewModel = viewModel, firebaseAuth = firebaseAuth) }
    val originalName = "John Doe"

    // When
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.EDIT_BUTTON).performClick()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD).performTextClearance()
    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD)
        .performTextInput("Changed")
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.CANCEL_BUTTON).performClick()

    // Then
    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_TEXT)
        .assertTextEquals(originalName)
  }

  @Test
  fun profileScreen_userChangesFromNullToValid_displaysUser() {
    // Given
    userRepository.userFlow.value = null
    val viewModel = ProfileViewModel(userRepository, testUserId)
    composeTestRule.setContent { ProfileScreen(viewModel = viewModel, firebaseAuth = firebaseAuth) }
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.ERROR_TEXT).assertIsDisplayed()

    // When
    userRepository.userFlow.value = testUser
    composeTestRule.waitForIdle()

    // Then
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_TEXT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.ERROR_TEXT).assertDoesNotExist()
  }

  @Test
  fun profileScreen_userChangesFromValidToNull_showsError() {
    // Given
    val viewModel = ProfileViewModel(userRepository, testUserId)
    composeTestRule.setContent { ProfileScreen(viewModel = viewModel, firebaseAuth = firebaseAuth) }
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_TEXT).assertIsDisplayed()

    // When
    userRepository.userFlow.value = null
    composeTestRule.waitForIdle()

    // Then
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.ERROR_TEXT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_TEXT).assertDoesNotExist()
  }

  @Test
  fun profileScreen_photoUrlChanges_displaysNewPhoto() {
    // Given
    val viewModel = ProfileViewModel(userRepository, testUserId)
    composeTestRule.setContent { ProfileScreen(viewModel = viewModel, firebaseAuth = firebaseAuth) }

    // When
    userRepository.userFlow.value = testUser.copy(photoUrl = "https://newphoto.com/image.jpg")
    composeTestRule.waitForIdle()

    // Then
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.PROFILE_PICTURE).assertIsDisplayed()
  }

  @Test
  fun profileScreen_photoUrlChangesToEmpty_showsFallbackIcon() {
    // Given
    val viewModel = ProfileViewModel(userRepository, testUserId)
    composeTestRule.setContent { ProfileScreen(viewModel = viewModel, firebaseAuth = firebaseAuth) }

    // When
    userRepository.userFlow.value = testUser.copy(photoUrl = "")
    composeTestRule.waitForIdle()

    // Then
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.PROFILE_PICTURE).assertIsDisplayed()
  }

  @Test
  fun profileScreen_editModeActiveWhenUserChanges_handlesGracefully() {
    // Given
    val viewModel = ProfileViewModel(userRepository, testUserId)
    composeTestRule.setContent { ProfileScreen(viewModel = viewModel, firebaseAuth = firebaseAuth) }
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.EDIT_BUTTON).performClick()

    // When
    userRepository.userFlow.value = testUser.copy(displayName = "New Name From Server")
    composeTestRule.waitForIdle()

    // Then
    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD)
        .assertTextEquals("Display Name", "New Name From Server")
  }

  @Test
  fun profileScreen_editMode_enforcesMaxCharacterLimit() {
    // Given
    val viewModel = ProfileViewModel(userRepository, testUserId)
    composeTestRule.setContent { ProfileScreen(viewModel = viewModel, firebaseAuth = firebaseAuth) }
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.EDIT_BUTTON).performClick()
    composeTestRule.waitForIdle()

    // When
    val maxText = "a".repeat(50)
    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD)
        .performTextReplacement(maxText)
    composeTestRule.waitForIdle()

    // Then
    composeTestRule.onNodeWithText(maxText).assertExists()
    composeTestRule.onNodeWithText("50/50").assertExists()

    // When
    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD)
        .performTextInput("charles")
    composeTestRule.waitForIdle()

    // Then
    composeTestRule.onNodeWithText(maxText).assertExists()
    composeTestRule.onNodeWithText("50/50").assertExists()
  }
}
