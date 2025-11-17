package ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.fieldtypes

/* Portions of this code were generated with the help of Claude Sonnet 4.5. */

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.model.data.template.field.SelectOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Android UI tests for MultiSelectFieldConfiguration.
 *
 * Portions of this code were generated with the help of Claude Sonnet 4.5.
 */
class MultiSelectFieldConfigurationTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val testOptions =
      listOf(SelectOption("opt1", "Option 1"), SelectOption("opt2", "Option 2"))

  @Test
  fun multiSelectFieldConfiguration_displaysAllFields() {
    composeTestRule.setContent {
      MultiSelectFieldConfiguration(
          fieldType = FieldType.MultiSelect(testOptions), onUpdate = {}, enabled = true)
    }

    composeTestRule.onNodeWithTag("multi_select_min").assertIsDisplayed()
    composeTestRule.onNodeWithTag("multi_select_max").assertIsDisplayed()
    composeTestRule.onNodeWithTag("multi_select_allow_custom").assertIsDisplayed()
    composeTestRule.onNodeWithText("Allow Custom Values").assertIsDisplayed()
  }

  @Test
  fun multiSelectFieldConfiguration_minSelectionsInput_updatesType() {
    var updatedType: FieldType.MultiSelect? = null
    composeTestRule.setContent {
      MultiSelectFieldConfiguration(
          fieldType = FieldType.MultiSelect(testOptions),
          onUpdate = { updatedType = it },
          enabled = true)
    }

    composeTestRule.onNodeWithTag("multi_select_min").performTextInput("2")
    assertNotNull(updatedType)
    assertEquals(2, updatedType?.minSelections)
  }

  @Test
  fun multiSelectFieldConfiguration_maxSelectionsInput_updatesType() {
    var updatedType: FieldType.MultiSelect? = null
    composeTestRule.setContent {
      MultiSelectFieldConfiguration(
          fieldType = FieldType.MultiSelect(testOptions),
          onUpdate = { updatedType = it },
          enabled = true)
    }

    composeTestRule.onNodeWithTag("multi_select_max").performTextInput("3")
    assertNotNull(updatedType)
    assertEquals(3, updatedType?.maxSelections)
  }

  @Test
  fun multiSelectFieldConfiguration_allowCustomCheckbox_updatesType() {
    var updatedType: FieldType.MultiSelect? = null
    composeTestRule.setContent {
      MultiSelectFieldConfiguration(
          fieldType = FieldType.MultiSelect(testOptions, allowCustom = false),
          onUpdate = { updatedType = it },
          enabled = true)
    }

    composeTestRule.onNodeWithTag("multi_select_allow_custom").performClick()
    assertNotNull(updatedType)
    assertTrue(updatedType?.allowCustom == true)
  }

  @Test
  fun multiSelectFieldConfiguration_minGreaterThanMax_showsError() {
    composeTestRule.setContent {
      MultiSelectFieldConfiguration(
          fieldType = FieldType.MultiSelect(testOptions, minSelections = 5, maxSelections = 2),
          onUpdate = {},
          enabled = true)
    }

    composeTestRule
        .onNodeWithText("Minimum selections must be less than or equal to maximum selections")
        .assertIsDisplayed()
  }

  @Test
  fun multiSelectFieldConfiguration_minLessThanMax_noError() {
    composeTestRule.setContent {
      MultiSelectFieldConfiguration(
          fieldType = FieldType.MultiSelect(testOptions, minSelections = 1, maxSelections = 3),
          onUpdate = {},
          enabled = true)
    }

    composeTestRule
        .onNodeWithText("Minimum selections must be less than or equal to maximum selections")
        .assertDoesNotExist()
  }

  @Test
  fun multiSelectFieldConfiguration_minEqualToMax_noError() {
    composeTestRule.setContent {
      MultiSelectFieldConfiguration(
          fieldType = FieldType.MultiSelect(testOptions, minSelections = 2, maxSelections = 2),
          onUpdate = {},
          enabled = true)
    }

    composeTestRule
        .onNodeWithText("Minimum selections must be less than or equal to maximum selections")
        .assertDoesNotExist()
  }

  @Test
  fun multiSelectFieldConfiguration_disabled_allFieldsDisabled() {
    composeTestRule.setContent {
      MultiSelectFieldConfiguration(
          fieldType = FieldType.MultiSelect(testOptions), onUpdate = {}, enabled = false)
    }

    composeTestRule.onNodeWithTag("multi_select_min").assertIsNotEnabled()
    composeTestRule.onNodeWithTag("multi_select_max").assertIsNotEnabled()
    composeTestRule.onNodeWithTag("multi_select_allow_custom").assertIsNotEnabled()
  }

  @Test
  fun multiSelectFieldConfiguration_emptyMinSelections_setsToNull() {
    var updatedType: FieldType.MultiSelect? = null
    composeTestRule.setContent {
      MultiSelectFieldConfiguration(
          fieldType = FieldType.MultiSelect(testOptions, minSelections = 2),
          onUpdate = { updatedType = it },
          enabled = true)
    }

    composeTestRule.onNodeWithTag("multi_select_min").performTextInput("")
    assertNotNull(updatedType)
    assertNull(updatedType?.minSelections)
  }

  @Test
  fun multiSelectFieldConfiguration_allowCustomUnchecked_updatesType() {
    var updatedType: FieldType.MultiSelect? = null
    composeTestRule.setContent {
      MultiSelectFieldConfiguration(
          fieldType = FieldType.MultiSelect(testOptions, allowCustom = true),
          onUpdate = { updatedType = it },
          enabled = true)
    }

    composeTestRule.onNodeWithTag("multi_select_allow_custom").performClick()
    assertNotNull(updatedType)
    assertFalse(updatedType?.allowCustom == true)
  }
}
