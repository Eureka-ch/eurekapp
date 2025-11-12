package ch.eureka.eurekapp.screens.subscreens.tasks

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.model.data.template.field.FieldValue
import ch.eureka.eurekapp.model.data.template.field.SelectOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

/**
 * Android UI tests for SingleSelectFieldComponent.
 *
 * Portions of this code were generated with the help of AI.
 */
class SingleSelectFieldComponentTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val testOptions =
      listOf(
          SelectOption("low", "Low", "Low priority"),
          SelectOption("medium", "Medium", "Medium priority"),
          SelectOption("high", "High", "High priority"))

  private val testFieldDefinition =
      FieldDefinition(
          id = "test_select",
          label = "Test Single Select",
          type = FieldType.SingleSelect(options = testOptions, allowCustom = false),
          required = false)

  @Test
  fun singleSelectFieldComponent_editMode_showsDropdown() {
    composeTestRule.setContent {
      SingleSelectFieldComponent(
          fieldDefinition = testFieldDefinition,
          value = null,
          onValueChange = {},
          mode = FieldInteractionMode.EditOnly)
    }

    composeTestRule.onNodeWithTag("single_select_field_dropdown_test_select").assertIsDisplayed()
  }

  @Test
  fun singleSelectFieldComponent_editMode_menuOpensOnClick() {
    composeTestRule.setContent {
      SingleSelectFieldComponent(
          fieldDefinition = testFieldDefinition,
          value = null,
          onValueChange = {},
          mode = FieldInteractionMode.EditOnly)
    }

    composeTestRule.onNodeWithTag("single_select_field_dropdown_test_select").performClick()

    composeTestRule.onNodeWithTag("single_select_field_menu_test_select").assertIsDisplayed()
  }

  @Test
  fun singleSelectFieldComponent_editMode_displaysAllOptions() {
    composeTestRule.setContent {
      SingleSelectFieldComponent(
          fieldDefinition = testFieldDefinition,
          value = null,
          onValueChange = {},
          mode = FieldInteractionMode.EditOnly)
    }

    composeTestRule.onNodeWithTag("single_select_field_dropdown_test_select").performClick()

    composeTestRule.onNodeWithTag("single_select_option_low").assertIsDisplayed()
    composeTestRule.onNodeWithTag("single_select_option_medium").assertIsDisplayed()
    composeTestRule.onNodeWithTag("single_select_option_high").assertIsDisplayed()
  }

  @Test
  fun singleSelectFieldComponent_editMode_displaysOptionDescriptions() {
    composeTestRule.setContent {
      SingleSelectFieldComponent(
          fieldDefinition = testFieldDefinition,
          value = null,
          onValueChange = {},
          mode = FieldInteractionMode.EditOnly)
    }

    composeTestRule.onNodeWithTag("single_select_field_dropdown_test_select").performClick()

    // Verify dropdown items are visible using test tags
    composeTestRule.onNodeWithTag("single_select_option_low").assertIsDisplayed()
    composeTestRule.onNodeWithTag("single_select_option_medium").assertIsDisplayed()
    composeTestRule.onNodeWithTag("single_select_option_high").assertIsDisplayed()

    // Verify descriptions are displayed within the dropdown items
    composeTestRule.onNodeWithText("Low priority").assertIsDisplayed()
    composeTestRule.onNodeWithText("Medium priority").assertIsDisplayed()
    composeTestRule.onNodeWithText("High priority").assertIsDisplayed()
  }

  @Test
  fun singleSelectFieldComponent_editMode_selectingOptionUpdatesValue() {
    var capturedValue: FieldValue.SingleSelectValue? = null

    composeTestRule.setContent {
      SingleSelectFieldComponent(
          fieldDefinition = testFieldDefinition,
          value = null,
          onValueChange = { capturedValue = it },
          mode = FieldInteractionMode.EditOnly)
    }

    composeTestRule.onNodeWithTag("single_select_field_dropdown_test_select").performClick()
    composeTestRule.onNodeWithTag("single_select_option_high").performClick()

    assertNotNull(capturedValue)
    assertEquals("high", capturedValue?.value)
  }

  @Test
  fun singleSelectFieldComponent_editMode_selectedValueDisplaysInTextField() {
    composeTestRule.setContent {
      SingleSelectFieldComponent(
          fieldDefinition = testFieldDefinition,
          value = FieldValue.SingleSelectValue("medium"),
          onValueChange = {},
          mode = FieldInteractionMode.EditOnly)
    }

    composeTestRule.onNodeWithText("medium").assertIsDisplayed()
  }

  @Test
  fun singleSelectFieldComponent_viewMode_showsSelectedValue() {
    composeTestRule.setContent {
      SingleSelectFieldComponent(
          fieldDefinition = testFieldDefinition,
          value = FieldValue.SingleSelectValue("high"),
          onValueChange = {},
          mode = FieldInteractionMode.ViewOnly)
    }

    composeTestRule.onNodeWithTag("single_select_field_value_test_select").assertIsDisplayed()
    composeTestRule.onNodeWithText("High").assertIsDisplayed()
  }

  @Test
  fun singleSelectFieldComponent_viewMode_doesNotShowDropdown() {
    composeTestRule.setContent {
      SingleSelectFieldComponent(
          fieldDefinition = testFieldDefinition,
          value = FieldValue.SingleSelectValue("low"),
          onValueChange = {},
          mode = FieldInteractionMode.ViewOnly)
    }

    composeTestRule.onNodeWithTag("single_select_field_dropdown_test_select").assertDoesNotExist()
  }

  @Test
  fun singleSelectFieldComponent_viewMode_displaysOptionLabel() {
    composeTestRule.setContent {
      SingleSelectFieldComponent(
          fieldDefinition = testFieldDefinition,
          value = FieldValue.SingleSelectValue("medium"),
          onValueChange = {},
          mode = FieldInteractionMode.ViewOnly)
    }

    composeTestRule.onNodeWithText("Medium").assertIsDisplayed()
  }

  @Test
  fun singleSelectFieldComponent_allowCustomTrue_allowsCustomInput() {
    val fieldWithCustom =
        testFieldDefinition.copy(
            type = FieldType.SingleSelect(options = testOptions, allowCustom = true))
    var capturedValue: FieldValue.SingleSelectValue? = null

    composeTestRule.setContent {
      SingleSelectFieldComponent(
          fieldDefinition = fieldWithCustom,
          value = null,
          onValueChange = { capturedValue = it },
          mode = FieldInteractionMode.EditOnly)
    }

    // Type directly into the text field using the input test tag
    composeTestRule
        .onNodeWithTag("single_select_field_input_test_select")
        .performTextInput("custom")

    assertNotNull(capturedValue)
    assertEquals("custom", capturedValue?.value)
  }

  @Test
  fun singleSelectFieldComponent_allowCustomTrue_displaysCustomValue() {
    val fieldWithCustom =
        testFieldDefinition.copy(
            type = FieldType.SingleSelect(options = testOptions, allowCustom = true))

    composeTestRule.setContent {
      SingleSelectFieldComponent(
          fieldDefinition = fieldWithCustom,
          value = FieldValue.SingleSelectValue("custom_value"),
          onValueChange = {},
          mode = FieldInteractionMode.ViewOnly)
    }

    composeTestRule.onNodeWithText("custom_value").assertIsDisplayed()
  }

  @Test
  fun singleSelectFieldComponent_toggleableMode_showsToggleButton() {
    composeTestRule.setContent {
      SingleSelectFieldComponent(
          fieldDefinition = testFieldDefinition,
          value = null,
          onValueChange = {},
          mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = false))
    }

    composeTestRule.onNodeWithTag("field_toggle_test_select").assertIsDisplayed()
  }

  @Test
  fun singleSelectFieldComponent_toggleableMode_callsOnModeToggle() {
    var toggleCalled = false

    composeTestRule.setContent {
      SingleSelectFieldComponent(
          fieldDefinition = testFieldDefinition,
          value = null,
          onValueChange = {},
          mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = false),
          onModeToggle = { toggleCalled = true })
    }

    composeTestRule.onNodeWithTag("field_toggle_test_select").performClick()

    assert(toggleCalled)
  }

  @Test
  fun singleSelectFieldComponent_requiredField_showsAsterisk() {
    val requiredField = testFieldDefinition.copy(required = true)

    composeTestRule.setContent {
      SingleSelectFieldComponent(
          fieldDefinition = requiredField,
          value = null,
          onValueChange = {},
          mode = FieldInteractionMode.EditOnly)
    }

    composeTestRule.onNodeWithText("Test Single Select *").assertIsDisplayed()
  }

  @Test
  fun singleSelectFieldComponent_withDescription_showsDescription() {
    val fieldWithDescription = testFieldDefinition.copy(description = "Choose a priority level")

    composeTestRule.setContent {
      SingleSelectFieldComponent(
          fieldDefinition = fieldWithDescription,
          value = null,
          onValueChange = {},
          mode = FieldInteractionMode.EditOnly)
    }

    composeTestRule.onNodeWithText("Choose a priority level").assertIsDisplayed()
  }

  @Test
  fun singleSelectFieldComponent_editMode_showsConstraintHint() {
    composeTestRule.setContent {
      SingleSelectFieldComponent(
          fieldDefinition = testFieldDefinition,
          value = null,
          onValueChange = {},
          mode = FieldInteractionMode.EditOnly)
    }

    composeTestRule.onNodeWithText("3 options").assertIsDisplayed()
  }

  @Test
  fun singleSelectFieldComponent_allowCustomTrue_showsConstraintHint() {
    val fieldWithCustom =
        testFieldDefinition.copy(
            type = FieldType.SingleSelect(options = testOptions, allowCustom = true))

    composeTestRule.setContent {
      SingleSelectFieldComponent(
          fieldDefinition = fieldWithCustom,
          value = null,
          onValueChange = {},
          mode = FieldInteractionMode.EditOnly)
    }

    composeTestRule.onNodeWithText("3 options (custom values allowed)").assertIsDisplayed()
  }

  @Test
  fun singleSelectFieldComponent_whenValidationEnabled_showsErrors() {
    val requiredField = testFieldDefinition.copy(required = true)

    composeTestRule.setContent {
      SingleSelectFieldComponent(
          fieldDefinition = requiredField,
          value = null,
          onValueChange = {},
          mode = FieldInteractionMode.EditOnly,
          showValidationErrors = true)
    }

    composeTestRule.onNodeWithText("This field is required").assertIsDisplayed()
  }

  @Test
  fun singleSelectFieldComponent_whenValidationDisabled_doesNotShowErrors() {
    val requiredField = testFieldDefinition.copy(required = true)

    composeTestRule.setContent {
      SingleSelectFieldComponent(
          fieldDefinition = requiredField,
          value = null,
          onValueChange = {},
          mode = FieldInteractionMode.EditOnly,
          showValidationErrors = false)
    }

    composeTestRule.onNodeWithText("This field is required").assertDoesNotExist()
  }
}
