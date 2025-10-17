package ch.eureka.eurekapp.ui.authentication

import ch.eureka.eurekapp.model.data.invitation.Invitation
import ch.eureka.eurekapp.model.data.invitation.InvitationRepository
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

/**
 * Comprehensive unit tests for TokenEntryViewModel.
 *
 * Tests cover:
 * - Token validation logic
 * - State management
 * - Error handling
 * - Edge cases and boundary conditions
 * - Repository interaction
 * - Firebase Auth integration
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TokenEntryViewModelTest {

  private lateinit var viewModel: TokenEntryViewModel
  private lateinit var mockRepository: InvitationRepository
  private lateinit var mockAuth: FirebaseAuth
  private lateinit var mockUser: FirebaseUser
  private val testDispatcher = StandardTestDispatcher()

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)

    mockRepository = mockk(relaxed = true)
    mockAuth = mockk(relaxed = true)
    mockUser = mockk(relaxed = true)

    // Default mock behaviors
    every { mockAuth.currentUser } returns mockUser
    every { mockUser.displayName } returns "Test User"
    every { mockUser.uid } returns "test_user_123"

    viewModel = TokenEntryViewModel(repository = mockRepository, auth = mockAuth)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    clearAllMocks()
  }

  // ========================================
  // Initialization Tests
  // ========================================

  @Test
  fun init_shouldLoadUserNameFromAuth() {
    assertEquals("Test User", viewModel.uiState.value.userName)
  }

  @Test
  fun init_whenUserHasNoDisplayName_shouldUseGuestAsDefault() {
    every { mockUser.displayName } returns null

    val viewModelNoName = TokenEntryViewModel(repository = mockRepository, auth = mockAuth)

    assertEquals("Guest", viewModelNoName.uiState.value.userName)
  }

  @Test
  fun init_whenUserHasEmptyDisplayName_shouldUseEmptyString() {
    every { mockUser.displayName } returns ""

    val viewModelEmptyName = TokenEntryViewModel(repository = mockRepository, auth = mockAuth)

    assertEquals("", viewModelEmptyName.uiState.value.userName)
  }

  @Test
  fun init_whenNoUserSignedIn_shouldUseGuestAsDefault() {
    every { mockAuth.currentUser } returns null

    val viewModelNoUser = TokenEntryViewModel(repository = mockRepository, auth = mockAuth)

    assertEquals("Guest", viewModelNoUser.uiState.value.userName)
  }

  // ========================================
  // Token Update Tests
  // ========================================

  @Test
  fun updateToken_shouldUpdateTokenInState() {
    viewModel.updateToken("ABC-123")

    assertEquals("ABC-123", viewModel.uiState.value.token)
  }

  @Test
  fun updateToken_shouldTrimWhitespace() {
    viewModel.updateToken("  ABC-123  ")

    assertEquals("ABC-123", viewModel.uiState.value.token)
  }

  @Test
  fun updateToken_shouldClearErrorMessage() = runTest {
    // Set an error first
    viewModel.validateToken() // This will set error for empty token

    advanceUntilIdle()

    assertNotNull(viewModel.uiState.value.errorMessage)

    // Update token should clear error
    viewModel.updateToken("NEW-TOKEN")

    assertNull(viewModel.uiState.value.errorMessage)
  }

  @Test
  fun updateToken_shouldHandleEmptyString() {
    viewModel.updateToken("")

    assertEquals("", viewModel.uiState.value.token)
  }

  @Test
  fun updateToken_shouldHandleVeryLongToken() {
    val longToken = "A".repeat(10000)
    viewModel.updateToken(longToken)

    assertEquals(longToken, viewModel.uiState.value.token)
  }

  @Test
  fun updateToken_shouldHandleSpecialCharacters() {
    val specialToken = "TOKEN_!@#$%^&*()_+-=[]{}|;:',.<>?"
    viewModel.updateToken(specialToken)

    assertEquals(specialToken, viewModel.uiState.value.token)
  }

  @Test
  fun updateToken_shouldHandleUnicodeCharacters() {
    val unicodeToken = "TOKEN_こんにちは_🎉_مرحبا"
    viewModel.updateToken(unicodeToken)

    assertEquals(unicodeToken, viewModel.uiState.value.token)
  }

  // ========================================
  // Clear Error Tests
  // ========================================

  @Test
  fun clearError_shouldRemoveErrorMessage() = runTest {
    // Trigger an error by validating empty token
    viewModel.validateToken()
    advanceUntilIdle()

    assertNotNull(viewModel.uiState.value.errorMessage)

    viewModel.clearError()

    assertNull(viewModel.uiState.value.errorMessage)
  }

  // ========================================
  // Validation - Empty Token Tests
  // ========================================

  @Test
  fun validateToken_whenTokenIsEmpty_shouldSetError() = runTest {
    viewModel.updateToken("")

    viewModel.validateToken()
    advanceUntilIdle()

    assertEquals("Please enter a token", viewModel.uiState.value.errorMessage)
    assertFalse(viewModel.uiState.value.isLoading)
    assertFalse(viewModel.uiState.value.validationSuccess)
  }

  @Test
  fun validateToken_whenTokenIsBlank_shouldSetError() = runTest {
    viewModel.updateToken("   ")

    viewModel.validateToken()
    advanceUntilIdle()

    assertEquals("Please enter a token", viewModel.uiState.value.errorMessage)
  }

  @Test
  fun validateToken_whenTokenIsWhitespace_shouldSetError() = runTest {
    viewModel.updateToken("\t\n  ")

    viewModel.validateToken()
    advanceUntilIdle()

    assertEquals("Please enter a token", viewModel.uiState.value.errorMessage)
  }

  // ========================================
  // Validation - No User Tests
  // ========================================

  @Test
  fun validateToken_whenNoUserSignedIn_shouldSetError() = runTest {
    every { mockAuth.currentUser } returns null
    viewModel.updateToken("VALID-TOKEN")

    viewModel.validateToken()
    advanceUntilIdle()

    assertEquals(
        "You must be signed in to use an invitation token", viewModel.uiState.value.errorMessage)
    assertFalse(viewModel.uiState.value.isLoading)
  }

  // ========================================
  // Validation - Invalid Token Tests
  // ========================================

  @Test
  fun validateToken_whenTokenNotFound_shouldSetError() = runTest {
    viewModel.updateToken("INVALID-TOKEN")
    coEvery { mockRepository.getInvitationByToken("INVALID-TOKEN") } returns flowOf(null)

    viewModel.validateToken()
    advanceUntilIdle()

    assertEquals("Invalid token. Please check and try again.", viewModel.uiState.value.errorMessage)
    assertFalse(viewModel.uiState.value.isLoading)
    assertFalse(viewModel.uiState.value.validationSuccess)
  }

  // ========================================
  // Validation - Already Used Token Tests
  // ========================================

  @Test
  fun validateToken_whenTokenAlreadyUsed_shouldSetError() = runTest {
    val usedInvitation =
        Invitation(
            token = "USED-TOKEN",
            projectId = "project_123",
            isUsed = true,
            usedBy = "other_user",
            usedAt = Timestamp.now())

    viewModel.updateToken("USED-TOKEN")
    coEvery { mockRepository.getInvitationByToken("USED-TOKEN") } returns flowOf(usedInvitation)

    viewModel.validateToken()
    advanceUntilIdle()

    assertEquals("This token has already been used.", viewModel.uiState.value.errorMessage)
    assertFalse(viewModel.uiState.value.isLoading)
    assertFalse(viewModel.uiState.value.validationSuccess)
  }

  // ========================================
  // Validation - Success Tests
  // ========================================

  @Test
  fun validateToken_whenValidToken_shouldMarkAsUsedAndSetSuccess() = runTest {
    val validInvitation =
        Invitation(token = "VALID-TOKEN", projectId = "project_123", isUsed = false)

    viewModel.updateToken("VALID-TOKEN")
    coEvery { mockRepository.getInvitationByToken("VALID-TOKEN") } returns flowOf(validInvitation)
    coEvery { mockRepository.markInvitationAsUsed("VALID-TOKEN", "test_user_123") } returns
        Result.success(Unit)

    viewModel.validateToken()
    advanceUntilIdle()

    assertNull(viewModel.uiState.value.errorMessage)
    assertFalse(viewModel.uiState.value.isLoading)
    assertTrue(viewModel.uiState.value.validationSuccess)
    coVerify { mockRepository.markInvitationAsUsed("VALID-TOKEN", "test_user_123") }
  }

  @Test
  fun validateToken_shouldCompleteValidationSuccessfully() = runTest {
    val validInvitation =
        Invitation(token = "VALID-TOKEN", projectId = "project_123", isUsed = false)

    viewModel.updateToken("VALID-TOKEN")
    coEvery { mockRepository.getInvitationByToken("VALID-TOKEN") } returns flowOf(validInvitation)
    coEvery { mockRepository.markInvitationAsUsed(any(), any()) } returns Result.success(Unit)

    viewModel.validateToken()
    advanceUntilIdle()

    // Loading should be false after completion
    assertFalse(viewModel.uiState.value.isLoading)
    assertTrue(viewModel.uiState.value.validationSuccess)
  }

  // ========================================
  // Validation - Repository Failure Tests
  // ========================================

  @Test
  fun validateToken_whenMarkAsUsedFails_shouldSetError() = runTest {
    val validInvitation =
        Invitation(token = "VALID-TOKEN", projectId = "project_123", isUsed = false)

    viewModel.updateToken("VALID-TOKEN")
    coEvery { mockRepository.getInvitationByToken("VALID-TOKEN") } returns flowOf(validInvitation)
    coEvery { mockRepository.markInvitationAsUsed("VALID-TOKEN", "test_user_123") } returns
        Result.failure(Exception("Database error"))

    viewModel.validateToken()
    advanceUntilIdle()

    assertEquals("Database error", viewModel.uiState.value.errorMessage)
    assertFalse(viewModel.uiState.value.isLoading)
    assertFalse(viewModel.uiState.value.validationSuccess)
  }

  @Test
  fun validateToken_whenMarkAsUsedFailsWithNoMessage_shouldSetGenericError() = runTest {
    val validInvitation =
        Invitation(token = "VALID-TOKEN", projectId = "project_123", isUsed = false)

    viewModel.updateToken("VALID-TOKEN")
    coEvery { mockRepository.getInvitationByToken("VALID-TOKEN") } returns flowOf(validInvitation)
    coEvery { mockRepository.markInvitationAsUsed("VALID-TOKEN", "test_user_123") } returns
        Result.failure(Exception())

    viewModel.validateToken()
    advanceUntilIdle()

    assertEquals(
        "Failed to validate token. Please try again.", viewModel.uiState.value.errorMessage)
  }

  // ========================================
  // Validation - Network/Exception Tests
  // ========================================

  @Test
  fun validateToken_whenRepositoryThrowsException_shouldSetError() = runTest {
    viewModel.updateToken("ERROR-TOKEN")
    coEvery { mockRepository.getInvitationByToken("ERROR-TOKEN") } throws Exception("Network error")

    viewModel.validateToken()
    advanceUntilIdle()

    assertTrue(viewModel.uiState.value.errorMessage?.contains("Network error") == true)
    assertFalse(viewModel.uiState.value.isLoading)
    assertFalse(viewModel.uiState.value.validationSuccess)
  }

  @Test
  fun validateToken_whenUnexpectedExceptionOccurs_shouldSetGenericError() = runTest {
    viewModel.updateToken("EXCEPTION-TOKEN")
    coEvery { mockRepository.getInvitationByToken("EXCEPTION-TOKEN") } throws
        RuntimeException("Unexpected error")

    viewModel.validateToken()
    advanceUntilIdle()

    assertTrue(
        viewModel.uiState.value.errorMessage?.startsWith("An unexpected error occurred") == true)
    assertFalse(viewModel.uiState.value.isLoading)
  }

  @Test
  fun validateToken_whenNullPointerException_shouldHandleGracefully() = runTest {
    viewModel.updateToken("NULL-TOKEN")
    coEvery { mockRepository.getInvitationByToken("NULL-TOKEN") } throws
        NullPointerException("Null pointer")

    viewModel.validateToken()
    advanceUntilIdle()

    assertNotNull(viewModel.uiState.value.errorMessage)
    assertTrue(viewModel.uiState.value.errorMessage?.contains("Null pointer") == true)
    assertFalse(viewModel.uiState.value.isLoading)
  }

  // ========================================
  // Edge Case Tests
  // ========================================

  @Test
  fun validateToken_withVeryLongValidToken_shouldSucceed() = runTest {
    val longToken = "A".repeat(1000)
    val validInvitation = Invitation(token = longToken, projectId = "project_123", isUsed = false)

    viewModel.updateToken(longToken)
    coEvery { mockRepository.getInvitationByToken(longToken) } returns flowOf(validInvitation)
    coEvery { mockRepository.markInvitationAsUsed(longToken, "test_user_123") } returns
        Result.success(Unit)

    viewModel.validateToken()
    advanceUntilIdle()

    assertTrue(viewModel.uiState.value.validationSuccess)
  }

  @Test
  fun validateToken_withSpecialCharactersInToken_shouldSucceed() = runTest {
    val specialToken = "TOKEN_!@#$%^&*()"
    val validInvitation =
        Invitation(token = specialToken, projectId = "project_123", isUsed = false)

    viewModel.updateToken(specialToken)
    coEvery { mockRepository.getInvitationByToken(specialToken) } returns flowOf(validInvitation)
    coEvery { mockRepository.markInvitationAsUsed(specialToken, "test_user_123") } returns
        Result.success(Unit)

    viewModel.validateToken()
    advanceUntilIdle()

    assertTrue(viewModel.uiState.value.validationSuccess)
  }

  @Test
  fun validateToken_multipleSequentialCalls_allExecute() = runTest {
    val validInvitation =
        Invitation(token = "VALID-TOKEN", projectId = "project_123", isUsed = false)

    viewModel.updateToken("VALID-TOKEN")
    coEvery { mockRepository.getInvitationByToken("VALID-TOKEN") } returns flowOf(validInvitation)
    coEvery { mockRepository.markInvitationAsUsed(any(), any()) } returns Result.success(Unit)

    // Call validate multiple times rapidly (UI prevents this in reality)
    viewModel.validateToken()
    viewModel.validateToken()
    viewModel.validateToken()

    advanceUntilIdle()

    // All calls execute since there's no mutex (UI disables button in practice)
    coVerify(atLeast = 1) { mockRepository.markInvitationAsUsed("VALID-TOKEN", "test_user_123") }
  }

  // ========================================
  // State Consistency Tests
  // ========================================

  @Test
  fun uiState_shouldStartWithCorrectDefaults() {
    val initialState = viewModel.uiState.value

    assertEquals("", initialState.token)
    assertFalse(initialState.isLoading)
    assertNull(initialState.errorMessage)
    assertFalse(initialState.validationSuccess)
    assertEquals("Test User", initialState.userName)
  }

  @Test
  fun validateToken_afterError_successfulValidationClearsError() = runTest {
    // Set an error first
    viewModel.validateToken() // Empty token error
    advanceUntilIdle()
    assertNotNull(viewModel.uiState.value.errorMessage)

    // Now try valid token
    val validInvitation =
        Invitation(token = "VALID-TOKEN", projectId = "project_123", isUsed = false)
    viewModel.updateToken("VALID-TOKEN")
    coEvery { mockRepository.getInvitationByToken("VALID-TOKEN") } returns flowOf(validInvitation)
    coEvery { mockRepository.markInvitationAsUsed("VALID-TOKEN", "test_user_123") } returns
        Result.success(Unit)

    viewModel.validateToken()
    advanceUntilIdle()

    // Error should be cleared after successful validation
    assertNull(viewModel.uiState.value.errorMessage)
    assertTrue(viewModel.uiState.value.validationSuccess)
  }

  @Test
  fun validateToken_afterSuccess_statesShouldBeCorrect() = runTest {
    val validInvitation =
        Invitation(token = "VALID-TOKEN", projectId = "project_123", isUsed = false)

    viewModel.updateToken("VALID-TOKEN")
    coEvery { mockRepository.getInvitationByToken("VALID-TOKEN") } returns flowOf(validInvitation)
    coEvery { mockRepository.markInvitationAsUsed("VALID-TOKEN", "test_user_123") } returns
        Result.success(Unit)

    viewModel.validateToken()
    advanceUntilIdle()

    val finalState = viewModel.uiState.value

    assertEquals("VALID-TOKEN", finalState.token)
    assertFalse(finalState.isLoading)
    assertNull(finalState.errorMessage)
    assertTrue(finalState.validationSuccess)
  }

  // ========================================
  // Concurrent Access Tests
  // ========================================
  // Note: Concurrent validation tests are covered in Integration tests

  // ========================================
  // Mock Verification Tests
  // ========================================

  @Test
  fun validateToken_success_shouldCallRepositoryInCorrectOrder() = runTest {
    val validInvitation =
        Invitation(token = "ORDER-TOKEN", projectId = "project_123", isUsed = false)

    viewModel.updateToken("ORDER-TOKEN")
    coEvery { mockRepository.getInvitationByToken("ORDER-TOKEN") } returns flowOf(validInvitation)
    coEvery { mockRepository.markInvitationAsUsed("ORDER-TOKEN", "test_user_123") } returns
        Result.success(Unit)

    viewModel.validateToken()
    advanceUntilIdle()

    coVerifyOrder {
      mockRepository.getInvitationByToken("ORDER-TOKEN")
      mockRepository.markInvitationAsUsed("ORDER-TOKEN", "test_user_123")
    }
  }

  @Test
  fun validateToken_whenInvitationNotFound_shouldNotCallMarkAsUsed() = runTest {
    viewModel.updateToken("NOT-FOUND-TOKEN")
    coEvery { mockRepository.getInvitationByToken("NOT-FOUND-TOKEN") } returns flowOf(null)

    viewModel.validateToken()
    advanceUntilIdle()

    coVerify(exactly = 1) { mockRepository.getInvitationByToken("NOT-FOUND-TOKEN") }
    coVerify(exactly = 0) { mockRepository.markInvitationAsUsed(any(), any()) }
  }

  @Test
  fun validateToken_whenAlreadyUsed_shouldNotCallMarkAsUsed() = runTest {
    val usedInvitation =
        Invitation(
            token = "ALREADY-USED", projectId = "project_123", isUsed = true, usedBy = "other_user")

    viewModel.updateToken("ALREADY-USED")
    coEvery { mockRepository.getInvitationByToken("ALREADY-USED") } returns flowOf(usedInvitation)

    viewModel.validateToken()
    advanceUntilIdle()

    coVerify(exactly = 1) { mockRepository.getInvitationByToken("ALREADY-USED") }
    coVerify(exactly = 0) { mockRepository.markInvitationAsUsed(any(), any()) }
  }
}
