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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Android UI tests for DateFieldConfiguration.
 *
 * Portions of this code were generated with the help of Claude Sonnet 4.5.
 */
class DateFieldConfigurationTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun dateFieldConfiguration_displaysAllFields() {
    composeTestRule.setContent {
      DateFieldConfiguration(fieldType = FieldType.Date(), onUpdate = {}, enabled = true)
    }

    composeTestRule.onNodeWithTag("date_min").assertIsDisplayed()
    composeTestRule.onNodeWithTag("date_max").assertIsDisplayed()
    composeTestRule.onNodeWithTag("date_include_time").assertIsDisplayed()
    composeTestRule.onNodeWithTag("date_format").assertIsDisplayed()
  }

  @Test
  fun dateFieldConfiguration_minDateInput_updatesType() {
    var updatedType: FieldType.Date? = null
    composeTestRule.setContent {
      DateFieldConfiguration(
          fieldType = FieldType.Date(), onUpdate = { updatedType = it }, enabled = true)
    }

    composeTestRule.onNodeWithTag("date_min").performTextInput("2025-01-01")
    assertNotNull(updatedType)
    assertEquals("2025-01-01", updatedType?.minDate)
  }

  @Test
  fun dateFieldConfiguration_maxDateInput_updatesType() {
    var updatedType: FieldType.Date? = null
    composeTestRule.setContent {
      DateFieldConfiguration(
          fieldType = FieldType.Date(), onUpdate = { updatedType = it }, enabled = true)
    }

    composeTestRule.onNodeWithTag("date_max").performTextInput("2025-12-31")
    assertNotNull(updatedType)
    assertEquals("2025-12-31", updatedType?.maxDate)
  }

  @Test
  fun dateFieldConfiguration_includeTimeCheckbox_updatesType() {
    var updatedType: FieldType.Date? = null
    composeTestRule.setContent {
      DateFieldConfiguration(
          fieldType = FieldType.Date(includeTime = false),
          onUpdate = { updatedType = it },
          enabled = true)
    }

    composeTestRule.onNodeWithTag("date_include_time").performClick()
    assertNotNull(updatedType)
    assertTrue(updatedType?.includeTime == true)
  }

  @Test
  fun dateFieldConfiguration_invalidFormat_showsError() {
    composeTestRule.setContent {
      DateFieldConfiguration(
          fieldType = FieldType.Date(format = "invalid-format"), onUpdate = {}, enabled = true)
    }

    composeTestRule.onNodeWithText("Invalid date format pattern").assertIsDisplayed()
  }

  @Test
  fun dateFieldConfiguration_validFormat_noError() {
    composeTestRule.setContent {
      DateFieldConfiguration(
          fieldType = FieldType.Date(format = "yyyy-MM-dd"), onUpdate = {}, enabled = true)
    }

    composeTestRule.onNodeWithText("Invalid date format pattern").assertDoesNotExist()
  }

  @Test
  fun dateFieldConfiguration_disabled_allFieldsDisabled() {
    composeTestRule.setContent {
      DateFieldConfiguration(fieldType = FieldType.Date(), onUpdate = {}, enabled = false)
    }

    composeTestRule.onNodeWithTag("date_min").assertIsNotEnabled()
    composeTestRule.onNodeWithTag("date_max").assertIsNotEnabled()
    composeTestRule.onNodeWithTag("date_include_time").assertIsNotEnabled()
    composeTestRule.onNodeWithTag("date_format").assertIsNotEnabled()
  }

  @Test
  fun dateFieldConfiguration_includeTimeUnchecked_updatesType() {
    var updatedType: FieldType.Date? = null
    composeTestRule.setContent {
      DateFieldConfiguration(
          fieldType = FieldType.Date(includeTime = true),
          onUpdate = { updatedType = it },
          enabled = true)
    }

    composeTestRule.onNodeWithTag("date_include_time").performClick()
    assertNotNull(updatedType)
    assertFalse(updatedType?.includeTime == true)
  }

  @Test
  fun dateFieldConfiguration_withInitialMinDate_displays() {
    composeTestRule.setContent {
      DateFieldConfiguration(
          fieldType = FieldType.Date(minDate = "2025-01-01"), onUpdate = {}, enabled = true)
    }

    composeTestRule.onNodeWithTag("date_min").assertIsDisplayed()
  }

  @Test
  fun dateFieldConfiguration_withInitialMaxDate_displays() {
    composeTestRule.setContent {
      DateFieldConfiguration(
          fieldType = FieldType.Date(maxDate = "2025-12-31"), onUpdate = {}, enabled = true)
    }

    composeTestRule.onNodeWithTag("date_max").assertIsDisplayed()
  }

  @Test
  fun dateFieldConfiguration_withBothMinAndMaxDates_displays() {
    composeTestRule.setContent {
      DateFieldConfiguration(
          fieldType = FieldType.Date(minDate = "2025-01-01", maxDate = "2025-12-31"),
          onUpdate = {},
          enabled = true)
    }

    composeTestRule.onNodeWithTag("date_min").assertIsDisplayed()
    composeTestRule.onNodeWithTag("date_max").assertIsDisplayed()
  }

  @Test
  fun dateFieldConfiguration_customFormat_displays() {
    composeTestRule.setContent {
      DateFieldConfiguration(
          fieldType = FieldType.Date(format = "MM/dd/yyyy"), onUpdate = {}, enabled = true)
    }

    composeTestRule.onNodeWithTag("date_format").assertIsDisplayed()
  }
}
