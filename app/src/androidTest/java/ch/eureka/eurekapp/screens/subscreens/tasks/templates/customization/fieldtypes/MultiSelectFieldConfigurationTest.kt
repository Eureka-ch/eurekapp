package ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.fieldtypes

/* Portions of this code were generated with the help of Claude Sonnet 4.5. */

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
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
    val updates = mutableListOf<FieldType.MultiSelect>()
    composeTestRule.setContent {
      MultiSelectFieldConfiguration(
          fieldType = FieldType.MultiSelect(testOptions, minSelections = 2),
          onUpdate = { updates.add(it) },
          enabled = true)
    }

    composeTestRule.onNodeWithTag("multi_select_min").performTextClearance()
    assertTrue(updates.isNotEmpty())
    assertNull(updates.last().minSelections)
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

  @Test
  fun multiSelectFieldConfiguration_emptyMaxSelections_setsToNull() {
    val updates = mutableListOf<FieldType.MultiSelect>()
    composeTestRule.setContent {
      MultiSelectFieldConfiguration(
          fieldType = FieldType.MultiSelect(testOptions, maxSelections = 3),
          onUpdate = { updates.add(it) },
          enabled = true)
    }

    composeTestRule.onNodeWithTag("multi_select_max").performTextClearance()
    assertTrue(updates.isNotEmpty())
    assertNull(updates.last().maxSelections)
  }

  @Test
  fun multiSelectFieldConfiguration_withInitialMinSelections_displays() {
    composeTestRule.setContent {
      MultiSelectFieldConfiguration(
          fieldType = FieldType.MultiSelect(testOptions, minSelections = 1),
          onUpdate = {},
          enabled = true)
    }

    composeTestRule.onNodeWithTag("multi_select_min").assertIsDisplayed()
  }

  @Test
  fun multiSelectFieldConfiguration_withInitialMaxSelections_displays() {
    composeTestRule.setContent {
      MultiSelectFieldConfiguration(
          fieldType = FieldType.MultiSelect(testOptions, maxSelections = 3),
          onUpdate = {},
          enabled = true)
    }

    composeTestRule.onNodeWithTag("multi_select_max").assertIsDisplayed()
  }

  @Test
  fun multiSelectFieldConfiguration_preservesOptions_afterUpdate() {
    var updatedType: FieldType.MultiSelect? = null
    composeTestRule.setContent {
      MultiSelectFieldConfiguration(
          fieldType = FieldType.MultiSelect(testOptions),
          onUpdate = { updatedType = it },
          enabled = true)
    }

    composeTestRule.onNodeWithTag("multi_select_min").performTextInput("1")
    assertNotNull(updatedType)
    assertEquals(testOptions, updatedType?.options)
  }

  @Test
  fun multiSelectFieldConfiguration_withEmptyOptions_displays() {
    composeTestRule.setContent {
      MultiSelectFieldConfiguration(
          fieldType = FieldType.MultiSelect(emptyList()), onUpdate = {}, enabled = true)
    }

    composeTestRule.onNodeWithTag("multi_select_min").assertIsDisplayed()
    composeTestRule.onNodeWithTag("multi_select_max").assertIsDisplayed()
    composeTestRule.onNodeWithTag("multi_select_allow_custom").assertIsDisplayed()
  }
}
