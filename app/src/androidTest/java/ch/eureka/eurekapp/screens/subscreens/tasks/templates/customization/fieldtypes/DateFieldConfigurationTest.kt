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
        DateFieldConfigurationTestTags.MIN,
        DateFieldConfigurationTestTags.MAX,
        DateFieldConfigurationTestTags.INCLUDE_TIME,
        DateFieldConfigurationTestTags.FORMAT)
  }

  @Test
  fun dateFieldConfiguration_minDateInputUpdatesType() {
    val updates = mutableListOf<FieldType.Date>()

    utils.testInputUpdate(
        composeTestRule,
        content = { onUpdate ->
          DateFieldConfiguration(fieldType = FieldType.Date(), onUpdate = onUpdate, enabled = true)
        },
        capturedUpdate = updates,
        testTag = DateFieldConfigurationTestTags.MIN,
        inputValue = "2025-01-01") { updated ->
          assertions.assertPropertyEquals("2025-01-01", updated, { it.minDate }, "minDate")
        }
  }

  @Test
  fun dateFieldConfiguration_maxDateInputUpdatesType() {
    val updates = mutableListOf<FieldType.Date>()

    utils.testInputUpdate(
        composeTestRule,
        content = { onUpdate ->
          DateFieldConfiguration(fieldType = FieldType.Date(), onUpdate = onUpdate, enabled = true)
        },
        capturedUpdate = updates,
        testTag = DateFieldConfigurationTestTags.MAX,
        inputValue = "2025-12-31") { updated ->
          assertions.assertPropertyEquals("2025-12-31", updated, { it.maxDate }, "maxDate")
        }
  }

  @Test
  fun dateFieldConfiguration_includeTimeCheckboxUpdatesType() {
    val updates = mutableListOf<FieldType.Date>()

    utils.testCheckboxToggle(
        composeTestRule,
        content = { onUpdate ->
          DateFieldConfiguration(
              fieldType = FieldType.Date(includeTime = false), onUpdate = onUpdate, enabled = true)
        },
        capturedUpdate = updates,
        checkboxTag = DateFieldConfigurationTestTags.INCLUDE_TIME) { updated ->
          assertions.assertBooleanTrue(updated, { it.includeTime }, "includeTime should be true")
        }
  }

  @Test
  fun dateFieldConfiguration_invalidFormatShowsError() {
    this.composeTestRule.setContent {
      DateFieldConfiguration(
          fieldType = FieldType.Date(format = "invalid-format"), onUpdate = {}, enabled = true)
    }

    utils.assertErrorDisplayed(composeTestRule, "Invalid date format pattern")
  }

  @Test
  fun dateFieldConfiguration_validFormatNoError() {
    this.composeTestRule.setContent {
      DateFieldConfiguration(
          fieldType = FieldType.Date(format = "yyyy-MM-dd"), onUpdate = {}, enabled = true)
    }

    utils.assertNoError(composeTestRule, "Invalid date format pattern")
  }

  @Test
  fun dateFieldConfiguration_disabledAllFieldsDisabled() {
    utils.testDisabledState(
        composeTestRule,
        content = {
          DateFieldConfiguration(fieldType = FieldType.Date(), onUpdate = {}, enabled = false)
        },
        DateFieldConfigurationTestTags.MIN,
        DateFieldConfigurationTestTags.MAX,
        DateFieldConfigurationTestTags.INCLUDE_TIME,
        DateFieldConfigurationTestTags.FORMAT)
  }

  @Test
  fun dateFieldConfiguration_includeTimeUncheckedUpdatesType() {
    val updates = mutableListOf<FieldType.Date>()

    utils.testCheckboxToggle(
        composeTestRule,
        content = { onUpdate ->
          DateFieldConfiguration(
              fieldType = FieldType.Date(includeTime = true), onUpdate = onUpdate, enabled = true)
        },
        capturedUpdate = updates,
        checkboxTag = DateFieldConfigurationTestTags.INCLUDE_TIME) { updated ->
          assertions.assertBooleanFalse(updated, { it.includeTime }, "includeTime should be false")
        }
  }

  @Test
  fun dateFieldConfiguration_withInitialMinDateDisplays() {
    utils.testDisplaysAllFields(
        composeTestRule,
        content = {
          DateFieldConfiguration(
              fieldType = FieldType.Date(minDate = "2025-01-01"), onUpdate = {}, enabled = true)
        },
        DateFieldConfigurationTestTags.MIN)
  }

  @Test
  fun dateFieldConfiguration_withInitialMaxDateDisplays() {
    utils.testDisplaysAllFields(
        composeTestRule,
        content = {
          DateFieldConfiguration(
              fieldType = FieldType.Date(maxDate = "2025-12-31"), onUpdate = {}, enabled = true)
        },
        DateFieldConfigurationTestTags.MAX)
  }

  @Test
  fun dateFieldConfiguration_withBothMinAndMaxDatesDisplays() {
    utils.testDisplaysAllFields(
        composeTestRule,
        content = {
          DateFieldConfiguration(
              fieldType = FieldType.Date(minDate = "2025-01-01", maxDate = "2025-12-31"),
              onUpdate = {},
              enabled = true)
        },
        "date_min",
        DateFieldConfigurationTestTags.MAX)
  }

  @Test
  fun dateFieldConfiguration_customFormatDisplays() {
    utils.testDisplaysAllFields(
        composeTestRule,
        content = {
          DateFieldConfiguration(
              fieldType = FieldType.Date(format = "MM/dd/yyyy"), onUpdate = {}, enabled = true)
        },
        DateFieldConfigurationTestTags.FORMAT)
  }

  @Test
  fun dateFieldConfiguration_blankMinDateSetsToNull() {
    val updates = mutableListOf<FieldType.Date>()
    this.composeTestRule.setContent {
      DateFieldConfiguration(
          fieldType = FieldType.Date(minDate = "2025-01-01"),
          onUpdate = { updates.add(it) },
          enabled = true)
    }

    utils.clearText(composeTestRule, DateFieldConfigurationTestTags.MIN)
    assertions.assertPropertyNull(updates.lastOrNull(), { it.minDate }, "minDate")
  }

  @Test
  fun dateFieldConfiguration_blankMaxDateSetsToNull() {
    val updates = mutableListOf<FieldType.Date>()
    this.composeTestRule.setContent {
      DateFieldConfiguration(
          fieldType = FieldType.Date(maxDate = "2025-12-31"),
          onUpdate = { updates.add(it) },
          enabled = true)
    }

    utils.clearText(composeTestRule, DateFieldConfigurationTestTags.MAX)
    assertions.assertPropertyNull(updates.lastOrNull(), { it.maxDate }, "maxDate")
  }
}
