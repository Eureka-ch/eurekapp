package ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.fieldtypes

/* Portions of this code were generated with the help of Claude Sonnet 4.5. */

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import ch.eureka.eurekapp.model.data.template.field.FieldType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

/**
 * Android UI tests for NumberFieldConfiguration.
 *
 * Portions of this code were generated with the help of Claude Sonnet 4.5.
 */
class NumberFieldConfigurationTest {

  @get:Rule val composeTestRule = createComposeRule()

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
  fun numberFieldConfiguration_minInput_updatesType() {
    var updatedType: FieldType.Number? = null
    composeTestRule.setContent {
      NumberFieldConfiguration(
          fieldType = FieldType.Number(), onUpdate = { updatedType = it }, enabled = true)
    }

    composeTestRule.onNodeWithTag("number_min").performTextInput("10.5")
    assertNotNull(updatedType)
    assertEquals(10.5, updatedType?.min)
  }

  @Test
  fun numberFieldConfiguration_maxInput_updatesType() {
    var updatedType: FieldType.Number? = null
    composeTestRule.setContent {
      NumberFieldConfiguration(
          fieldType = FieldType.Number(), onUpdate = { updatedType = it }, enabled = true)
    }

    composeTestRule.onNodeWithTag("number_max").performTextInput("100.0")
    assertNotNull(updatedType)
    assertEquals(100.0, updatedType?.max)
  }

  @Test
  fun numberFieldConfiguration_stepInput_updatesType() {
    var updatedType: FieldType.Number? = null
    composeTestRule.setContent {
      NumberFieldConfiguration(
          fieldType = FieldType.Number(), onUpdate = { updatedType = it }, enabled = true)
    }

    composeTestRule.onNodeWithTag("number_step").performTextInput("0.5")
    assertNotNull(updatedType)
    assertEquals(0.5, updatedType?.step)
  }

  @Test
  fun numberFieldConfiguration_decimalsInput_updatesType() {
    var updatedType: FieldType.Number? = null
    composeTestRule.setContent {
      NumberFieldConfiguration(
          fieldType = FieldType.Number(), onUpdate = { updatedType = it }, enabled = true)
    }

    composeTestRule.onNodeWithTag("number_decimals").performTextInput("2")
    assertNotNull(updatedType)
    assertEquals(2, updatedType?.decimals)
  }

  @Test
  fun numberFieldConfiguration_unitInput_updatesType() {
    var updatedType: FieldType.Number? = null
    composeTestRule.setContent {
      NumberFieldConfiguration(
          fieldType = FieldType.Number(), onUpdate = { updatedType = it }, enabled = true)
    }

    composeTestRule.onNodeWithTag("number_unit").performTextInput("kg")
    assertNotNull(updatedType)
    assertEquals("kg", updatedType?.unit)
  }

  @Test
  fun numberFieldConfiguration_minGreaterThanMax_showsError() {
    composeTestRule.setContent {
      NumberFieldConfiguration(
          fieldType = FieldType.Number(min = 100.0, max = 50.0), onUpdate = {}, enabled = true)
    }

    composeTestRule
        .onNodeWithText("Minimum value must be less than or equal to maximum value")
        .assertIsDisplayed()
  }

  @Test
  fun numberFieldConfiguration_minLessThanMax_noError() {
    composeTestRule.setContent {
      NumberFieldConfiguration(
          fieldType = FieldType.Number(min = 10.0, max = 100.0), onUpdate = {}, enabled = true)
    }

    composeTestRule
        .onNodeWithText("Minimum value must be less than or equal to maximum value")
        .assertDoesNotExist()
  }

  @Test
  fun numberFieldConfiguration_minEqualToMax_noError() {
    composeTestRule.setContent {
      NumberFieldConfiguration(
          fieldType = FieldType.Number(min = 50.0, max = 50.0), onUpdate = {}, enabled = true)
    }

    composeTestRule
        .onNodeWithText("Minimum value must be less than or equal to maximum value")
        .assertDoesNotExist()
  }

  @Test
  fun numberFieldConfiguration_negativeMin_accepted() {
    var updatedType: FieldType.Number? = null
    composeTestRule.setContent {
      NumberFieldConfiguration(
          fieldType = FieldType.Number(), onUpdate = { updatedType = it }, enabled = true)
    }

    composeTestRule.onNodeWithTag("number_min").performTextInput("-25.5")
    assertNotNull(updatedType)
    assertEquals(-25.5, updatedType?.min)
  }

  @Test
  fun numberFieldConfiguration_emptyMin_setsToNull() {
    var updatedType: FieldType.Number? = null
    composeTestRule.setContent {
      NumberFieldConfiguration(
          fieldType = FieldType.Number(min = 10.0), onUpdate = { updatedType = it }, enabled = true)
    }

    // Clearing the field sets it to empty string, which parses to null
    composeTestRule.onNodeWithTag("number_min").performTextInput("")
    assertNotNull(updatedType)
    assertNull(updatedType?.min)
  }

  @Test
  fun numberFieldConfiguration_invalidDecimalsInput_fallsBackToZero() {
    var updatedType: FieldType.Number? = null
    composeTestRule.setContent {
      NumberFieldConfiguration(
          fieldType = FieldType.Number(), onUpdate = { updatedType = it }, enabled = true)
    }

    composeTestRule.onNodeWithTag("number_decimals").performTextInput("abc")
    assertNotNull(updatedType)
    assertEquals(0, updatedType?.decimals)
  }

  @Test
  fun numberFieldConfiguration_blankUnit_setsToNull() {
    var updatedType: FieldType.Number? = null
    composeTestRule.setContent {
      NumberFieldConfiguration(
          fieldType = FieldType.Number(unit = "kg"),
          onUpdate = { updatedType = it },
          enabled = true)
    }

    composeTestRule.onNodeWithTag("number_unit").performTextInput("   ")
    assertNotNull(updatedType)
    assertNull(updatedType?.unit)
  }

  @Test
  fun numberFieldConfiguration_disabled_allFieldsDisabled() {
    composeTestRule.setContent {
      NumberFieldConfiguration(fieldType = FieldType.Number(), onUpdate = {}, enabled = false)
    }

    composeTestRule.onNodeWithTag("number_min").assertIsNotEnabled()
    composeTestRule.onNodeWithTag("number_max").assertIsNotEnabled()
    composeTestRule.onNodeWithTag("number_step").assertIsNotEnabled()
    composeTestRule.onNodeWithTag("number_decimals").assertIsNotEnabled()
    composeTestRule.onNodeWithTag("number_unit").assertIsNotEnabled()
  }

  @Test
  fun numberFieldConfiguration_zeroDecimals_accepted() {
    var updatedType: FieldType.Number? = null
    composeTestRule.setContent {
      NumberFieldConfiguration(
          fieldType = FieldType.Number(), onUpdate = { updatedType = it }, enabled = true)
    }

    composeTestRule.onNodeWithTag("number_decimals").performTextInput("0")
    assertNotNull(updatedType)
    assertEquals(0, updatedType?.decimals)
  }
}
