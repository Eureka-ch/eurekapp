package ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.fieldtypes

/* Portions of this code were generated with the help of Claude Sonnet 4.5. */

import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.utils.BaseFieldConfigurationTest
import org.junit.Test

/**
 * Android UI tests for DateFieldConfiguration.
 *
 * Portions of this code were generated with the help of Claude Sonnet 4.5.
 */
class DateFieldConfigurationTest : BaseFieldConfigurationTest() {

  @Test
  fun dateFieldConfiguration_displaysAllFields() {
    utils.testDisplaysAllFields(
        composeTestRule,
        content = {
          DateFieldConfiguration(fieldType = FieldType.Date(), onUpdate = {}, enabled = true)
        },
        "date_min",
        "date_max",
        "date_include_time",
        "date_format")
  }

  @Test
  fun dateFieldConfiguration_minDateInput_updatesType() {
    val updates = mutableListOf<FieldType.Date>()

    utils.testInputUpdate(
        composeTestRule,
        content = { onUpdate ->
          DateFieldConfiguration(fieldType = FieldType.Date(), onUpdate = onUpdate, enabled = true)
        },
        capturedUpdate = updates,
        testTag = "date_min",
        inputValue = "2025-01-01") { updated ->
          assertions.assertPropertyEquals("2025-01-01", updated, { it.minDate }, "minDate")
        }
  }

  @Test
  fun dateFieldConfiguration_maxDateInput_updatesType() {
    val updates = mutableListOf<FieldType.Date>()

    utils.testInputUpdate(
        composeTestRule,
        content = { onUpdate ->
          DateFieldConfiguration(fieldType = FieldType.Date(), onUpdate = onUpdate, enabled = true)
        },
        capturedUpdate = updates,
        testTag = "date_max",
        inputValue = "2025-12-31") { updated ->
          assertions.assertPropertyEquals("2025-12-31", updated, { it.maxDate }, "maxDate")
        }
  }

  @Test
  fun dateFieldConfiguration_includeTimeCheckbox_updatesType() {
    val updates = mutableListOf<FieldType.Date>()

    utils.testCheckboxToggle(
        composeTestRule,
        content = { onUpdate ->
          DateFieldConfiguration(
              fieldType = FieldType.Date(includeTime = false), onUpdate = onUpdate, enabled = true)
        },
        capturedUpdate = updates,
        checkboxTag = "date_include_time") { updated ->
          assertions.assertBooleanTrue(updated, { it.includeTime }, "includeTime should be true")
        }
  }

  @Test
  fun dateFieldConfiguration_invalidFormat_showsError() {
    this.composeTestRule.setContent {
      DateFieldConfiguration(
          fieldType = FieldType.Date(format = "invalid-format"), onUpdate = {}, enabled = true)
    }

    utils.assertErrorDisplayed(composeTestRule, "Invalid date format pattern")
  }

  @Test
  fun dateFieldConfiguration_validFormat_noError() {
    this.composeTestRule.setContent {
      DateFieldConfiguration(
          fieldType = FieldType.Date(format = "yyyy-MM-dd"), onUpdate = {}, enabled = true)
    }

    utils.assertNoError(composeTestRule, "Invalid date format pattern")
  }

  @Test
  fun dateFieldConfiguration_disabled_allFieldsDisabled() {
    utils.testDisabledState(
        composeTestRule,
        content = {
          DateFieldConfiguration(fieldType = FieldType.Date(), onUpdate = {}, enabled = false)
        },
        "date_min",
        "date_max",
        "date_include_time",
        "date_format")
  }

  @Test
  fun dateFieldConfiguration_includeTimeUnchecked_updatesType() {
    val updates = mutableListOf<FieldType.Date>()

    utils.testCheckboxToggle(
        composeTestRule,
        content = { onUpdate ->
          DateFieldConfiguration(
              fieldType = FieldType.Date(includeTime = true), onUpdate = onUpdate, enabled = true)
        },
        capturedUpdate = updates,
        checkboxTag = "date_include_time") { updated ->
          assertions.assertBooleanFalse(updated, { it.includeTime }, "includeTime should be false")
        }
  }

  @Test
  fun dateFieldConfiguration_withInitialMinDate_displays() {
    utils.testDisplaysAllFields(
        composeTestRule,
        content = {
          DateFieldConfiguration(
              fieldType = FieldType.Date(minDate = "2025-01-01"), onUpdate = {}, enabled = true)
        },
        "date_min")
  }

  @Test
  fun dateFieldConfiguration_withInitialMaxDate_displays() {
    utils.testDisplaysAllFields(
        composeTestRule,
        content = {
          DateFieldConfiguration(
              fieldType = FieldType.Date(maxDate = "2025-12-31"), onUpdate = {}, enabled = true)
        },
        "date_max")
  }

  @Test
  fun dateFieldConfiguration_withBothMinAndMaxDates_displays() {
    utils.testDisplaysAllFields(
        composeTestRule,
        content = {
          DateFieldConfiguration(
              fieldType = FieldType.Date(minDate = "2025-01-01", maxDate = "2025-12-31"),
              onUpdate = {},
              enabled = true)
        },
        "date_min",
        "date_max")
  }

  @Test
  fun dateFieldConfiguration_customFormat_displays() {
    utils.testDisplaysAllFields(
        composeTestRule,
        content = {
          DateFieldConfiguration(
              fieldType = FieldType.Date(format = "MM/dd/yyyy"), onUpdate = {}, enabled = true)
        },
        "date_format")
  }

  @Test
  fun dateFieldConfiguration_blankMinDate_setsToNull() {
    val updates = mutableListOf<FieldType.Date>()
    this.composeTestRule.setContent {
      DateFieldConfiguration(
          fieldType = FieldType.Date(minDate = "2025-01-01"),
          onUpdate = { updates.add(it) },
          enabled = true)
    }

    utils.clearText(composeTestRule, "date_min")
    assertions.assertPropertyNull(updates.lastOrNull(), { it.minDate }, "minDate")
  }

  @Test
  fun dateFieldConfiguration_blankMaxDate_setsToNull() {
    val updates = mutableListOf<FieldType.Date>()
    this.composeTestRule.setContent {
      DateFieldConfiguration(
          fieldType = FieldType.Date(maxDate = "2025-12-31"),
          onUpdate = { updates.add(it) },
          enabled = true)
    }

    utils.clearText(composeTestRule, "date_max")
    assertions.assertPropertyNull(updates.lastOrNull(), { it.maxDate }, "maxDate")
  }
}
