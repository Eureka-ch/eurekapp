package ch.eureka.eurekapp.ui.authentication

import android.content.Context
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import ch.eureka.eurekapp.model.authentication.AuthRepository
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.model.data.user.UserRepository
import com.google.firebase.Timestamp
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
class SignInViewModelTest {

  private lateinit var authRepository: AuthRepository
  private lateinit var userRepository: UserRepository
  private lateinit var credentialManager: CredentialManager
  private lateinit var context: Context
  private lateinit var viewModel: SignInViewModel
  private val testDispatcher = StandardTestDispatcher()

  private val testUser =
      User(
          uid = "test-uid",
          displayName = "Test User",
          email = "test@example.com",
          photoUrl = "https://exampleeeee.com/photo.jpg",
          lastActive = Timestamp(1000, 0))

  private lateinit var mockFirebaseUser: FirebaseUser

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)

    authRepository = mockk(relaxed = true)
    userRepository = mockk(relaxed = true)
    credentialManager = mockk(relaxed = true)
    context = mockk(relaxed = true)

    mockFirebaseUser = mockk(relaxed = true)
    val mockPhotoUrl = mockk<android.net.Uri>(relaxed = true)
    every { mockPhotoUrl.toString() } returns "https://example.com/photo.jpg"

    every { mockFirebaseUser.uid } returns "test-uid"
    every { mockFirebaseUser.displayName } returns "Test User"
    every { mockFirebaseUser.email } returns "test@example.com"
    every { mockFirebaseUser.photoUrl } returns mockPhotoUrl

    every { context.getString(any()) } returns "mock-web-client-id"

    viewModel = SignInViewModel(authRepository, userRepository)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    unmockkAll()
  }

  @Test
  fun `signIn with new user creates user in database`() = runTest {
    // Given
    val credential = mockk<Credential>()
    val credentialResponse = mockk<GetCredentialResponse>()
    every { credentialResponse.credential } returns credential

    coEvery { credentialManager.getCredential(any(), any<GetCredentialRequest>()) } returns
        credentialResponse
    coEvery { authRepository.signInWithGoogle(credential) } returns Result.success(mockFirebaseUser)
    every { userRepository.getUserById("test-uid") } returns flowOf(null)
    coEvery { userRepository.saveUser(any()) } returns Result.success(Unit)

    // When
    viewModel.signIn(context, credentialManager)
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    coVerify {
      userRepository.saveUser(
          match {
            it.uid == "test-uid" &&
                it.displayName == "Test User" &&
                it.email == "test@example.com" &&
                it.photoUrl == "https://example.com/photo.jpg"
          })
    }
  }

  @Test
  fun `signIn with existing user updates photoUrl and lastActive`() = runTest {
    // Given
    val credential = mockk<Credential>()
    val credentialResponse = mockk<GetCredentialResponse>()
    every { credentialResponse.credential } returns credential

    val existingUser = testUser.copy(photoUrl = "old-photo.jpg")
    val newPhotoUrl = "https://example.com/new-photo.jpg"

    val mockNewPhotoUrl = mockk<android.net.Uri>(relaxed = true)
    every { mockNewPhotoUrl.toString() } returns newPhotoUrl
    every { mockFirebaseUser.photoUrl } returns mockNewPhotoUrl

    coEvery { credentialManager.getCredential(any(), any<GetCredentialRequest>()) } returns
        credentialResponse
    coEvery { authRepository.signInWithGoogle(credential) } returns Result.success(mockFirebaseUser)
    every { userRepository.getUserById("test-uid") } returns flowOf(existingUser)
    coEvery { userRepository.saveUser(any()) } returns Result.success(Unit)

    // When
    viewModel.signIn(context, credentialManager)
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    coVerify {
      userRepository.saveUser(
          match {
            it.uid == existingUser.uid &&
                it.displayName == existingUser.displayName &&
                it.email == existingUser.email &&
                it.photoUrl == newPhotoUrl
          })
    }
  }

  @Test
  fun `signIn with existing user preserves displayName and email`() = runTest {
    // Given
    val credential = mockk<Credential>()
    val credentialResponse = mockk<GetCredentialResponse>()
    every { credentialResponse.credential } returns credential

    coEvery { credentialManager.getCredential(any(), any<GetCredentialRequest>()) } returns
        credentialResponse
    coEvery { authRepository.signInWithGoogle(credential) } returns Result.success(mockFirebaseUser)
    every { userRepository.getUserById("test-uid") } returns flowOf(testUser)
    coEvery { userRepository.saveUser(any()) } returns Result.success(Unit)

    // When
    viewModel.signIn(context, credentialManager)
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    coVerify {
      userRepository.saveUser(
          match { it.displayName == testUser.displayName && it.email == testUser.email })
    }
  }

  @Test
  fun `signIn handles user save failure`() = runTest {
    // Given
    val credential = mockk<Credential>()
    val credentialResponse = mockk<GetCredentialResponse>()
    every { credentialResponse.credential } returns credential

    coEvery { credentialManager.getCredential(any(), any<GetCredentialRequest>()) } returns
        credentialResponse
    coEvery { authRepository.signInWithGoogle(credential) } returns Result.success(mockFirebaseUser)
    every { userRepository.getUserById("test-uid") } returns flowOf(null)
    coEvery { userRepository.saveUser(any()) } returns Result.failure(Exception("Database error"))

    // When
    viewModel.signIn(context, credentialManager)
    testDispatcher.scheduler.advanceUntilIdle()

    // Then - verify saveUser was called and failed
    coVerify { userRepository.saveUser(any()) }
  }

  @Test
  fun `signIn with null Firebase photoUrl uses empty string for new user`() = runTest {
    // Given
    val credential = mockk<Credential>()
    val credentialResponse = mockk<GetCredentialResponse>()
    every { credentialResponse.credential } returns credential
    every { mockFirebaseUser.photoUrl } returns null

    coEvery { credentialManager.getCredential(any(), any<GetCredentialRequest>()) } returns
        credentialResponse
    coEvery { authRepository.signInWithGoogle(credential) } returns Result.success(mockFirebaseUser)
    every { userRepository.getUserById("test-uid") } returns flowOf(null)
    coEvery { userRepository.saveUser(any()) } returns Result.success(Unit)

    // When
    viewModel.signIn(context, credentialManager)
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    coVerify { userRepository.saveUser(match { it.photoUrl == "" }) }
  }

  @Test
  fun `signIn with null Firebase photoUrl preserves existing photoUrl for existing user`() =
      runTest {
        // Given
        val credential = mockk<Credential>()
        val credentialResponse = mockk<GetCredentialResponse>()
        every { credentialResponse.credential } returns credential
        every { mockFirebaseUser.photoUrl } returns null

        coEvery { credentialManager.getCredential(any(), any<GetCredentialRequest>()) } returns
            credentialResponse
        coEvery { authRepository.signInWithGoogle(credential) } returns
            Result.success(mockFirebaseUser)
        every { userRepository.getUserById("test-uid") } returns flowOf(testUser)
        coEvery { userRepository.saveUser(any()) } returns Result.success(Unit)

        // When
        viewModel.signIn(context, credentialManager)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { userRepository.saveUser(match { it.photoUrl == testUser.photoUrl }) }
      }

  @Test
  fun `signIn with null displayName uses empty string`() = runTest {
    // Given
    val credential = mockk<Credential>()
    val credentialResponse = mockk<GetCredentialResponse>()
    every { credentialResponse.credential } returns credential
    every { mockFirebaseUser.displayName } returns null

    coEvery { credentialManager.getCredential(any(), any<GetCredentialRequest>()) } returns
        credentialResponse
    coEvery { authRepository.signInWithGoogle(credential) } returns Result.success(mockFirebaseUser)
    every { userRepository.getUserById("test-uid") } returns flowOf(null)
    coEvery { userRepository.saveUser(any()) } returns Result.success(Unit)

    // When
    viewModel.signIn(context, credentialManager)
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    coVerify { userRepository.saveUser(match { it.displayName == "" }) }
  }

  @Test
  fun `signIn with null email uses empty string`() = runTest {
    // Given
    val credential = mockk<Credential>()
    val credentialResponse = mockk<GetCredentialResponse>()
    every { credentialResponse.credential } returns credential
    every { mockFirebaseUser.email } returns null

    coEvery { credentialManager.getCredential(any(), any<GetCredentialRequest>()) } returns
        credentialResponse
    coEvery { authRepository.signInWithGoogle(credential) } returns Result.success(mockFirebaseUser)
    every { userRepository.getUserById("test-uid") } returns flowOf(null)
    coEvery { userRepository.saveUser(any()) } returns Result.success(Unit)

    // When
    viewModel.signIn(context, credentialManager)
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    coVerify { userRepository.saveUser(match { it.email == "" }) }
  }

  @Test
  fun `signIn updates lastActive timestamp for existing user`() = runTest {
    // Given
    val credential = mockk<Credential>()
    val credentialResponse = mockk<GetCredentialResponse>()
    every { credentialResponse.credential } returns credential

    val oldTimestamp = Timestamp(1000, 0)
    val existingUser = testUser.copy(lastActive = oldTimestamp)

    coEvery { credentialManager.getCredential(any(), any<GetCredentialRequest>()) } returns
        credentialResponse
    coEvery { authRepository.signInWithGoogle(credential) } returns Result.success(mockFirebaseUser)
    every { userRepository.getUserById("test-uid") } returns flowOf(existingUser)
    coEvery { userRepository.saveUser(any()) } returns Result.success(Unit)

    // When
    viewModel.signIn(context, credentialManager)
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    coVerify { userRepository.saveUser(match { it.lastActive.seconds > oldTimestamp.seconds }) }
  }
}
