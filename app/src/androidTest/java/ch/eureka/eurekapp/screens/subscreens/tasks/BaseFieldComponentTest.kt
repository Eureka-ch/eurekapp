package ch.eureka.eurekapp.screens.subscreens.tasks

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.model.data.template.field.FieldValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for BaseFieldComponent.
 *
 * Portions of this code were generated with the help of AI.
 */
class BaseFieldComponentTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val testFieldDefinition =
      FieldDefinition(
          id = "test_field",
          label = "Test Field",
          type = FieldType.Text(),
          required = false,
          description = "Test description")

  @Test
  fun baseFieldComponent_displaysLabel() {
    composeTestRule.setContent {
      BaseFieldComponent(
          fieldDefinition = testFieldDefinition,
          fieldType = FieldType.Text(),
          value = null,
          onValueChange = {},
          mode = FieldInteractionMode.EditOnly) { _, _, _ ->
            Text("Test Content")
          }
    }

    composeTestRule.onNodeWithTag("field_label_test_field").assertIsDisplayed()
    composeTestRule.onNodeWithText("Test Field").assertIsDisplayed()
  }

  @Test
  fun baseFieldComponent_whenRequired_displaysAsterisk() {
    val requiredField = testFieldDefinition.copy(required = true)

    composeTestRule.setContent {
      BaseFieldComponent(
          fieldDefinition = requiredField,
          fieldType = FieldType.Text(),
          value = null,
          onValueChange = {},
          mode = FieldInteractionMode.EditOnly) { _, _, _ ->
            Text("Test Content")
          }
    }

    composeTestRule.onNodeWithText("Test Field *").assertIsDisplayed()
  }

  @Test
  fun baseFieldComponent_displaysDescription() {
    composeTestRule.setContent {
      BaseFieldComponent(
          fieldDefinition = testFieldDefinition,
          fieldType = FieldType.Text(),
          value = null,
          onValueChange = {},
          mode = FieldInteractionMode.EditOnly) { _, _, _ ->
            Text("Test Content")
          }
    }

    composeTestRule.onNodeWithTag("field_description_test_field").assertIsDisplayed()
    composeTestRule.onNodeWithText("Test description").assertIsDisplayed()
  }

  @Test
  fun baseFieldComponent_whenDescriptionNull_doesNotDisplayDescription() {
    val fieldWithoutDescription = testFieldDefinition.copy(description = null)

    composeTestRule.setContent {
      BaseFieldComponent(
          fieldDefinition = fieldWithoutDescription,
          fieldType = FieldType.Text(),
          value = null,
          onValueChange = {},
          mode = FieldInteractionMode.EditOnly) { _, _, _ ->
            Text("Test Content")
          }
    }

    composeTestRule.onNodeWithTag("field_description_test_field").assertDoesNotExist()
  }

  @Test
  fun baseFieldComponent_whenEditOnly_doesNotShowToggleButton() {
    composeTestRule.setContent {
      BaseFieldComponent(
          fieldDefinition = testFieldDefinition,
          fieldType = FieldType.Text(),
          value = null,
          onValueChange = {},
          mode = FieldInteractionMode.EditOnly) { _, _, _ ->
            Text("Test Content")
          }
    }

    composeTestRule.onNodeWithTag("field_toggle_test_field").assertDoesNotExist()
  }

  @Test
  fun baseFieldComponent_whenViewOnly_doesNotShowToggleButton() {
    composeTestRule.setContent {
      BaseFieldComponent(
          fieldDefinition = testFieldDefinition,
          fieldType = FieldType.Text(),
          value = null,
          onValueChange = {},
          mode = FieldInteractionMode.ViewOnly) { _, _, _ ->
            Text("Test Content")
          }
    }

    composeTestRule.onNodeWithTag("field_toggle_test_field").assertDoesNotExist()
  }

  @Test
  fun baseFieldComponent_whenToggleable_showsToggleButton() {
    composeTestRule.setContent {
      BaseFieldComponent(
          fieldDefinition = testFieldDefinition,
          fieldType = FieldType.Text(),
          value = null,
          onValueChange = {},
          mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = false)) { _, _, _ ->
            Text("Test Content")
          }
    }

    composeTestRule.onNodeWithTag("field_toggle_test_field").assertIsDisplayed()
  }

  @Test
  fun baseFieldComponent_whenToggleableAndToggleClicked_callsOnModeToggle() {
    var toggleCalled = false

    composeTestRule.setContent {
      BaseFieldComponent(
          fieldDefinition = testFieldDefinition,
          fieldType = FieldType.Text(),
          value = null,
          onValueChange = {},
          mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = false),
          onModeToggle = { toggleCalled = true }) { _, _, _ ->
            Text("Test Content")
          }
    }

    composeTestRule.onNodeWithTag("field_toggle_test_field").performClick()

    assertTrue(toggleCalled)
  }

  @Test
  fun baseFieldComponent_rendererReceivesCorrectEditingState_whenEditOnly() {
    var receivedIsEditing = false

    composeTestRule.setContent {
      BaseFieldComponent(
          fieldDefinition = testFieldDefinition,
          fieldType = FieldType.Text(),
          value = null,
          onValueChange = {},
          mode = FieldInteractionMode.EditOnly) { _, _, isEditing ->
            receivedIsEditing = isEditing
            Text("Test Content")
          }
    }

    assertTrue(receivedIsEditing)
  }

  @Test
  fun baseFieldComponent_rendererReceivesCorrectEditingState_whenViewOnly() {
    var receivedIsEditing = true

    composeTestRule.setContent {
      BaseFieldComponent(
          fieldDefinition = testFieldDefinition,
          fieldType = FieldType.Text(),
          value = null,
          onValueChange = {},
          mode = FieldInteractionMode.ViewOnly) { _, _, isEditing ->
            receivedIsEditing = isEditing
            Text("Test Content")
          }
    }

    assertFalse(receivedIsEditing)
  }

  @Test
  fun baseFieldComponent_rendererReceivesCorrectEditingState_whenToggleableEditing() {
    var receivedIsEditing = false

    composeTestRule.setContent {
      BaseFieldComponent(
          fieldDefinition = testFieldDefinition,
          fieldType = FieldType.Text(),
          value = null,
          onValueChange = {},
          mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = true)) { _, _, isEditing ->
            receivedIsEditing = isEditing
            Text("Test Content")
          }
    }

    assertTrue(receivedIsEditing)
  }

  @Test
  fun baseFieldComponent_rendererReceivesCorrectEditingState_whenToggleableViewing() {
    var receivedIsEditing = true

    composeTestRule.setContent {
      BaseFieldComponent(
          fieldDefinition = testFieldDefinition,
          fieldType = FieldType.Text(),
          value = null,
          onValueChange = {},
          mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = false)) { _, _, isEditing ->
            receivedIsEditing = isEditing
            Text("Test Content")
          }
    }

    assertFalse(receivedIsEditing)
  }

  @Test
  fun baseFieldComponent_whenRequiredAndNullValue_showsValidationError() {
    val requiredField = testFieldDefinition.copy(required = true)

    composeTestRule.setContent {
      BaseFieldComponent(
          fieldDefinition = requiredField,
          fieldType = FieldType.Text(),
          value = null,
          onValueChange = {},
          mode = FieldInteractionMode.EditOnly,
          showValidationErrors = true) { _, _, _ ->
            Text("Test Content")
          }
    }

    composeTestRule.onNodeWithTag("field_error_test_field").assertIsDisplayed()
    composeTestRule.onNodeWithText("This field is required").assertIsDisplayed()
  }

  @Test
  fun baseFieldComponent_whenInvalidValue_showsValidationError() {
    val textFieldWithMaxLength = FieldType.Text(maxLength = 5)
    val invalidValue = FieldValue.TextValue("Too long text")

    composeTestRule.setContent {
      BaseFieldComponent(
          fieldDefinition = testFieldDefinition.copy(type = textFieldWithMaxLength),
          fieldType = textFieldWithMaxLength,
          value = invalidValue,
          onValueChange = {},
          mode = FieldInteractionMode.EditOnly,
          showValidationErrors = true) { _, _, _ ->
            Text("Test Content")
          }
    }

    composeTestRule.onNodeWithTag("field_error_test_field").assertIsDisplayed()
    composeTestRule.onNodeWithText("Text exceeds maxLength of 5 characters").assertIsDisplayed()
  }

  @Test
  fun baseFieldComponent_whenValidationErrorsHidden_doesNotShowErrors() {
    val requiredField = testFieldDefinition.copy(required = true)

    composeTestRule.setContent {
      BaseFieldComponent(
          fieldDefinition = requiredField,
          fieldType = FieldType.Text(),
          value = null,
          onValueChange = {},
          mode = FieldInteractionMode.EditOnly,
          showValidationErrors = false) { _, _, _ ->
            Text("Test Content")
          }
    }

    composeTestRule.onNodeWithTag("field_error_test_field").assertDoesNotExist()
  }

  @Test
  fun baseFieldComponent_displaysTextFieldConstraintHints() {
    val textFieldWithConstraints = FieldType.Text(maxLength = 100, minLength = 10)

    composeTestRule.setContent {
      BaseFieldComponent(
          fieldDefinition = testFieldDefinition.copy(type = textFieldWithConstraints),
          fieldType = textFieldWithConstraints,
          value = null,
          onValueChange = {},
          mode = FieldInteractionMode.EditOnly) { _, _, _ ->
            Text("Test Content")
          }
    }

    composeTestRule.onNodeWithTag("field_hint_test_field").assertIsDisplayed()
    composeTestRule.onNodeWithText("Max 100 characters • Min 10 characters").assertIsDisplayed()
  }

  @Test
  fun baseFieldComponent_displaysNumberFieldConstraintHints() {
    val numberFieldWithConstraints = FieldType.Number(min = 0.0, max = 100.0, unit = "kg")

    composeTestRule.setContent {
      BaseFieldComponent(
          fieldDefinition = testFieldDefinition.copy(type = numberFieldWithConstraints),
          fieldType = numberFieldWithConstraints,
          value = null,
          onValueChange = {},
          mode = FieldInteractionMode.EditOnly) { _, _, _ ->
            Text("Test Content")
          }
    }

    composeTestRule.onNodeWithTag("field_hint_test_field").assertIsDisplayed()
    composeTestRule.onNodeWithText("Range: 0.0 - 100.0 • Unit: kg").assertIsDisplayed()
  }

  @Test
  fun baseFieldComponent_rendererReceivesValue() {
    val testValue = FieldValue.TextValue("Test Value")
    var receivedValue: FieldValue.TextValue? = null

    composeTestRule.setContent {
      BaseFieldComponent(
          fieldDefinition = testFieldDefinition,
          fieldType = FieldType.Text(),
          value = testValue,
          onValueChange = {},
          mode = FieldInteractionMode.EditOnly) { value, _, _ ->
            receivedValue = value
            Text("Test Content")
          }
    }

    assertEquals(testValue, receivedValue)
  }

  @Test
  fun baseFieldComponent_rendererReceivesOnValueChange() {
    var receivedValue: FieldValue.TextValue? = null
    val newValue = FieldValue.TextValue("New Value")

    composeTestRule.setContent {
      BaseFieldComponent<FieldType.Text, FieldValue.TextValue>(
          fieldDefinition = testFieldDefinition,
          fieldType = FieldType.Text(),
          value = null,
          onValueChange = { receivedValue = it },
          mode = FieldInteractionMode.EditOnly) { _, onValueChange, _ ->
            // Simulate value change in renderer
            onValueChange(newValue)
            Text("Test Content")
          }
    }

    assertEquals(newValue, receivedValue)
  }

  @Test
  fun baseFieldComponent_toggleableEditing_showsSaveAndCancelButtons() {
    composeTestRule.setContent {
      BaseFieldComponent(
          fieldDefinition = testFieldDefinition,
          fieldType = FieldType.Text(),
          value = null,
          onValueChange = {},
          mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = true)) { _, _, _ ->
            Text("Test Content")
          }
    }

    composeTestRule.onNodeWithTag("field_save_test_field").assertIsDisplayed()
    composeTestRule.onNodeWithTag("field_cancel_test_field").assertIsDisplayed()
    composeTestRule.onNodeWithTag("field_toggle_test_field").assertDoesNotExist()
  }

  @Test
  fun baseFieldComponent_toggleableViewing_showsEditButton() {
    composeTestRule.setContent {
      BaseFieldComponent(
          fieldDefinition = testFieldDefinition,
          fieldType = FieldType.Text(),
          value = null,
          onValueChange = {},
          mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = false)) { _, _, _ ->
            Text("Test Content")
          }
    }

    composeTestRule.onNodeWithTag("field_toggle_test_field").assertIsDisplayed()
    composeTestRule.onNodeWithTag("field_save_test_field").assertDoesNotExist()
    composeTestRule.onNodeWithTag("field_cancel_test_field").assertDoesNotExist()
  }

  @Test
  fun baseFieldComponent_saveButton_commitsValueAndCallsCallbacks() {
    var committedValue: FieldValue.TextValue? = null
    var saveCalled = false
    var toggleCalled = false
    val editedValue = FieldValue.TextValue("Edited")

    composeTestRule.setContent {
      BaseFieldComponent<FieldType.Text, FieldValue.TextValue>(
          fieldDefinition = testFieldDefinition,
          fieldType = FieldType.Text(),
          value = null,
          onValueChange = { committedValue = it },
          mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = true),
          onModeToggle = { toggleCalled = true },
          onSave = { saveCalled = true }) { _, onValueChange, _ ->
            // Simulate user editing
            onValueChange(editedValue)
            Text("Test Content")
          }
    }

    composeTestRule.onNodeWithTag("field_save_test_field").performClick()

    assertEquals(editedValue, committedValue)
    assertTrue(saveCalled)
    assertTrue(toggleCalled)
  }

  @Test
  fun baseFieldComponent_cancelButton_discardsChangesAndCallsCallbacks() {
    var committedValue: FieldValue.TextValue? = FieldValue.TextValue("Original")
    var cancelCalled = false
    var toggleCalled = false

    composeTestRule.setContent {
      BaseFieldComponent<FieldType.Text, FieldValue.TextValue>(
          fieldDefinition = testFieldDefinition,
          fieldType = FieldType.Text(),
          value = committedValue,
          onValueChange = { committedValue = it },
          mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = true),
          onModeToggle = { toggleCalled = true },
          onCancel = { cancelCalled = true }) { _, onValueChange, _ ->
            // Simulate user editing
            onValueChange(FieldValue.TextValue("Edited"))
            Text("Test Content")
          }
    }

    val originalValue = committedValue

    composeTestRule.onNodeWithTag("field_cancel_test_field").performClick()

    // Value should remain unchanged
    assertEquals(originalValue, committedValue)
    assertTrue(cancelCalled)
    assertTrue(toggleCalled)
  }

  @Test
  fun baseFieldComponent_editOnlyMode_callsOnValueChangeImmediately() {
    var receivedValue: FieldValue.TextValue? = null
    val newValue = FieldValue.TextValue("Immediate")

    composeTestRule.setContent {
      BaseFieldComponent<FieldType.Text, FieldValue.TextValue>(
          fieldDefinition = testFieldDefinition,
          fieldType = FieldType.Text(),
          value = null,
          onValueChange = { receivedValue = it },
          mode = FieldInteractionMode.EditOnly) { _, onValueChange, _ ->
            // Simulate user typing
            onValueChange(newValue)
            Text("Test Content")
          }
    }

    // In EditOnly mode, onValueChange should be called immediately
    assertEquals(newValue, receivedValue)
  }

  @Test
  fun baseFieldComponent_toggleableMode_buffersChangesUntilSave() {
    var committedValue: FieldValue.TextValue? = null
    val editedValue = FieldValue.TextValue("Buffered")

    composeTestRule.setContent {
      BaseFieldComponent<FieldType.Text, FieldValue.TextValue>(
          fieldDefinition = testFieldDefinition,
          fieldType = FieldType.Text(),
          value = null,
          onValueChange = { committedValue = it },
          mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = true)) { _, onValueChange, _
            ->
            // Simulate user typing
            onValueChange(editedValue)
            Text("Test Content")
          }
    }

    // Value should NOT be committed yet
    assertEquals(null, committedValue)

    // Click save
    composeTestRule.onNodeWithTag("field_save_test_field").performClick()

    // NOW value should be committed
    assertEquals(editedValue, committedValue)
  }
}
