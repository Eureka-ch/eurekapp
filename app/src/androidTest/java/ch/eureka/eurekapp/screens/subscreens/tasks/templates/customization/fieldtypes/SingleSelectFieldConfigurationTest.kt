package ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.fieldtypes

/* Portions of this code were generated with the help of Claude Sonnet 4.5. */

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.model.data.template.field.SelectOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Android UI tests for SingleSelectFieldConfiguration.
 *
 * Portions of this code were generated with the help of Claude Sonnet 4.5.
 */
class SingleSelectFieldConfigurationTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val testOptions =
      listOf(SelectOption("opt1", "Option 1"), SelectOption("opt2", "Option 2"))

  @Test
  fun singleSelectFieldConfiguration_displaysAllFields() {
    composeTestRule.setContent {
      SingleSelectFieldConfiguration(
          fieldType = FieldType.SingleSelect(testOptions), onUpdate = {}, enabled = true)
    }

    composeTestRule.onNodeWithTag("single_select_allow_custom").assertIsDisplayed()
    composeTestRule.onNodeWithText("Allow Custom Values").assertIsDisplayed()
  }

  @Test
  fun singleSelectFieldConfiguration_allowCustomCheckbox_togglesToTrue() {
    var updatedType: FieldType.SingleSelect? = null
    composeTestRule.setContent {
      SingleSelectFieldConfiguration(
          fieldType = FieldType.SingleSelect(testOptions, allowCustom = false),
          onUpdate = { updatedType = it },
          enabled = true)
    }

    composeTestRule.onNodeWithTag("single_select_allow_custom").performClick()
    assertNotNull(updatedType)
    assertTrue(updatedType?.allowCustom == true)
  }

  @Test
  fun singleSelectFieldConfiguration_allowCustomCheckbox_togglesToFalse() {
    var updatedType: FieldType.SingleSelect? = null
    composeTestRule.setContent {
      SingleSelectFieldConfiguration(
          fieldType = FieldType.SingleSelect(testOptions, allowCustom = true),
          onUpdate = { updatedType = it },
          enabled = true)
    }

    composeTestRule.onNodeWithTag("single_select_allow_custom").performClick()
    assertNotNull(updatedType)
    assertFalse(updatedType?.allowCustom == true)
  }

  @Test
  fun singleSelectFieldConfiguration_disabled_checkboxDisabled() {
    composeTestRule.setContent {
      SingleSelectFieldConfiguration(
          fieldType = FieldType.SingleSelect(testOptions), onUpdate = {}, enabled = false)
    }

    composeTestRule.onNodeWithTag("single_select_allow_custom").assertIsNotEnabled()
  }

  @Test
  fun singleSelectFieldConfiguration_allowCustomTrue_checkboxChecked() {
    composeTestRule.setContent {
      SingleSelectFieldConfiguration(
          fieldType = FieldType.SingleSelect(testOptions, allowCustom = true),
          onUpdate = {},
          enabled = true)
    }

    composeTestRule.onNodeWithTag("single_select_allow_custom").assertIsOn()
  }

  @Test
  fun singleSelectFieldConfiguration_allowCustomFalse_checkboxUnchecked() {
    composeTestRule.setContent {
      SingleSelectFieldConfiguration(
          fieldType = FieldType.SingleSelect(testOptions, allowCustom = false),
          onUpdate = {},
          enabled = true)
    }

    composeTestRule.onNodeWithTag("single_select_allow_custom").assertIsOff()
  }

  @Test
  fun singleSelectFieldConfiguration_withManyOptions_displays() {
    val manyOptions = (1..10).map { SelectOption("opt$it", "Option $it") }
    composeTestRule.setContent {
      SingleSelectFieldConfiguration(
          fieldType = FieldType.SingleSelect(manyOptions), onUpdate = {}, enabled = true)
    }

    composeTestRule.onNodeWithTag("single_select_allow_custom").assertIsDisplayed()
  }

  @Test
  fun singleSelectFieldConfiguration_preservesOptions_afterToggle() {
    var updatedType: FieldType.SingleSelect? = null
    composeTestRule.setContent {
      SingleSelectFieldConfiguration(
          fieldType = FieldType.SingleSelect(testOptions, allowCustom = false),
          onUpdate = { updatedType = it },
          enabled = true)
    }

    composeTestRule.onNodeWithTag("single_select_allow_custom").performClick()
    assertNotNull(updatedType)
    assertEquals(testOptions, updatedType?.options)
  }
}
