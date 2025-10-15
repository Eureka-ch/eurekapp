package ch.eureka.eurekapp.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.model.data.user.UserRepository
import ch.eureka.eurekapp.ui.profile.ProfileViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var userRepository: UserRepository
  private lateinit var firebaseAuth: FirebaseAuth
  private lateinit var firebaseUser: FirebaseUser
  private lateinit var userFlow: MutableStateFlow<User?>

  private val testUser =
      User(
          uid = "test-uid",
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
    firebaseUser = mockk(relaxed = true)
    every { firebaseUser.uid } returns "test-uid"
    every { firebaseAuth.currentUser } returns firebaseUser

    mockkStatic(FirebaseAuth::class)
    every { FirebaseAuth.getInstance() } returns firebaseAuth

    // Mock Userrepository
    userRepository = mockk()
    userFlow = MutableStateFlow(testUser)
    every { userRepository.getUserById(any()) } returns userFlow
    coEvery { userRepository.saveUser(any()) } returns Result.success(Unit)
  }

  @After
  fun tearDown() {
    unmockkAll()
  }

  @Test
  fun profileScreen_displaysUserInformation() {
    // Given
    val viewModel = ProfileViewModel(userRepository)

    // When
    composeTestRule.setContent { ProfileScreen(viewModel = viewModel) }

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
    val viewModel = ProfileViewModel(userRepository)

    // When
    composeTestRule.setContent { ProfileScreen(viewModel = viewModel) }

    // Then
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.PROFILE_PICTURE).assertIsDisplayed()
  }

  @Test
  fun profileScreen_displaysFallbackIconWhenPhotoUrlEmpty() {
    // Given
    userFlow.value = testUserWithoutPhoto
    val viewModel = ProfileViewModel(userRepository)

    // When
    composeTestRule.setContent { ProfileScreen(viewModel = viewModel) }

    // Then
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.PROFILE_PICTURE).assertIsDisplayed()
  }

  @Test
  fun profileScreen_displaysEditButton() {
    // Given
    val viewModel = ProfileViewModel(userRepository)

    // When
    composeTestRule.setContent { ProfileScreen(viewModel = viewModel) }

    // Then
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.EDIT_BUTTON).assertIsDisplayed()
  }

  @Test
  fun profileScreen_displaysSignOutButton() {
    // Given
    val viewModel = ProfileViewModel(userRepository)

    // When
    composeTestRule.setContent { ProfileScreen(viewModel = viewModel) }

    // Then
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.SIGN_OUT_BUTTON).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.SIGN_OUT_BUTTON)
        .assertTextContains("Sign Out")
  }

  @Test
  fun profileScreen_clickEditButton_showsEditMode() {
    // Given
    val viewModel = ProfileViewModel(userRepository)
    composeTestRule.setContent { ProfileScreen(viewModel = viewModel) }

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
    val viewModel = ProfileViewModel(userRepository)
    composeTestRule.setContent { ProfileScreen(viewModel = viewModel) }

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
    val viewModel = ProfileViewModel(userRepository)
    composeTestRule.setContent { ProfileScreen(viewModel = viewModel) }
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
    val viewModel = ProfileViewModel(userRepository)
    composeTestRule.setContent { ProfileScreen(viewModel = viewModel) }
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
    val viewModel = ProfileViewModel(userRepository)
    composeTestRule.setContent { ProfileScreen(viewModel = viewModel) }
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.EDIT_BUTTON).performClick()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD).performTextClearance()
    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD)
        .performTextInput("Updated Name")

    // When
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.SAVE_BUTTON).performClick()

    // Then
    composeTestRule.waitForIdle()
    coVerify { userRepository.saveUser(match { it.displayName == "Updated Name" }) }
  }

  @Test
  fun profileScreen_editMode_clickSave_exitsEditMode() {
    // Given
    val viewModel = ProfileViewModel(userRepository)
    composeTestRule.setContent { ProfileScreen(viewModel = viewModel) }
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
    userFlow.value = null
    val viewModel = ProfileViewModel(userRepository)

    // When
    composeTestRule.setContent { ProfileScreen(viewModel = viewModel) }

    // Then
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.ERROR_TEXT).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.ERROR_TEXT)
        .assertTextEquals("No user data available")
  }

  @Test
  fun profileScreen_noUserData_doesNotShowEditButton() {
    // Given
    userFlow.value = null
    val viewModel = ProfileViewModel(userRepository)

    // When
    composeTestRule.setContent { ProfileScreen(viewModel = viewModel) }

    // Then
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.EDIT_BUTTON).assertDoesNotExist()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_TEXT).assertDoesNotExist()
  }

  @Test
  fun profileScreen_clickSignOut_callsFirebaseSignOut() {
    // Given
    val viewModel = ProfileViewModel(userRepository)
    composeTestRule.setContent { ProfileScreen(viewModel = viewModel) }

    // When
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.SIGN_OUT_BUTTON).performClick()

    // Then
    verify { firebaseAuth.signOut() }
  }

  @Test
  fun profileScreen_editMode_saveEmptyName_savesEmptyString() {
    // Given
    val viewModel = ProfileViewModel(userRepository)
    composeTestRule.setContent { ProfileScreen(viewModel = viewModel) }
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.EDIT_BUTTON).performClick()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD).performTextClearance()

    // When
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.SAVE_BUTTON).performClick()

    // Then
    composeTestRule.waitForIdle()
    coVerify { userRepository.saveUser(match { it.displayName == "" }) }
  }

  @Test
  fun profileScreen_editMode_saveLongName_savesLongString() {
    // Given
    val longName = "A".repeat(100)
    val viewModel = ProfileViewModel(userRepository)
    composeTestRule.setContent { ProfileScreen(viewModel = viewModel) }
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.EDIT_BUTTON).performClick()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD).performTextClearance()
    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD)
        .performTextInput(longName)

    // When
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.SAVE_BUTTON).performClick()

    // Then
    composeTestRule.waitForIdle()
    coVerify { userRepository.saveUser(match { it.displayName == longName }) }
  }

  @Test
  fun profileScreen_editMode_saveSpecialCharacters_savesCorrectly() {
    // Given
    val specialName = "Test@#$%Name"
    val viewModel = ProfileViewModel(userRepository)
    composeTestRule.setContent { ProfileScreen(viewModel = viewModel) }
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.EDIT_BUTTON).performClick()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD).performTextClearance()
    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD)
        .performTextInput(specialName)

    // When
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.SAVE_BUTTON).performClick()

    // Then
    composeTestRule.waitForIdle()
    coVerify { userRepository.saveUser(match { it.displayName == specialName }) }
  }

  @Test
  fun profileScreen_userDataUpdates_displaysNewData() {
    // Given
    val viewModel = ProfileViewModel(userRepository)
    composeTestRule.setContent { ProfileScreen(viewModel = viewModel) }

    // When
    userFlow.value = testUser.copy(displayName = "Updated User")
    composeTestRule.waitForIdle()

    // Then
    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_TEXT)
        .assertTextEquals("Updated User")
  }

  @Test
  fun profileScreen_multipleEditCancelCycles_worksCorrectly() {
    // Given
    val viewModel = ProfileViewModel(userRepository)
    composeTestRule.setContent { ProfileScreen(viewModel = viewModel) }

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
    val viewModel = ProfileViewModel(userRepository)

    // When
    composeTestRule.setContent { ProfileScreen(viewModel = viewModel) }

    // Then
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.PROFILE_SCREEN).assertIsDisplayed()
  }

  @Test
  fun profileScreen_repositorySaveFails_stillExitsEditMode() {
    // Given
    coEvery { userRepository.saveUser(any()) } returns Result.failure(Exception("Save failed"))
    val viewModel = ProfileViewModel(userRepository)
    composeTestRule.setContent { ProfileScreen(viewModel = viewModel) }
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
    userFlow.value = testUser.copy(displayName = "Test 测试 テスト 🎉 العربية")
    val viewModel = ProfileViewModel(userRepository)

    // When
    composeTestRule.setContent { ProfileScreen(viewModel = viewModel) }

    // Then
    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_TEXT)
        .assertTextEquals("Test 测试 テスト 🎉 العربية")
  }

  @Test
  fun profileScreen_editAndCancel_restoresOriginalName() {
    // Given
    val viewModel = ProfileViewModel(userRepository)
    composeTestRule.setContent { ProfileScreen(viewModel = viewModel) }
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
    userFlow.value = null
    val viewModel = ProfileViewModel(userRepository)
    composeTestRule.setContent { ProfileScreen(viewModel = viewModel) }
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.ERROR_TEXT).assertIsDisplayed()

    // When
    userFlow.value = testUser
    composeTestRule.waitForIdle()

    // Then
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_TEXT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.ERROR_TEXT).assertDoesNotExist()
  }

  @Test
  fun profileScreen_userChangesFromValidToNull_showsError() {
    // Given
    val viewModel = ProfileViewModel(userRepository)
    composeTestRule.setContent { ProfileScreen(viewModel = viewModel) }
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_TEXT).assertIsDisplayed()

    // When
    userFlow.value = null
    composeTestRule.waitForIdle()

    // Then
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.ERROR_TEXT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_TEXT).assertDoesNotExist()
  }

  @Test
  fun profileScreen_photoUrlChanges_displaysNewPhoto() {
    // Given
    val viewModel = ProfileViewModel(userRepository)
    composeTestRule.setContent { ProfileScreen(viewModel = viewModel) }

    // When
    userFlow.value = testUser.copy(photoUrl = "https://newphoto.com/image.jpg")
    composeTestRule.waitForIdle()

    // Then
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.PROFILE_PICTURE).assertIsDisplayed()
  }

  @Test
  fun profileScreen_photoUrlChangesToEmpty_showsFallbackIcon() {
    // Given
    val viewModel = ProfileViewModel(userRepository)
    composeTestRule.setContent { ProfileScreen(viewModel = viewModel) }

    // When
    userFlow.value = testUser.copy(photoUrl = "")
    composeTestRule.waitForIdle()

    // Then
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.PROFILE_PICTURE).assertIsDisplayed()
  }

  @Test
  fun profileScreen_editModeActiveWhenUserChanges_handlesGracefully() {
    // Given
    val viewModel = ProfileViewModel(userRepository)
    composeTestRule.setContent { ProfileScreen(viewModel = viewModel) }
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.EDIT_BUTTON).performClick()

    // When
    userFlow.value = testUser.copy(displayName = "New Name From Server")
    composeTestRule.waitForIdle()

    // Then
    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD)
        .assertTextEquals("Display Name", "New Name From Server")
  }
}
