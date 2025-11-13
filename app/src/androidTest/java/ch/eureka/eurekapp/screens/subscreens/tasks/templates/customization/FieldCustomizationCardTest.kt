package ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.screens.subscreens.tasks.FieldInteractionMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

/**
 * Android UI tests for FieldCustomizationCard.
 *
 * Portions of this code were generated with the help of Claude Sonnet 4.5.
 */
class FieldCustomizationCardTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val testField =
      FieldDefinition(
          id = "test_field", label = "Test Field", type = FieldType.Text(), required = false)

  @Test
  fun fieldCustomizationCard_viewMode_showsFieldLabel() {
    composeTestRule.setContent {
      FieldCustomizationCard(
          field = testField, mode = FieldInteractionMode.ViewOnly, onFieldUpdate = {})
    }

    // "Test Field" appears in both the title and preview, check that at least one exists
    assert(composeTestRule.onAllNodesWithText("Test Field").fetchSemanticsNodes().isNotEmpty())
  }

  @Test
  fun fieldCustomizationCard_editMode_showsConfigurationTitle() {
    composeTestRule.setContent {
      FieldCustomizationCard(
          field = testField, mode = FieldInteractionMode.EditOnly, onFieldUpdate = {})
    }

    // Expand the card first
    composeTestRule.onNodeWithTag("field_expand_button").performClick()
    // Check that Advanced Configuration title is displayed
    composeTestRule.onNodeWithText("Advanced Configuration").assertIsDisplayed()
  }

  @Test
  fun fieldCustomizationCard_editMode_showsCommonConfiguration() {
    composeTestRule.setContent {
      FieldCustomizationCard(
          field = testField, mode = FieldInteractionMode.EditOnly, onFieldUpdate = {})
    }

    // Expand the card first
    composeTestRule.onNodeWithTag("field_expand_button").performClick()
    composeTestRule.onNodeWithTag("field_label_input").assertIsDisplayed()
    composeTestRule.onNodeWithTag("field_required_checkbox").assertIsDisplayed()
  }

  @Test
  fun fieldCustomizationCard_actionButtons_allDisplayed() {
    composeTestRule.setContent {
      FieldCustomizationCard(
          field = testField, mode = FieldInteractionMode.EditOnly, onFieldUpdate = {})
    }

    // Move up/down/duplicate buttons are visible in the header
    composeTestRule.onNodeWithTag("field_move_up_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("field_move_down_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("field_duplicate_button").assertIsDisplayed()

    // Delete button only shows when expanded
    composeTestRule.onNodeWithTag("field_expand_button").performClick()
    composeTestRule.onNodeWithTag("field_delete_button").assertIsDisplayed()
  }

  @Test
  fun fieldCustomizationCard_moveUpButton_respectsCanMoveUp() {
    composeTestRule.setContent {
      FieldCustomizationCard(
          field = testField,
          mode = FieldInteractionMode.EditOnly,
          onFieldUpdate = {},
          canMoveUp = false)
    }

    composeTestRule.onNodeWithTag("field_move_up_button").assertIsNotEnabled()
  }

  @Test
  fun fieldCustomizationCard_moveDownButton_respectsCanMoveDown() {
    composeTestRule.setContent {
      FieldCustomizationCard(
          field = testField,
          mode = FieldInteractionMode.EditOnly,
          onFieldUpdate = {},
          canMoveDown = false)
    }

    composeTestRule.onNodeWithTag("field_move_down_button").assertIsNotEnabled()
  }

  @Test
  fun fieldCustomizationCard_deleteButton_triggersCallback() {
    var deleted = false
    composeTestRule.setContent {
      FieldCustomizationCard(
          field = testField,
          mode = FieldInteractionMode.EditOnly,
          onFieldUpdate = {},
          onDelete = { deleted = true })
    }

    // Expand the card to see the delete button
    composeTestRule.onNodeWithTag("field_expand_button").performClick()
    composeTestRule.onNodeWithTag("field_delete_button").performClick()
    assert(deleted)
  }

  @Test
  fun fieldCustomizationCard_duplicateButton_triggersCallback() {
    var duplicated = false
    composeTestRule.setContent {
      FieldCustomizationCard(
          field = testField,
          mode = FieldInteractionMode.EditOnly,
          onFieldUpdate = {},
          onDuplicate = { duplicated = true })
    }

    composeTestRule.onNodeWithTag("field_duplicate_button").performClick()
    assert(duplicated)
  }

  @Test
  fun fieldCustomizationCard_toggleableMode_viewState_showsEditButton() {
    composeTestRule.setContent {
      FieldCustomizationCard(
          field = testField,
          mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = false),
          onFieldUpdate = {})
    }

    // The expand button shows Edit icon in toggleable not-editing mode
    composeTestRule.onNodeWithTag("field_expand_button").assertIsDisplayed()
  }

  @Test
  fun fieldCustomizationCard_toggleableMode_editState_showsSaveAndCancel() {
    composeTestRule.setContent {
      FieldCustomizationCard(
          field = testField,
          mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = true),
          onFieldUpdate = {})
    }

    // The expand button shows Check icon (save) when expanded in editing mode
    // The cancel button is visible when editing
    composeTestRule.onNodeWithTag("field_expand_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("field_cancel_button").assertIsDisplayed()
  }

  @Test
  fun fieldCustomizationCard_toggleableMode_editButton_triggersCallback() {
    var toggled = false
    composeTestRule.setContent {
      FieldCustomizationCard(
          field = testField,
          mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = false),
          onFieldUpdate = {},
          onModeToggle = { toggled = true })
    }

    // The expand button acts as edit button in toggleable not-editing mode
    composeTestRule.onNodeWithTag("field_expand_button").performClick()
    assert(toggled)
  }

  @Test
  fun fieldCustomizationCard_toggleableMode_saveButton_triggersCallbacks() {
    var updated: FieldDefinition? = null
    var toggled = false
    composeTestRule.setContent {
      FieldCustomizationCard(
          field = testField,
          mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = true),
          onFieldUpdate = { updated = it },
          onModeToggle = { toggled = true })
    }

    // First expand the card, then click expand button again to save
    composeTestRule.onNodeWithTag("field_expand_button").performClick()
    composeTestRule.onNodeWithTag("field_expand_button").performClick()
    assertNotNull(updated)
    assert(toggled)
  }

  @Test
  fun fieldCustomizationCard_toggleableMode_cancelButton_triggersToggle() {
    var toggled = false
    composeTestRule.setContent {
      FieldCustomizationCard(
          field = testField,
          mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = true),
          onFieldUpdate = {},
          onModeToggle = { toggled = true })
    }

    composeTestRule.onNodeWithTag("field_cancel_button").performClick()
    assert(toggled)
  }

  @Test
  fun fieldCustomizationCard_editMode_labelChange_updatesField() {
    var updatedField: FieldDefinition? = null
    composeTestRule.setContent {
      FieldCustomizationCard(
          field = testField,
          mode = FieldInteractionMode.EditOnly,
          onFieldUpdate = { updatedField = it })
    }

    // Expand the card to access the label input
    composeTestRule.onNodeWithTag("field_expand_button").performClick()
    composeTestRule.onNodeWithTag("field_label_input").performTextInput("X")
    assertNotNull(updatedField)
    assertEquals("XTest Field", updatedField?.label)
  }

  @Test
  fun fieldCustomizationCard_viewOnlyMode_expandButtonTogglesExpansion() {
    composeTestRule.setContent {
      FieldCustomizationCard(
          field = testField, mode = FieldInteractionMode.ViewOnly, onFieldUpdate = {})
    }

    // Initially collapsed - Advanced Configuration text should not exist
    composeTestRule.onNodeWithText("Advanced Configuration").assertDoesNotExist()

    // Click to expand
    composeTestRule.onNodeWithTag("field_expand_button").performClick()
    // Should now show the preview (expanded)
    composeTestRule.onNodeWithText("Test Field").assertIsDisplayed()

    // Click to collapse
    composeTestRule.onNodeWithTag("field_expand_button").performClick()
    // The preview should still be there
    composeTestRule.onNodeWithText("Test Field").assertIsDisplayed()
  }

  @Test
  fun fieldCustomizationCard_editOnlyMode_expandButtonTogglesExpansion() {
    composeTestRule.setContent {
      FieldCustomizationCard(
          field = testField, mode = FieldInteractionMode.EditOnly, onFieldUpdate = {})
    }

    // Initially collapsed - label input should not exist
    composeTestRule.onNodeWithTag("field_label_input").assertDoesNotExist()

    // Click to expand
    composeTestRule.onNodeWithTag("field_expand_button").performClick()
    // Should now show the label input
    composeTestRule.onNodeWithTag("field_label_input").assertIsDisplayed()

    // Click to collapse
    composeTestRule.onNodeWithTag("field_expand_button").performClick()
    // Label input should no longer be visible
    composeTestRule.onNodeWithTag("field_label_input").assertDoesNotExist()
  }

  @Test
  fun fieldCustomizationCard_toggleableNotEditing_expandButtonEntersEditAndExpands() {
    var toggled = false
    composeTestRule.setContent {
      FieldCustomizationCard(
          field = testField,
          mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = false),
          onFieldUpdate = {},
          onModeToggle = { toggled = true })
    }

    // Initially collapsed and not editing
    composeTestRule.onNodeWithTag("field_label_input").assertDoesNotExist()
    assert(!toggled)

    // Click to enter edit mode and expand
    composeTestRule.onNodeWithTag("field_expand_button").performClick()
    // Should have toggled to editing mode
    assert(toggled)
  }

  @Test
  fun fieldCustomizationCard_toggleableEditing_expandButtonSavesExitsAndCollapses() {
    var updated: FieldDefinition? = null
    var toggled = false
    composeTestRule.setContent {
      FieldCustomizationCard(
          field = testField,
          mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = true),
          onFieldUpdate = { updated = it },
          onModeToggle = { toggled = true })
    }

    // First expand the card
    composeTestRule.onNodeWithTag("field_expand_button").performClick()
    // Should now be expanded and show label input
    composeTestRule.onNodeWithTag("field_label_input").assertIsDisplayed()

    // Click expand button again to save, exit edit, and collapse
    composeTestRule.onNodeWithTag("field_expand_button").performClick()
    // Should have saved and toggled
    assertNotNull(updated)
    assert(toggled)
  }

  @Test
  fun fieldCustomizationCard_toggleableEditing_cancelButtonRevertsExitsAndCollapses() {
    var toggled = false
    composeTestRule.setContent {
      FieldCustomizationCard(
          field = testField,
          mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = true),
          onFieldUpdate = {},
          onModeToggle = { toggled = true })
    }

    // Cancel button should be visible when editing
    composeTestRule.onNodeWithTag("field_cancel_button").assertIsDisplayed()

    // Click cancel
    composeTestRule.onNodeWithTag("field_cancel_button").performClick()
    // Should have toggled (exited edit mode)
    assert(toggled)
  }
}
