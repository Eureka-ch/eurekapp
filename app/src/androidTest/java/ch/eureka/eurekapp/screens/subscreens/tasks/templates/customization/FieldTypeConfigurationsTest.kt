// Portions of this file were written with the help of Grok.
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
import ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.fieldtypes.DateFieldConfigurationTestTags
import ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.fieldtypes.MultiSelectFieldConfiguration
import ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.fieldtypes.MultiSelectFieldConfigurationTestTags
import ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.fieldtypes.NumberFieldConfiguration
import ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.fieldtypes.NumberFieldConfigurationTestTags
import ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.fieldtypes.SingleSelectFieldConfiguration
import ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.fieldtypes.SingleSelectFieldConfigurationTestTags
import ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.fieldtypes.TextFieldConfiguration
import ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.fieldtypes.TextFieldConfigurationTestTags
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
  fun fieldTypeConfigurations_textFieldConfigurationDisplaysAllFields() {
    composeTestRule.setContent {
      TextFieldConfiguration(fieldType = FieldType.Text(), onUpdate = {}, enabled = true)
    }

    composeTestRule.onNodeWithTag(TextFieldConfigurationTestTags.MAX_LENGTH).assertIsDisplayed()
    composeTestRule.onNodeWithTag(TextFieldConfigurationTestTags.MIN_LENGTH).assertIsDisplayed()
    composeTestRule.onNodeWithTag(TextFieldConfigurationTestTags.PLACEHOLDER).assertIsDisplayed()
  }

  @Test
  fun fieldTypeConfigurations_textFieldConfigurationMaxLengthInputUpdatesType() {
    var updatedType: FieldType.Text? = null
    composeTestRule.setContent {
      TextFieldConfiguration(
          fieldType = FieldType.Text(), onUpdate = { updatedType = it }, enabled = true)
    }

    composeTestRule.onNodeWithTag(TextFieldConfigurationTestTags.MAX_LENGTH).performTextInput("100")
    assertNotNull(updatedType)
    assertEquals(100, updatedType?.maxLength)
  }

  @Test
  fun fieldTypeConfigurations_numberFieldConfigurationDisplaysAllFields() {
    composeTestRule.setContent {
      NumberFieldConfiguration(fieldType = FieldType.Number(), onUpdate = {}, enabled = true)
    }

    composeTestRule.onNodeWithTag(NumberFieldConfigurationTestTags.MIN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NumberFieldConfigurationTestTags.MAX).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NumberFieldConfigurationTestTags.STEP).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NumberFieldConfigurationTestTags.DECIMALS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NumberFieldConfigurationTestTags.UNIT).assertIsDisplayed()
  }

  @Test
  fun fieldTypeConfigurations_numberFieldConfigurationMinInputUpdatesType() {
    var updatedType: FieldType.Number? = null
    composeTestRule.setContent {
      NumberFieldConfiguration(
          fieldType = FieldType.Number(), onUpdate = { updatedType = it }, enabled = true)
    }

    composeTestRule.onNodeWithTag(NumberFieldConfigurationTestTags.MIN).performTextInput("10")
    assertNotNull(updatedType)
    assertEquals(10.0, updatedType!!.min!!, 0.01)
  }

  @Test
  fun fieldTypeConfigurations_dateFieldConfigurationDisplaysAllFields() {
    composeTestRule.setContent {
      DateFieldConfiguration(fieldType = FieldType.Date(), onUpdate = {}, enabled = true)
    }

    composeTestRule.onNodeWithTag(DateFieldConfigurationTestTags.INCLUDE_TIME).assertIsDisplayed()
    composeTestRule.onNodeWithTag(DateFieldConfigurationTestTags.FORMAT).assertIsDisplayed()
  }

  @Test
  fun fieldTypeConfigurations_dateFieldConfigurationIncludeTimeCheckboxUpdatesType() {
    var updatedType: FieldType.Date? = null
    composeTestRule.setContent {
      DateFieldConfiguration(
          fieldType = FieldType.Date(includeTime = false),
          onUpdate = { updatedType = it },
          enabled = true)
    }

    composeTestRule.onNodeWithTag(DateFieldConfigurationTestTags.INCLUDE_TIME).performClick()
    assertNotNull(updatedType)
    assertEquals(true, updatedType?.includeTime)
  }

  @Test
  fun fieldTypeConfigurations_singleSelectFieldConfigurationDisplaysAllowCustomCheckbox() {
    val options = listOf(SelectOption("opt1", "Option 1"), SelectOption("opt2", "Option 2"))
    composeTestRule.setContent {
      SingleSelectFieldConfiguration(
          fieldType = FieldType.SingleSelect(options), onUpdate = {}, enabled = true)
    }

    composeTestRule.onNodeWithText("Allow Custom Values").assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(SingleSelectFieldConfigurationTestTags.ALLOW_CUSTOM)
        .assertIsDisplayed()
  }

  @Test
  fun fieldTypeConfigurations_singleSelectFieldConfigurationAllowCustomCheckboxUpdatesType() {
    val options = listOf(SelectOption("opt1", "Option 1"), SelectOption("opt2", "Option 2"))
    var updatedType: FieldType.SingleSelect? = null
    composeTestRule.setContent {
      SingleSelectFieldConfiguration(
          fieldType = FieldType.SingleSelect(options, allowCustom = false),
          onUpdate = { updatedType = it },
          enabled = true)
    }

    composeTestRule
        .onNodeWithTag(SingleSelectFieldConfigurationTestTags.ALLOW_CUSTOM)
        .performClick()
    assertNotNull(updatedType)
    assertEquals(true, updatedType?.allowCustom)
  }

  @Test
  fun fieldTypeConfigurations_multiSelectFieldConfigurationDisplaysAllFields() {
    val options = listOf(SelectOption("opt1", "Option 1"), SelectOption("opt2", "Option 2"))
    composeTestRule.setContent {
      MultiSelectFieldConfiguration(
          fieldType = FieldType.MultiSelect(options), onUpdate = {}, enabled = true)
    }

    composeTestRule.onNodeWithText("Allow Custom Values").assertIsDisplayed()
    composeTestRule.onNodeWithTag(MultiSelectFieldConfigurationTestTags.MIN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(MultiSelectFieldConfigurationTestTags.MAX).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(MultiSelectFieldConfigurationTestTags.ALLOW_CUSTOM)
        .assertIsDisplayed()
  }

  @Test
  fun fieldTypeConfigurations_multiSelectFieldConfigurationMinSelectionsInputUpdatesType() {
    val options = listOf(SelectOption("opt1", "Option 1"), SelectOption("opt2", "Option 2"))
    var updatedType: FieldType.MultiSelect? = null
    composeTestRule.setContent {
      MultiSelectFieldConfiguration(
          fieldType = FieldType.MultiSelect(options),
          onUpdate = { updatedType = it },
          enabled = true)
    }

    composeTestRule.onNodeWithTag(MultiSelectFieldConfigurationTestTags.MIN).performTextInput("2")
    assertNotNull(updatedType)
    assertEquals(2, updatedType?.minSelections)
  }
}
