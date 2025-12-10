package ch.eureka.eurekapp.ui.mcp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodes
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import ch.eureka.eurekapp.model.data.mcp.McpToken
import ch.eureka.eurekapp.model.data.mcp.McpTokenRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.Instant
import org.junit.Rule
import org.junit.Test

class McpTokenScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private fun waitUntilNotLoading() {
    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule
          .onAllNodes(hasTestTag(McpTokenScreenTestTags.LOADING_INDICATOR))
          .fetchSemanticsNodes()
          .isEmpty()
    }
  }

  private val mockRepository = mockk<McpTokenRepository>(relaxed = true)

  private val testTokens =
      listOf(
          McpToken(
              tokenId = "token-123-full-id",
              name = "Test Token",
              createdAt = Instant.parse("2025-01-01T00:00:00Z"),
              expiresAt = Instant.parse("2025-02-01T00:00:00Z")),
          McpToken(
              tokenId = "token-456-full-id",
              name = "Another Token",
              createdAt = Instant.parse("2025-01-02T00:00:00Z"),
              expiresAt = Instant.parse("2025-02-02T00:00:00Z")))

  @Test
  fun mcpTokenScreen_displaysEmptyStateWhenNoTokens() {
    coEvery { mockRepository.listTokens() } returns Result.success(emptyList())

    val viewModel = McpTokenViewModel(mockRepository)
    composeTestRule.setContent { McpTokenScreen(viewModel = viewModel, onNavigateBack = {}) }

    waitUntilNotLoading()

    composeTestRule.onNodeWithTag(McpTokenScreenTestTags.EMPTY_STATE).assertIsDisplayed()
    composeTestRule.onNodeWithText("No MCP tokens yet").assertIsDisplayed()
  }

  @Test
  fun mcpTokenScreen_displaysTokenListWhenTokensExist() {
    coEvery { mockRepository.listTokens() } returns Result.success(testTokens)

    val viewModel = McpTokenViewModel(mockRepository)
    composeTestRule.setContent { McpTokenScreen(viewModel = viewModel, onNavigateBack = {}) }

    waitUntilNotLoading()

    composeTestRule.onNodeWithTag(McpTokenScreenTestTags.TOKEN_LIST).assertIsDisplayed()
    composeTestRule.onNodeWithText("Test Token").assertIsDisplayed()
    composeTestRule.onNodeWithText("Another Token").assertIsDisplayed()
  }

  @Test
  fun mcpTokenScreen_displaysLoadingIndicatorWhenLoading() {
    coEvery { mockRepository.listTokens() } coAnswers
        {
          kotlinx.coroutines.delay(5000)
          Result.success(emptyList())
        }

    val viewModel = McpTokenViewModel(mockRepository)
    composeTestRule.setContent { McpTokenScreen(viewModel = viewModel, onNavigateBack = {}) }

    composeTestRule.onNodeWithTag(McpTokenScreenTestTags.LOADING_INDICATOR).assertIsDisplayed()
  }

  @Test
  fun mcpTokenScreen_displaysErrorWhenLoadingFails() {
    coEvery { mockRepository.listTokens() } returns Result.failure(Exception("Network error"))

    val viewModel = McpTokenViewModel(mockRepository)
    composeTestRule.setContent { McpTokenScreen(viewModel = viewModel, onNavigateBack = {}) }

    waitUntilNotLoading()

    composeTestRule.onNodeWithTag(McpTokenScreenTestTags.ERROR_TEXT).assertIsDisplayed()
    composeTestRule.onNodeWithText("Network error").assertIsDisplayed()
  }

  @Test
  fun mcpTokenScreen_createButtonOpensDialog() {
    coEvery { mockRepository.listTokens() } returns Result.success(emptyList())

    val viewModel = McpTokenViewModel(mockRepository)
    composeTestRule.setContent { McpTokenScreen(viewModel = viewModel, onNavigateBack = {}) }

    waitUntilNotLoading()

    composeTestRule.onNodeWithTag(McpTokenScreenTestTags.CREATE_BUTTON).performClick()
    composeTestRule.onNodeWithTag(McpTokenScreenTestTags.CREATE_DIALOG).assertIsDisplayed()
  }

  @Test
  fun mcpTokenScreen_createDialogCancelClosesDialog() {
    coEvery { mockRepository.listTokens() } returns Result.success(emptyList())

    val viewModel = McpTokenViewModel(mockRepository)
    composeTestRule.setContent { McpTokenScreen(viewModel = viewModel, onNavigateBack = {}) }

    waitUntilNotLoading()

    composeTestRule.onNodeWithTag(McpTokenScreenTestTags.CREATE_BUTTON).performClick()
    composeTestRule.onNodeWithTag(McpTokenScreenTestTags.CREATE_DIALOG).assertIsDisplayed()

    composeTestRule.onNodeWithTag(McpTokenScreenTestTags.CANCEL_CREATE).performClick()
    composeTestRule.onNodeWithTag(McpTokenScreenTestTags.CREATE_DIALOG).assertIsNotDisplayed()
  }

  @Test
  fun mcpTokenScreen_createDialogConfirmCreatesToken() {
    coEvery { mockRepository.listTokens() } returns Result.success(emptyList())
    coEvery { mockRepository.createToken(any(), any()) } returns
        Result.success(McpToken(tokenId = "new-token", name = "New Token"))

    val viewModel = McpTokenViewModel(mockRepository)
    composeTestRule.setContent { McpTokenScreen(viewModel = viewModel, onNavigateBack = {}) }

    waitUntilNotLoading()

    composeTestRule.onNodeWithTag(McpTokenScreenTestTags.CREATE_BUTTON).performClick()
    composeTestRule
        .onNodeWithTag(McpTokenScreenTestTags.TOKEN_NAME_FIELD)
        .performTextInput("My New Token")
    composeTestRule.onNodeWithTag(McpTokenScreenTestTags.CONFIRM_CREATE).performClick()

    waitUntilNotLoading()

    coVerify { mockRepository.createToken(any(), any()) }
  }

  @Test
  fun mcpTokenScreen_tokenCardDisplaysCopyAndDeleteButtons() {
    coEvery { mockRepository.listTokens() } returns Result.success(testTokens)

    val viewModel = McpTokenViewModel(mockRepository)
    composeTestRule.setContent { McpTokenScreen(viewModel = viewModel, onNavigateBack = {}) }

    waitUntilNotLoading()

    composeTestRule
        .onNodeWithTag("${McpTokenScreenTestTags.COPY_BUTTON}_token-123-full-id")
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("${McpTokenScreenTestTags.DELETE_BUTTON}_token-123-full-id")
        .assertIsDisplayed()
  }

  @Test
  fun mcpTokenScreen_deleteButtonShowsConfirmationDialog() {
    coEvery { mockRepository.listTokens() } returns Result.success(testTokens)

    val viewModel = McpTokenViewModel(mockRepository)
    composeTestRule.setContent { McpTokenScreen(viewModel = viewModel, onNavigateBack = {}) }

    waitUntilNotLoading()

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
    coEvery { mockRepository.listTokens() } returns Result.success(emptyList())

    var backClicked = false
    val viewModel = McpTokenViewModel(mockRepository)
    composeTestRule.setContent {
      McpTokenScreen(viewModel = viewModel, onNavigateBack = { backClicked = true })
    }

    waitUntilNotLoading()

    composeTestRule.onNodeWithTag(McpTokenScreenTestTags.BACK_BUTTON).performClick()
    assert(backClicked)
  }

  @Test
  fun mcpTokenScreen_tokenCreatedDialogIsDisplayedWhenTokenCreated() {
    coEvery { mockRepository.listTokens() } returns Result.success(emptyList())
    coEvery { mockRepository.createToken(any(), any()) } returns
        Result.success(McpToken(tokenId = "new-secret-token-value", name = "New Token"))

    val viewModel = McpTokenViewModel(mockRepository)
    composeTestRule.setContent { McpTokenScreen(viewModel = viewModel, onNavigateBack = {}) }

    waitUntilNotLoading()

    composeTestRule.onNodeWithTag(McpTokenScreenTestTags.CREATE_BUTTON).performClick()
    composeTestRule.onNodeWithTag(McpTokenScreenTestTags.CONFIRM_CREATE).performClick()

    waitUntilNotLoading()

    composeTestRule.onNodeWithTag(McpTokenScreenTestTags.TOKEN_CREATED_DIALOG).assertIsDisplayed()
    composeTestRule.onNodeWithText("Token Created").assertIsDisplayed()
  }

  @Test
  fun mcpTokenScreen_retryButtonReloadsTokensWhenErrorOccurs() {
    coEvery { mockRepository.listTokens() } returns Result.failure(Exception("Network error"))

    val viewModel = McpTokenViewModel(mockRepository)
    composeTestRule.setContent { McpTokenScreen(viewModel = viewModel, onNavigateBack = {}) }

    waitUntilNotLoading()

    coEvery { mockRepository.listTokens() } returns Result.success(testTokens)

    composeTestRule.onNodeWithText("Retry").performClick()

    waitUntilNotLoading()

    coVerify(atLeast = 2) { mockRepository.listTokens() }
  }

  @Test
  fun mcpTokenScreen_tokenCardDisplaysTokenInfo() {
    coEvery { mockRepository.listTokens() } returns Result.success(testTokens)

    val viewModel = McpTokenViewModel(mockRepository)
    composeTestRule.setContent { McpTokenScreen(viewModel = viewModel, onNavigateBack = {}) }

    waitUntilNotLoading()

    composeTestRule.onNodeWithText("Test Token").assertIsDisplayed()
    composeTestRule.onNodeWithText("ID: token-123-full", substring = true).assertIsDisplayed()
  }

  @Test
  fun mcpTokenScreen_tokenCardDisplaysExpiredStatusWhenExpired() {
    val expiredToken =
        McpToken(
            tokenId = "expired-token",
            name = "Expired Token",
            createdAt = Instant.parse("2024-01-01T00:00:00Z"),
            expiresAt = Instant.parse("2024-02-01T00:00:00Z"))

    coEvery { mockRepository.listTokens() } returns Result.success(listOf(expiredToken))

    val viewModel = McpTokenViewModel(mockRepository)
    composeTestRule.setContent { McpTokenScreen(viewModel = viewModel, onNavigateBack = {}) }

    waitUntilNotLoading()

    composeTestRule.onNodeWithText("Expired:", substring = true).assertIsDisplayed()
  }

  @Test
  fun mcpTokenScreen_displaysTopBar() {
    coEvery { mockRepository.listTokens() } returns Result.success(emptyList())

    val viewModel = McpTokenViewModel(mockRepository)
    composeTestRule.setContent { McpTokenScreen(viewModel = viewModel, onNavigateBack = {}) }

    waitUntilNotLoading()

    composeTestRule.onNodeWithText("MCP Tokens").assertIsDisplayed()
  }
}
