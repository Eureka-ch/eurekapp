package ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.model.data.template.field.SelectOption
import ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.fieldtypes.DateFieldConfiguration
import ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.fieldtypes.MultiSelectFieldConfiguration
import ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.fieldtypes.NumberFieldConfiguration
import ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.fieldtypes.SingleSelectFieldConfiguration
import ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.fieldtypes.TextFieldConfiguration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

/**
 * Android UI tests for field type configuration components.
 *
 * Portions of this code were generated with the help of Claude Sonnet 4.5.
 */
class FieldTypeConfigurationsTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun textFieldConfiguration_displaysAllFields() {
    composeTestRule.setContent {
      TextFieldConfiguration(fieldType = FieldType.Text(), onUpdate = {}, enabled = true)
    }

    composeTestRule.onNodeWithTag("text_max_length").assertIsDisplayed()
    composeTestRule.onNodeWithTag("text_min_length").assertIsDisplayed()
    composeTestRule.onNodeWithTag("text_placeholder").assertIsDisplayed()
  }

  @Test
  fun textFieldConfiguration_maxLengthInputUpdatesType() {
    var updatedType: FieldType.Text? = null
    composeTestRule.setContent {
      TextFieldConfiguration(
          fieldType = FieldType.Text(), onUpdate = { updatedType = it }, enabled = true)
    }

    composeTestRule.onNodeWithTag("text_max_length").performTextInput("100")
    assertNotNull(updatedType)
    assertEquals(100, updatedType?.maxLength)
  }

  @Test
  fun numberFieldConfiguration_displaysAllFields() {
    composeTestRule.setContent {
      NumberFieldConfiguration(fieldType = FieldType.Number(), onUpdate = {}, enabled = true)
    }

    composeTestRule.onNodeWithTag("number_min").assertIsDisplayed()
    composeTestRule.onNodeWithTag("number_max").assertIsDisplayed()
    composeTestRule.onNodeWithTag("number_step").assertIsDisplayed()
    composeTestRule.onNodeWithTag("number_decimals").assertIsDisplayed()
    composeTestRule.onNodeWithTag("number_unit").assertIsDisplayed()
  }

  @Test
  fun numberFieldConfiguration_minInputUpdatesType() {
    var updatedType: FieldType.Number? = null
    composeTestRule.setContent {
      NumberFieldConfiguration(
          fieldType = FieldType.Number(), onUpdate = { updatedType = it }, enabled = true)
    }

    composeTestRule.onNodeWithTag("number_min").performTextInput("10")
    assertNotNull(updatedType)
    assertEquals(10.0, updatedType!!.min!!, 0.01)
  }

  @Test
  fun dateFieldConfiguration_displaysAllFields() {
    composeTestRule.setContent {
      DateFieldConfiguration(fieldType = FieldType.Date(), onUpdate = {}, enabled = true)
    }

    composeTestRule.onNodeWithTag("date_include_time").assertIsDisplayed()
    composeTestRule.onNodeWithTag("date_format").assertIsDisplayed()
  }

  @Test
  fun dateFieldConfiguration_includeTimeCheckboxUpdatesType() {
    var updatedType: FieldType.Date? = null
    composeTestRule.setContent {
      DateFieldConfiguration(
          fieldType = FieldType.Date(includeTime = false),
          onUpdate = { updatedType = it },
          enabled = true)
    }

    composeTestRule.onNodeWithTag("date_include_time").performClick()
    assertNotNull(updatedType)
    assertEquals(true, updatedType?.includeTime)
  }

  @Test
  fun singleSelectFieldConfiguration_displaysAllowCustomCheckbox() {
    val options = listOf(SelectOption("opt1", "Option 1"), SelectOption("opt2", "Option 2"))
    composeTestRule.setContent {
      SingleSelectFieldConfiguration(
          fieldType = FieldType.SingleSelect(options), onUpdate = {}, enabled = true)
    }

    composeTestRule.onNodeWithText("Allow Custom Values").assertIsDisplayed()
    composeTestRule.onNodeWithTag("single_select_allow_custom").assertIsDisplayed()
  }

  @Test
  fun singleSelectFieldConfiguration_allowCustomCheckboxUpdatesType() {
    val options = listOf(SelectOption("opt1", "Option 1"), SelectOption("opt2", "Option 2"))
    var updatedType: FieldType.SingleSelect? = null
    composeTestRule.setContent {
      SingleSelectFieldConfiguration(
          fieldType = FieldType.SingleSelect(options, allowCustom = false),
          onUpdate = { updatedType = it },
          enabled = true)
    }

    composeTestRule.onNodeWithTag("single_select_allow_custom").performClick()
    assertNotNull(updatedType)
    assertEquals(true, updatedType?.allowCustom)
  }

  @Test
  fun multiSelectFieldConfiguration_displaysAllFields() {
    val options = listOf(SelectOption("opt1", "Option 1"), SelectOption("opt2", "Option 2"))
    composeTestRule.setContent {
      MultiSelectFieldConfiguration(
          fieldType = FieldType.MultiSelect(options), onUpdate = {}, enabled = true)
    }

    composeTestRule.onNodeWithText("Allow Custom Values").assertIsDisplayed()
    composeTestRule.onNodeWithTag("multi_select_min").assertIsDisplayed()
    composeTestRule.onNodeWithTag("multi_select_max").assertIsDisplayed()
    composeTestRule.onNodeWithTag("multi_select_allow_custom").assertIsDisplayed()
  }

  @Test
  fun multiSelectFieldConfiguration_minSelectionsInputUpdatesType() {
    val options = listOf(SelectOption("opt1", "Option 1"), SelectOption("opt2", "Option 2"))
    var updatedType: FieldType.MultiSelect? = null
    composeTestRule.setContent {
      MultiSelectFieldConfiguration(
          fieldType = FieldType.MultiSelect(options),
          onUpdate = { updatedType = it },
          enabled = true)
    }

    composeTestRule.onNodeWithTag("multi_select_min").performTextInput("2")
    assertNotNull(updatedType)
    assertEquals(2, updatedType?.minSelections)
  }
}
