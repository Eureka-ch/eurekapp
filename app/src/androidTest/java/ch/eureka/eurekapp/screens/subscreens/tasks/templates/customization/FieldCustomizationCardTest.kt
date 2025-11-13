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

    composeTestRule.onNodeWithText("Field Configuration").assertIsDisplayed()
  }

  @Test
  fun fieldCustomizationCard_editMode_showsCommonConfiguration() {
    composeTestRule.setContent {
      FieldCustomizationCard(
          field = testField, mode = FieldInteractionMode.EditOnly, onFieldUpdate = {})
    }

    composeTestRule.onNodeWithTag("field_label_input").assertIsDisplayed()
    composeTestRule.onNodeWithTag("field_required_checkbox").assertIsDisplayed()
  }

  @Test
  fun fieldCustomizationCard_actionButtons_allDisplayed() {
    composeTestRule.setContent {
      FieldCustomizationCard(
          field = testField, mode = FieldInteractionMode.ViewOnly, onFieldUpdate = {})
    }

    composeTestRule.onNodeWithTag("field_move_up_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("field_move_down_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("field_duplicate_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("field_delete_button").assertIsDisplayed()
  }

  @Test
  fun fieldCustomizationCard_moveUpButton_respectsCanMoveUp() {
    composeTestRule.setContent {
      FieldCustomizationCard(
          field = testField,
          mode = FieldInteractionMode.ViewOnly,
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
          mode = FieldInteractionMode.ViewOnly,
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
          mode = FieldInteractionMode.ViewOnly,
          onFieldUpdate = {},
          onDelete = { deleted = true })
    }

    composeTestRule.onNodeWithTag("field_delete_button").performClick()
    assert(deleted)
  }

  @Test
  fun fieldCustomizationCard_duplicateButton_triggersCallback() {
    var duplicated = false
    composeTestRule.setContent {
      FieldCustomizationCard(
          field = testField,
          mode = FieldInteractionMode.ViewOnly,
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

    composeTestRule.onNodeWithTag("field_edit_button").assertIsDisplayed()
  }

  @Test
  fun fieldCustomizationCard_toggleableMode_editState_showsSaveAndCancel() {
    composeTestRule.setContent {
      FieldCustomizationCard(
          field = testField,
          mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = true),
          onFieldUpdate = {})
    }

    composeTestRule.onNodeWithTag("field_save_button").assertIsDisplayed()
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

    composeTestRule.onNodeWithTag("field_edit_button").performClick()
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

    composeTestRule.onNodeWithTag("field_save_button").performClick()
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

    composeTestRule.onNodeWithTag("field_label_input").performTextInput("X")
    assertNotNull(updatedField)
    assertEquals("XTest Field", updatedField?.label)
  }
}
