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
 * Portions of this code were generated with the help of Claude Sonnet 4.5.
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

    composeTestRule.onNodeWithTag(FieldComponentTestTags.label("test_field")).assertIsDisplayed()
    composeTestRule.onNodeWithText("Test Field").assertIsDisplayed()
  }

  @Test
  fun baseFieldComponent_whenRequiredDisplaysAsterisk() {
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

    composeTestRule
        .onNodeWithTag(FieldComponentTestTags.description("test_field"))
        .assertIsDisplayed()
    composeTestRule.onNodeWithText("Test description").assertIsDisplayed()
  }

  @Test
  fun baseFieldComponent_whenDescriptionNullDoesNotDisplayDescription() {
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

    composeTestRule
        .onNodeWithTag(FieldComponentTestTags.description("test_field"))
        .assertDoesNotExist()
  }

  @Test
  fun baseFieldComponent_whenEditOnlyDoesNotShowToggleButton() {
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

    composeTestRule.onNodeWithTag(FieldComponentTestTags.toggle("test_field")).assertDoesNotExist()
  }

  @Test
  fun baseFieldComponent_whenViewOnlyDoesNotShowToggleButton() {
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

    composeTestRule.onNodeWithTag(FieldComponentTestTags.toggle("test_field")).assertDoesNotExist()
  }

  @Test
  fun baseFieldComponent_whenToggleableShowsToggleButton() {
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

    composeTestRule.onNodeWithTag(FieldComponentTestTags.toggle("test_field")).assertIsDisplayed()
  }

  @Test
  fun baseFieldComponent_whenToggleableAndToggleClickedCallsOnModeToggle() {
    var toggleCalled = false

    composeTestRule.setContent {
      BaseFieldComponent(
          fieldDefinition = testFieldDefinition,
          fieldType = FieldType.Text(),
          value = null,
          onValueChange = {},
          mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = false),
          callbacks = FieldCallbacks(onModeToggle = { toggleCalled = true })) { _, _, _ ->
            Text("Test Content")
          }
    }

    composeTestRule.onNodeWithTag(FieldComponentTestTags.toggle("test_field")).performClick()

    assertTrue(toggleCalled)
  }

  @Test
  fun baseFieldComponent_rendererReceivesCorrectEditingStateWhenEditOnly() {
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
  fun baseFieldComponent_rendererReceivesCorrectEditingStateWhenViewOnly() {
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
  fun baseFieldComponent_rendererReceivesCorrectEditingStateWhenToggleableEditing() {
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
  fun baseFieldComponent_rendererReceivesCorrectEditingStateWhenToggleableViewing() {
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
  fun baseFieldComponent_whenRequiredAndNullValueShowsValidationError() {
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

    composeTestRule.onNodeWithTag(FieldComponentTestTags.error("test_field")).assertIsDisplayed()
    composeTestRule.onNodeWithText("This field is required").assertIsDisplayed()
  }

  @Test
  fun baseFieldComponent_whenInvalidValueShowsValidationError() {
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

    composeTestRule.onNodeWithTag(FieldComponentTestTags.error("test_field")).assertIsDisplayed()
    composeTestRule.onNodeWithText("Text exceeds maxLength of 5 characters").assertIsDisplayed()
  }

  @Test
  fun baseFieldComponent_whenValidationErrorsHiddenDoesNotShowErrors() {
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

    composeTestRule.onNodeWithTag(FieldComponentTestTags.error("test_field")).assertDoesNotExist()
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

    composeTestRule.onNodeWithTag(FieldComponentTestTags.hint("test_field")).assertIsDisplayed()
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

    composeTestRule.onNodeWithTag(FieldComponentTestTags.hint("test_field")).assertIsDisplayed()
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
  fun baseFieldComponent_toggleableEditingShowsSaveAndCancelButtons() {
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

    composeTestRule.onNodeWithTag(FieldComponentTestTags.save("test_field")).assertIsDisplayed()
    composeTestRule.onNodeWithTag(FieldComponentTestTags.cancel("test_field")).assertIsDisplayed()
    composeTestRule.onNodeWithTag(FieldComponentTestTags.toggle("test_field")).assertDoesNotExist()
  }

  @Test
  fun baseFieldComponent_toggleableViewingShowsEditButton() {
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

    composeTestRule.onNodeWithTag(FieldComponentTestTags.toggle("test_field")).assertIsDisplayed()
    composeTestRule.onNodeWithTag(FieldComponentTestTags.save("test_field")).assertDoesNotExist()
    composeTestRule.onNodeWithTag(FieldComponentTestTags.cancel("test_field")).assertDoesNotExist()
  }

  @Test
  fun baseFieldComponent_saveButtonCommitsValueAndCallsCallbacks() {
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
          callbacks =
              FieldCallbacks(
                  onModeToggle = { toggleCalled = true }, onSave = { saveCalled = true })) {
              _,
              onValueChange,
              _ ->
            // Simulate user editing
            androidx.compose.runtime.SideEffect { onValueChange(editedValue) }
            Text("Test Content")
          }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(FieldComponentTestTags.save("test_field")).performClick()

    assertEquals(editedValue, committedValue)
    assertTrue(saveCalled)
    assertTrue(toggleCalled)
  }

  @Test
  fun baseFieldComponent_cancelButtonDiscardsChangesAndCallsCallbacks() {
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
          callbacks =
              FieldCallbacks(
                  onModeToggle = { toggleCalled = true }, onCancel = { cancelCalled = true })) {
              _,
              onValueChange,
              _ ->
            // Simulate user editing
            onValueChange(FieldValue.TextValue("Edited"))
            Text("Test Content")
          }
    }

    val originalValue = committedValue

    composeTestRule.onNodeWithTag(FieldComponentTestTags.cancel("test_field")).performClick()

    // Value should remain unchanged
    assertEquals(originalValue, committedValue)
    assertTrue(cancelCalled)
    assertTrue(toggleCalled)
  }

  @Test
  fun baseFieldComponent_editOnlyModeCallsOnValueChangeImmediately() {
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
  fun baseFieldComponent_toggleableModeBuffersChangesUntilSave() {
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
            androidx.compose.runtime.SideEffect { onValueChange(editedValue) }
            Text("Test Content")
          }
    }

    composeTestRule.waitForIdle()

    // Value should NOT be committed yet
    assertEquals(null, committedValue)

    // Click save
    composeTestRule.onNodeWithTag(FieldComponentTestTags.save("test_field")).performClick()

    // NOW value should be committed
    assertEquals(editedValue, committedValue)
  }

  @Test
  fun baseFieldComponent_whenShowHeaderTrueDisplaysHeader() {
    composeTestRule.setContent {
      BaseFieldComponent(
          fieldDefinition = testFieldDefinition,
          fieldType = FieldType.Text(),
          value = null,
          onValueChange = {},
          mode = FieldInteractionMode.EditOnly,
          showHeader = true) { _, _, _ ->
            Text("Test Content")
          }
    }

    // Label should be displayed
    composeTestRule.onNodeWithTag(FieldComponentTestTags.label("test_field")).assertIsDisplayed()
    // Description should be displayed
    composeTestRule
        .onNodeWithTag(FieldComponentTestTags.description("test_field"))
        .assertIsDisplayed()
  }

  @Test
  fun baseFieldComponent_whenShowHeaderFalseHidesHeader() {
    composeTestRule.setContent {
      BaseFieldComponent(
          fieldDefinition = testFieldDefinition,
          fieldType = FieldType.Text(),
          value = null,
          onValueChange = {},
          mode = FieldInteractionMode.EditOnly,
          showHeader = false) { _, _, _ ->
            Text("Test Content")
          }
    }

    // Label should NOT be displayed
    composeTestRule.onNodeWithTag(FieldComponentTestTags.label("test_field")).assertDoesNotExist()
    // Description should NOT be displayed
    composeTestRule
        .onNodeWithTag(FieldComponentTestTags.description("test_field"))
        .assertDoesNotExist()
  }

  @Test
  fun baseFieldComponent_whenShowHeaderFalseStillRendersContent() {
    composeTestRule.setContent {
      BaseFieldComponent(
          fieldDefinition = testFieldDefinition,
          fieldType = FieldType.Text(),
          value = null,
          onValueChange = {},
          mode = FieldInteractionMode.EditOnly,
          showHeader = false) { _, _, _ ->
            Text("Test Content")
          }
    }

    // Content should still be displayed
    composeTestRule.onNodeWithText("Test Content").assertIsDisplayed()
  }

  @Test
  fun baseFieldComponent_whenShowHeaderFalseHidesActionButtons() {
    composeTestRule.setContent {
      BaseFieldComponent(
          fieldDefinition = testFieldDefinition,
          fieldType = FieldType.Text(),
          value = null,
          onValueChange = {},
          mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = false),
          showHeader = false) { _, _, _ ->
            Text("Test Content")
          }
    }

    // Toggle button should NOT be displayed
    composeTestRule.onNodeWithTag(FieldComponentTestTags.toggle("test_field")).assertDoesNotExist()
  }

  @Test
  fun baseFieldComponent_whenShowHeaderFalseRendererStillReceivesCorrectEditingState() {
    var receivedIsEditing = false

    composeTestRule.setContent {
      BaseFieldComponent(
          fieldDefinition = testFieldDefinition,
          fieldType = FieldType.Text(),
          value = null,
          onValueChange = {},
          mode = FieldInteractionMode.EditOnly,
          showHeader = false) { _, _, isEditing ->
            receivedIsEditing = isEditing
            Text("Test Content")
          }
    }

    // Renderer should still receive correct editing state
    assertTrue(receivedIsEditing)
  }
}
