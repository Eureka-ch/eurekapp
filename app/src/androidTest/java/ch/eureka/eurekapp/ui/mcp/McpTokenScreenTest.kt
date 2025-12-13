// Co-authored by Claude Code
package ch.eureka.eurekapp.ui.mcp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import ch.eureka.eurekapp.model.data.mcp.McpToken
import ch.eureka.eurekapp.model.data.mcp.McpTokenRepository
import com.google.firebase.Timestamp
import java.util.Date
import kotlinx.coroutines.delay
import org.junit.Rule
import org.junit.Test

class McpTokenScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val testTokens =
      listOf(
          McpToken(
              tokenId = "token-123-full-id",
              name = "Test Token",
              createdAt = Timestamp(Date(1704067200000L)),
              expiresAt = Timestamp(Date(1706745600000L))),
          McpToken(
              tokenId = "token-456-full-id",
              name = "Another Token",
              createdAt = Timestamp(Date(1704153600000L)),
              expiresAt = Timestamp(Date(1706832000000L))))

  private class FakeRepository(
      private var tokens: List<McpToken> = emptyList(),
      private var listTokensError: Exception? = null,
      private var createTokenResult: McpToken? = null,
      private var createTokenError: Exception? = null,
      private var shouldDelayListTokens: Boolean = false
  ) : McpTokenRepository {
    var createTokenCalled = false
    var revokeTokenCalled = false
    var listTokensCalls = 0

    override suspend fun listTokens(): Result<List<McpToken>> {
      listTokensCalls++
      if (shouldDelayListTokens) {
        delay(5000)
      }
      return listTokensError?.let { Result.failure(it) } ?: Result.success(tokens)
    }

    override suspend fun createToken(name: String, ttlDays: Int): Result<McpToken> {
      createTokenCalled = true
      return createTokenError?.let { Result.failure(it) }
          ?: Result.success(createTokenResult ?: McpToken(tokenId = "new-token", name = name))
    }

    override suspend fun revokeToken(tokenId: String): Result<Unit> {
      revokeTokenCalled = true
      return Result.success(Unit)
    }

    fun setTokens(newTokens: List<McpToken>) {
      tokens = newTokens
    }

    fun setError(error: Exception?) {
      listTokensError = error
    }
  }

  @Test
  fun mcpTokenScreen_displaysEmptyStateWhenNoTokens() {
    val repository = FakeRepository(tokens = emptyList())
    val viewModel = McpTokenViewModel(repository)
    composeTestRule.setContent { McpTokenScreen(viewModel = viewModel, onNavigateBack = {}) }

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(McpTokenScreenTestTags.EMPTY_STATE).assertIsDisplayed()
    composeTestRule.onNodeWithText("No MCP tokens yet").assertIsDisplayed()
  }

  @Test
  fun mcpTokenScreen_displaysTokenListWhenTokensExist() {
    val repository = FakeRepository(tokens = testTokens)
    val viewModel = McpTokenViewModel(repository)
    composeTestRule.setContent { McpTokenScreen(viewModel = viewModel, onNavigateBack = {}) }

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(McpTokenScreenTestTags.TOKEN_LIST).assertIsDisplayed()
    composeTestRule.onNodeWithText("Test Token").assertIsDisplayed()
    composeTestRule.onNodeWithText("Another Token").assertIsDisplayed()
  }

  @Test
  fun mcpTokenScreen_displaysLoadingIndicatorWhenLoading() {
    val repository = FakeRepository(tokens = emptyList(), shouldDelayListTokens = true)
    val viewModel = McpTokenViewModel(repository)
    composeTestRule.setContent { McpTokenScreen(viewModel = viewModel, onNavigateBack = {}) }

    composeTestRule.onNodeWithTag(McpTokenScreenTestTags.LOADING_INDICATOR).assertIsDisplayed()
  }

  @Test
  fun mcpTokenScreen_displaysErrorWhenLoadingFails() {
    val repository = FakeRepository(listTokensError = Exception("Network error"))
    val viewModel = McpTokenViewModel(repository)
    composeTestRule.setContent { McpTokenScreen(viewModel = viewModel, onNavigateBack = {}) }

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(McpTokenScreenTestTags.ERROR_TEXT).assertIsDisplayed()
    composeTestRule.onNodeWithText("Network error").assertIsDisplayed()
  }

  @Test
  fun mcpTokenScreen_createButtonOpensDialog() {
    val repository = FakeRepository(tokens = emptyList())
    val viewModel = McpTokenViewModel(repository)
    composeTestRule.setContent { McpTokenScreen(viewModel = viewModel, onNavigateBack = {}) }

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(McpTokenScreenTestTags.CREATE_BUTTON).performClick()
    composeTestRule.onNodeWithTag(McpTokenScreenTestTags.CREATE_DIALOG).assertIsDisplayed()
  }

  @Test
  fun mcpTokenScreen_createDialogCancelClosesDialog() {
    val repository = FakeRepository(tokens = emptyList())
    val viewModel = McpTokenViewModel(repository)
    composeTestRule.setContent { McpTokenScreen(viewModel = viewModel, onNavigateBack = {}) }

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(McpTokenScreenTestTags.CREATE_BUTTON).performClick()
    composeTestRule.onNodeWithTag(McpTokenScreenTestTags.CREATE_DIALOG).assertIsDisplayed()

    composeTestRule.onNodeWithTag(McpTokenScreenTestTags.CANCEL_CREATE).performClick()
    composeTestRule.onNodeWithTag(McpTokenScreenTestTags.CREATE_DIALOG).assertIsNotDisplayed()
  }

  @Test
  fun mcpTokenScreen_createDialogConfirmCreatesToken() {
    val repository =
        FakeRepository(
            tokens = emptyList(),
            createTokenResult = McpToken(tokenId = "new-token", name = "New Token"))
    val viewModel = McpTokenViewModel(repository)
    composeTestRule.setContent { McpTokenScreen(viewModel = viewModel, onNavigateBack = {}) }

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(McpTokenScreenTestTags.CREATE_BUTTON).performClick()
    composeTestRule
        .onNodeWithTag(McpTokenScreenTestTags.TOKEN_NAME_FIELD)
        .performTextInput("My New Token")
    composeTestRule.onNodeWithTag(McpTokenScreenTestTags.CONFIRM_CREATE).performClick()

    composeTestRule.waitForIdle()

    assert(repository.createTokenCalled)
  }

  @Test
  fun mcpTokenScreen_tokenCardDisplaysCopyAndDeleteButtons() {
    val repository = FakeRepository(tokens = testTokens)
    val viewModel = McpTokenViewModel(repository)
    composeTestRule.setContent { McpTokenScreen(viewModel = viewModel, onNavigateBack = {}) }

    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag("${McpTokenScreenTestTags.COPY_BUTTON}_token-123-full-id")
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("${McpTokenScreenTestTags.DELETE_BUTTON}_token-123-full-id")
        .assertIsDisplayed()
  }

  @Test
  fun mcpTokenScreen_deleteButtonShowsConfirmationDialog() {
    val repository = FakeRepository(tokens = testTokens)
    val viewModel = McpTokenViewModel(repository)
    composeTestRule.setContent { McpTokenScreen(viewModel = viewModel, onNavigateBack = {}) }

    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag("${McpTokenScreenTestTags.DELETE_BUTTON}_token-123-full-id")
        .performClick()

    composeTestRule.onNodeWithText("Delete Token").assertIsDisplayed()
    composeTestRule
        .onNodeWithText("Are you sure you want to delete this token?", substring = true)
        .assertIsDisplayed()
  }

  @Test
  fun mcpTokenScreen_backButtonCallsOnNavigateBack() {
    val repository = FakeRepository(tokens = emptyList())
    var backClicked = false
    val viewModel = McpTokenViewModel(repository)
    composeTestRule.setContent {
      McpTokenScreen(viewModel = viewModel, onNavigateBack = { backClicked = true })
    }

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(McpTokenScreenTestTags.BACK_BUTTON).performClick()
    assert(backClicked)
  }

  @Test
  fun mcpTokenScreen_tokenCreatedDialogIsDisplayedWhenTokenCreated() {
    val repository =
        FakeRepository(
            tokens = emptyList(),
            createTokenResult = McpToken(tokenId = "new-secret-token-value", name = "New Token"))
    val viewModel = McpTokenViewModel(repository)
    composeTestRule.setContent { McpTokenScreen(viewModel = viewModel, onNavigateBack = {}) }

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(McpTokenScreenTestTags.CREATE_BUTTON).performClick()
    composeTestRule.onNodeWithTag(McpTokenScreenTestTags.CONFIRM_CREATE).performClick()

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(McpTokenScreenTestTags.TOKEN_CREATED_DIALOG).assertIsDisplayed()
    composeTestRule.onNodeWithText("Token Created").assertIsDisplayed()
  }

  @Test
  fun mcpTokenScreen_retryButtonReloadsTokensWhenErrorOccurs() {
    val repository = FakeRepository(listTokensError = Exception("Network error"))
    val viewModel = McpTokenViewModel(repository)
    composeTestRule.setContent { McpTokenScreen(viewModel = viewModel, onNavigateBack = {}) }

    composeTestRule.waitForIdle()

    repository.setError(null)
    repository.setTokens(testTokens)

    composeTestRule.onNodeWithText("Retry").performClick()

    composeTestRule.waitForIdle()

    assert(repository.listTokensCalls >= 2)
  }

  @Test
  fun mcpTokenScreen_tokenCardDisplaysTokenInfo() {
    val repository = FakeRepository(tokens = testTokens)
    val viewModel = McpTokenViewModel(repository)
    composeTestRule.setContent { McpTokenScreen(viewModel = viewModel, onNavigateBack = {}) }

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Test Token").assertIsDisplayed()
    composeTestRule.onNodeWithText("ID: token-123-full", substring = true).assertIsDisplayed()
  }

  @Test
  fun mcpTokenScreen_tokenCardDisplaysExpiredStatusWhenExpired() {
    val expiredToken =
        McpToken(
            tokenId = "expired-token",
            name = "Expired Token",
            createdAt = Timestamp(Date(1704067200000L)),
            expiresAt = Timestamp(Date(1706745600000L)))

    val repository = FakeRepository(tokens = listOf(expiredToken))
    val viewModel = McpTokenViewModel(repository)
    composeTestRule.setContent { McpTokenScreen(viewModel = viewModel, onNavigateBack = {}) }

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Expired:", substring = true).assertIsDisplayed()
  }

  @Test
  fun mcpTokenScreen_displaysTopBar() {
    val repository = FakeRepository(tokens = emptyList())
    val viewModel = McpTokenViewModel(repository)
    composeTestRule.setContent { McpTokenScreen(viewModel = viewModel, onNavigateBack = {}) }

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("MCP Tokens").assertIsDisplayed()
  }
}
