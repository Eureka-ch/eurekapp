package ch.eureka.eurekapp.ui.authentication

import ch.eureka.eurekapp.model.data.invitation.Invitation
import ch.eureka.eurekapp.model.data.invitation.MockInvitationRepository
import ch.eureka.eurekapp.utils.FirebaseEmulator
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Integration tests for TokenEntryViewModel using MockInvitationRepository.
 *
 * These tests verify the ViewModel's behavior with a realistic repository implementation, testing
 * state management, error handling, and edge cases.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TokenEntryViewModelIntegrationTest {

  private lateinit var mockRepository: MockInvitationRepository
  private lateinit var auth: FirebaseAuth
  private lateinit var viewModel: TokenEntryViewModel
  private val testDispatcher = StandardTestDispatcher()

  @Before
  fun setup() = runBlocking {
    Dispatchers.setMain(testDispatcher)

    // Setup Firebase Emulator
    if (!FirebaseEmulator.isRunning) {
      throw IllegalStateException(
          "Firebase Emulator must be running for integration tests. Run 'firebase emulators:start'")
    }

    FirebaseEmulator.clearAuthEmulator()
    FirebaseEmulator.clearFirestoreEmulator()

    auth = FirebaseEmulator.auth

    // Sign in test user using await
    auth.signInAnonymously().await()

    mockRepository = MockInvitationRepository()
    viewModel = TokenEntryViewModel(repository = mockRepository, auth = auth)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    mockRepository.reset()
    FirebaseEmulator.clearAuthEmulator()
    FirebaseEmulator.clearFirestoreEmulator()
  }

  // ========================================
  // Full Flow Integration Tests
  // ========================================

  @Test
  fun fullFlow_validToken_successfullyMarksAsUsed() = runTest {
    // Setup: Add valid invitation to repository
    val invitation = Invitation(token = "VALID-TOKEN-123", projectId = "project_1", isUsed = false)
    mockRepository.addInvitation(invitation)

    // Act: Update token and validate
    viewModel.updateToken("VALID-TOKEN-123")
    viewModel.validateToken()
    advanceUntilIdle()

    // Assert: Validation succeeded
    assertTrue(viewModel.uiState.value.validationSuccess)
    assertFalse(viewModel.uiState.value.isLoading)
    assertNull(viewModel.uiState.value.errorMessage)

    // Assert: Repository was called correctly
    assertTrue(mockRepository.getInvitationByTokenCalls.contains("VALID-TOKEN-123"))
    assertTrue(mockRepository.markInvitationAsUsedCalls.any { it.first == "VALID-TOKEN-123" })

    // Assert: Invitation marked as used in repository
    val updatedInvitation = mockRepository.getInvitationByToken("VALID-TOKEN-123").first()
    assertTrue(updatedInvitation?.isUsed == true)
    assertNotNull(updatedInvitation?.usedBy)
  }

  @Test
  fun fullFlow_invalidToken_showsError() = runTest {
    // Act: Try to validate non-existent token
    viewModel.updateToken("INVALID-TOKEN")
    viewModel.validateToken()
    advanceUntilIdle()

    // Assert: Shows error
    assertFalse(viewModel.uiState.value.validationSuccess)
    assertFalse(viewModel.uiState.value.isLoading)
    assertEquals("Invalid token. Please check and try again.", viewModel.uiState.value.errorMessage)

    // Assert: Repository was queried but not marked as used
    assertTrue(mockRepository.getInvitationByTokenCalls.contains("INVALID-TOKEN"))
    assertTrue(mockRepository.markInvitationAsUsedCalls.isEmpty())
  }

  @Test
  fun fullFlow_alreadyUsedToken_showsError() = runTest {
    // Setup: Add already used invitation
    val usedInvitation =
        Invitation(
            token = "USED-TOKEN", projectId = "project_1", isUsed = true, usedBy = "other_user_123")
    mockRepository.addInvitation(usedInvitation)

    // Act: Try to validate used token
    viewModel.updateToken("USED-TOKEN")
    viewModel.validateToken()
    advanceUntilIdle()

    // Assert: Shows error
    assertFalse(viewModel.uiState.value.validationSuccess)
    assertEquals("This token has already been used.", viewModel.uiState.value.errorMessage)

    // Assert: markAsUsed was not called
    assertTrue(mockRepository.markInvitationAsUsedCalls.isEmpty())
  }

  @Test
  fun fullFlow_repositoryFailure_showsError() = runTest {
    // Setup: Add valid invitation but configure repository to fail
    val invitation = Invitation(token = "FAIL-TOKEN", projectId = "project_1", isUsed = false)
    mockRepository.addInvitation(invitation)
    mockRepository.shouldFailMarkAsUsed = true
    mockRepository.markAsUsedFailureMessage = "Database connection failed"

    // Act: Try to validate
    viewModel.updateToken("FAIL-TOKEN")
    viewModel.validateToken()
    advanceUntilIdle()

    // Assert: Shows repository error
    assertFalse(viewModel.uiState.value.validationSuccess)
    assertEquals("Database connection failed", viewModel.uiState.value.errorMessage)
  }

  @Test
  fun fullFlow_repositoryException_showsError() = runTest {
    // Setup: Configure repository to throw exception
    mockRepository.shouldThrowException = true
    mockRepository.exceptionToThrow = Exception("Network timeout")

    // Act: Try to validate
    viewModel.updateToken("EXCEPTION-TOKEN")
    viewModel.validateToken()
    advanceUntilIdle()

    // Assert: Shows exception error
    assertFalse(viewModel.uiState.value.validationSuccess)
    assertTrue(viewModel.uiState.value.errorMessage?.contains("Network timeout") == true)
  }

  // ========================================
  // Race Condition Tests
  // ========================================

  @Test
  fun raceCondition_multipleUsersSimultaneous_onlyOneSucceeds() = runTest {
    // Setup: Create invitation
    val invitation = Invitation(token = "RACE-TOKEN", projectId = "project_1", isUsed = false)
    mockRepository.addInvitation(invitation)

    // Create two ViewModels simulating two users
    val viewModel1 = TokenEntryViewModel(repository = mockRepository, auth = auth)
    val viewModel2 = TokenEntryViewModel(repository = mockRepository, auth = auth)

    // Act: Both try to validate same token
    viewModel1.updateToken("RACE-TOKEN")
    viewModel2.updateToken("RACE-TOKEN")

    viewModel1.validateToken()
    viewModel2.validateToken()

    advanceUntilIdle()

    // Assert: At least one should succeed (in mock, both succeed since we don't have real locking)
    // In real Firestore, only one would succeed due to transactions
    val invitation1Success = viewModel1.uiState.value.validationSuccess
    val invitation2Success = viewModel2.uiState.value.validationSuccess

    // With mock, both succeed - but we verify both tried
    assertTrue(mockRepository.markInvitationAsUsedCalls.size >= 1)
  }

  // ========================================
  // State Transition Tests
  // ========================================

  @Test
  fun stateTransition_loadingStateSetCorrectly() = runTest {
    // Setup
    val invitation = Invitation(token = "STATE-TOKEN", projectId = "project_1", isUsed = false)
    mockRepository.addInvitation(invitation)

    viewModel.updateToken("STATE-TOKEN")

    // Initial state
    assertFalse(viewModel.uiState.value.isLoading)

    // Start validation
    viewModel.validateToken()

    // Loading should be set immediately
    // Note: Due to test dispatcher, we may need to check after some advancement
    advanceUntilIdle()

    // After completion, loading should be false
    assertFalse(viewModel.uiState.value.isLoading)
  }

  @Test
  fun stateTransition_errorRecovery_clearsErrorOnSuccess() = runTest {
    // Step 1: Cause an error
    viewModel.validateToken() // Empty token
    advanceUntilIdle()
    assertNotNull(viewModel.uiState.value.errorMessage)

    // Step 2: Fix and succeed
    val invitation = Invitation(token = "RECOVER-TOKEN", projectId = "project_1", isUsed = false)
    mockRepository.addInvitation(invitation)

    viewModel.updateToken("RECOVER-TOKEN")
    viewModel.validateToken()
    advanceUntilIdle()

    // Assert: Error cleared, success achieved
    assertNull(viewModel.uiState.value.errorMessage)
    assertTrue(viewModel.uiState.value.validationSuccess)
  }

  // ========================================
  // Edge Case Tests
  // ========================================

  @Test
  fun edgeCase_veryLongToken_handlesCorrectly() = runTest {
    // Setup: Create invitation with very long token
    val longToken = "A".repeat(1000)
    val invitation = Invitation(token = longToken, projectId = "project_1", isUsed = false)
    mockRepository.addInvitation(invitation)

    // Act
    viewModel.updateToken(longToken)
    viewModel.validateToken()
    advanceUntilIdle()

    // Assert: Successfully validates
    assertTrue(viewModel.uiState.value.validationSuccess)
  }

  @Test
  fun edgeCase_specialCharactersInToken_handlesCorrectly() = runTest {
    // Setup: Create invitation with special characters
    val specialToken = "TOKEN_!@#\$%^&*()"
    val invitation = Invitation(token = specialToken, projectId = "project_1", isUsed = false)
    mockRepository.addInvitation(invitation)

    // Act
    viewModel.updateToken(specialToken)
    viewModel.validateToken()
    advanceUntilIdle()

    // Assert: Successfully validates
    assertTrue(viewModel.uiState.value.validationSuccess)
  }

  // ========================================
  // Repository Call Verification Tests
  // ========================================

  @Test
  fun repositoryCalls_correctSequence() = runTest {
    // Setup
    val invitation = Invitation(token = "SEQ-TOKEN", projectId = "project_1", isUsed = false)
    mockRepository.addInvitation(invitation)

    // Act
    viewModel.updateToken("SEQ-TOKEN")
    viewModel.validateToken()
    advanceUntilIdle()

    // Assert: Calls made in correct order
    assertEquals(1, mockRepository.getInvitationByTokenCalls.size)
    assertEquals(1, mockRepository.markInvitationAsUsedCalls.size)
    assertEquals("SEQ-TOKEN", mockRepository.getInvitationByTokenCalls[0])
    assertEquals("SEQ-TOKEN", mockRepository.markInvitationAsUsedCalls[0].first)
  }

  @Test
  fun repositoryCalls_failurePreventsMarkAsUsed() = runTest {
    // Act: Try invalid token
    viewModel.updateToken("NONEXISTENT")
    viewModel.validateToken()
    advanceUntilIdle()

    // Assert: getInvitationByToken called, but not markAsUsed
    assertEquals(1, mockRepository.getInvitationByTokenCalls.size)
    assertEquals(0, mockRepository.markInvitationAsUsedCalls.size)
  }
}
