// Co-authored by Claude Code
package ch.eureka.eurekapp.ui.mcp

import ch.eureka.eurekapp.model.data.mcp.McpToken
import ch.eureka.eurekapp.model.data.mcp.McpTokenRepository
import com.google.firebase.Timestamp
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.util.Date
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class McpTokenViewModelTest {

  private lateinit var viewModel: McpTokenViewModel
  private lateinit var repository: McpTokenRepository
  private lateinit var testDispatcher: TestDispatcher

  private val now = Timestamp.now()
  private val expiresAt = Timestamp(Date(now.toDate().time + 30 * 24 * 60 * 60 * 1000L))

  private val testTokens =
      listOf(
          McpToken(
              tokenId = "token-1", name = "Test Token 1", createdAt = now, expiresAt = expiresAt),
          McpToken(
              tokenId = "token-2", name = "Test Token 2", createdAt = now, expiresAt = expiresAt))

  @Before
  fun setup() {
    testDispatcher = StandardTestDispatcher()
    Dispatchers.setMain(testDispatcher)

    repository = mockk(relaxed = true)
    coEvery { repository.listTokens() } returns Result.success(testTokens)

    viewModel = McpTokenViewModel(repository)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    clearAllMocks()
  }

  @Test
  fun mcpTokenViewModel_initialStateHasCorrectDefaults() {
    val state = viewModel.uiState.value

    assertTrue(state.tokens.isEmpty())
    assertTrue(state.isLoading)
    assertNull(state.error)
    assertNull(state.newlyCreatedToken)
  }

  @Test
  fun loadTokens_fetchesTokensSuccessfully() = runTest {
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertEquals(2, state.tokens.size)
    assertEquals("token-1", state.tokens[0].tokenId)
    assertEquals("Test Token 1", state.tokens[0].name)
  }

  @Test
  fun loadTokens_handlesRepositoryErrors() = runTest {
    coEvery { repository.listTokens() } returns Result.failure(Exception("Network error"))

    viewModel.loadTokens()
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals("Network error", state.error)
    assertFalse(state.isLoading)
  }

  @Test
  fun createToken_createsTokenAndRefreshesList() = runTest {
    val newToken = McpToken(tokenId = "new-token-123", name = "New Token", expiresAt = expiresAt)
    coEvery { repository.createToken("New Token", 30) } returns Result.success(newToken)
    coEvery { repository.listTokens() } returns Result.success(testTokens + newToken)

    advanceUntilIdle()

    viewModel.createToken("New Token", 30)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals("new-token-123", state.newlyCreatedToken)
    assertFalse(state.isLoading)
    coVerify { repository.listTokens() }
  }

  @Test
  fun createToken_handlesErrors() = runTest {
    coEvery { repository.createToken(any(), any()) } returns
        Result.failure(Exception("Creation failed"))

    advanceUntilIdle()

    viewModel.createToken("Test Token")
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals("Creation failed", state.error)
    assertNull(state.newlyCreatedToken)
    assertFalse(state.isLoading)
  }

  @Test
  fun createToken_usesDefaultNameWhenBlank() = runTest {
    val newToken = McpToken(tokenId = "new-token-123", name = "MCP Token", expiresAt = expiresAt)
    coEvery { repository.createToken("MCP Token", 30) } returns Result.success(newToken)

    advanceUntilIdle()

    viewModel.createToken("MCP Token")
    advanceUntilIdle()

    coVerify { repository.createToken("MCP Token", 30) }
  }

  @Test
  fun revokeToken_deletesTokenAndRefreshesList() = runTest {
    coEvery { repository.revokeToken("token-1") } returns Result.success(Unit)
    coEvery { repository.listTokens() } returns Result.success(listOf(testTokens[1]))

    advanceUntilIdle()

    viewModel.revokeToken("token-1")
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    coVerify { repository.revokeToken("token-1") }
    coVerify(atLeast = 2) { repository.listTokens() }
  }

  @Test
  fun revokeToken_handlesErrors() = runTest {
    coEvery { repository.revokeToken(any()) } returns Result.failure(Exception("Revoke failed"))

    advanceUntilIdle()

    viewModel.revokeToken("token-1")
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals("Revoke failed", state.error)
    assertFalse(state.isLoading)
  }

  @Test
  fun clearNewlyCreatedToken_clearsTheToken() = runTest {
    val newToken = McpToken(tokenId = "new-token-123", name = "Test")
    coEvery { repository.createToken(any(), any()) } returns Result.success(newToken)

    advanceUntilIdle()

    viewModel.createToken("Test")
    advanceUntilIdle()
    assertEquals("new-token-123", viewModel.uiState.value.newlyCreatedToken)

    viewModel.clearNewlyCreatedToken()
    assertNull(viewModel.uiState.value.newlyCreatedToken)
  }

  @Test
  fun clearError_clearsTheErrorState() = runTest {
    coEvery { repository.listTokens() } returns Result.failure(Exception("Test error"))

    viewModel.loadTokens()
    advanceUntilIdle()
    assertEquals("Test error", viewModel.uiState.value.error)

    viewModel.clearError()
    assertNull(viewModel.uiState.value.error)
  }

  @Test
  fun loadTokens_isCalledOnInit() = runTest {
    advanceUntilIdle()

    coVerify { repository.listTokens() }
  }
}
