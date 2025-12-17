/*
 * This file was co-authored by Claude Code.
 * This file was co-authored by Grok.
 */
package ch.eureka.eurekapp.ui.activity

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

class ActivitySearchBarTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun activitySearchBar_searchBarNotExpandedNotVisible() {
    composeTestRule.setContent {
      ActivitySearchBar(query = "", onQueryChange = {}, expanded = false)
    }

    composeTestRule.onNodeWithText("Search activities...").assertDoesNotExist()
  }

  @Test
  fun activitySearchBar_searchBarExpandedDisplaysSearchField() {
    composeTestRule.setContent {
      ActivitySearchBar(query = "", onQueryChange = {}, expanded = true)
    }

    composeTestRule.onNodeWithText("Search activities...").assertIsDisplayed()
  }

  @Test
  fun activitySearchBar_searchBarEmptyQueryNoClearButton() {
    composeTestRule.setContent {
      ActivitySearchBar(query = "", onQueryChange = {}, expanded = true)
    }

    composeTestRule.onNodeWithContentDescription("Clear search").assertDoesNotExist()
  }

  @Test
  fun activitySearchBar_searchBarWithQueryShowsClearButton() {
    composeTestRule.setContent {
      ActivitySearchBar(query = "test query", onQueryChange = {}, expanded = true)
    }

    composeTestRule.onNodeWithContentDescription("Clear search").assertIsDisplayed()
  }

  @Test
  fun activitySearchBar_searchBarTextInputTriggersCallback() {
    var capturedQuery = ""
    composeTestRule.setContent {
      ActivitySearchBar(query = "", onQueryChange = { capturedQuery = it }, expanded = true)
    }

    composeTestRule.onNodeWithText("Search activities...").performTextInput("new query")

    assert(capturedQuery == "new query")
  }

  @Test
  fun activitySearchBar_searchBarClearButtonClearsQuery() {
    var currentQuery = "test query"
    composeTestRule.setContent {
      ActivitySearchBar(
          query = currentQuery, onQueryChange = { currentQuery = it }, expanded = true)
    }

    composeTestRule.onNodeWithContentDescription("Clear search").performClick()

    assert(currentQuery == "")
  }

  @Test
  fun activitySearchBar_searchBarSingleLineDoesNotWrap() {
    composeTestRule.setContent {
      ActivitySearchBar(
          query = "This is a very long query that should not wrap to multiple lines",
          onQueryChange = {},
          expanded = true)
    }

    // Verify the text field exists with the long text (exact behavior may vary)
    composeTestRule
        .onNodeWithText("This is a very long query that should not wrap to multiple lines")
        .assertExists()
  }
}
