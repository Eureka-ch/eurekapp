package ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.fieldtypes

/* Portions of this code were generated with the help of Claude Sonnet 4.5. */

import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.utils.BaseFieldConfigurationTest
import org.junit.Test

/**
 * Android UI tests for MultiSelectFieldConfiguration.
 *
 * Portions of this code were generated with the help of Claude Sonnet 4.5.
 */
class MultiSelectFieldConfigurationTest : BaseFieldConfigurationTest() {

  private val testOptions = utils.createTestOptions()

  @Test
  fun multiSelectFieldConfiguration_displaysAllFields() {
    utils.testDisplaysAllFields(
        composeTestRule,
        content = {
          MultiSelectFieldConfiguration(
              fieldType = FieldType.MultiSelect(testOptions), onUpdate = {}, enabled = true)
        },
        "multi_select_min",
        "multi_select_max",
        "multi_select_allow_custom")
  }

  @Test
  fun multiSelectFieldConfiguration_minSelectionsInput_updatesType() {
    val updates = mutableListOf<FieldType.MultiSelect>()

    utils.testInputUpdate(
        composeTestRule,
        content = { onUpdate ->
          MultiSelectFieldConfiguration(
              fieldType = FieldType.MultiSelect(testOptions), onUpdate = onUpdate, enabled = true)
        },
        capturedUpdate = updates,
        testTag = "multi_select_min",
        inputValue = "2") { updated ->
          assertions.assertPropertyEquals(2, updated, { it.minSelections }, "minSelections")
        }
  }

  @Test
  fun multiSelectFieldConfiguration_maxSelectionsInput_updatesType() {
    val updates = mutableListOf<FieldType.MultiSelect>()

    utils.testInputUpdate(
        composeTestRule,
        content = { onUpdate ->
          MultiSelectFieldConfiguration(
              fieldType = FieldType.MultiSelect(testOptions), onUpdate = onUpdate, enabled = true)
        },
        capturedUpdate = updates,
        testTag = "multi_select_max",
        inputValue = "3") { updated ->
          assertions.assertPropertyEquals(3, updated, { it.maxSelections }, "maxSelections")
        }
  }

  @Test
  fun multiSelectFieldConfiguration_allowCustomCheckbox_updatesType() {
    val updates = mutableListOf<FieldType.MultiSelect>()

    utils.testCheckboxToggle(
        composeTestRule,
        content = { onUpdate ->
          MultiSelectFieldConfiguration(
              fieldType = FieldType.MultiSelect(testOptions, allowCustom = false),
              onUpdate = onUpdate,
              enabled = true)
        },
        capturedUpdate = updates,
        checkboxTag = "multi_select_allow_custom") { updated ->
          assertions.assertBooleanTrue(updated, { it.allowCustom }, "allowCustom should be true")
        }
  }

  @Test
  fun multiSelectFieldConfiguration_minLessThanMax_noError() {
    this.composeTestRule.setContent {
      MultiSelectFieldConfiguration(
          fieldType = FieldType.MultiSelect(testOptions, minSelections = 1, maxSelections = 3),
          onUpdate = {},
          enabled = true)
    }

    utils.assertNoError(
        composeTestRule, "Minimum selections must be less than or equal to maximum selections")
  }

  @Test
  fun multiSelectFieldConfiguration_minEqualToMax_noError() {
    this.composeTestRule.setContent {
      MultiSelectFieldConfiguration(
          fieldType = FieldType.MultiSelect(testOptions, minSelections = 2, maxSelections = 2),
          onUpdate = {},
          enabled = true)
    }

    utils.assertNoError(
        composeTestRule, "Minimum selections must be less than or equal to maximum selections")
  }

  @Test
  fun multiSelectFieldConfiguration_disabled_allFieldsDisabled() {
    utils.testDisabledState(
        composeTestRule,
        content = {
          MultiSelectFieldConfiguration(
              fieldType = FieldType.MultiSelect(testOptions), onUpdate = {}, enabled = false)
        },
        "multi_select_min",
        "multi_select_max",
        "multi_select_allow_custom")
  }

  @Test
  fun multiSelectFieldConfiguration_emptyMinSelections_setsToNull() {
    val updates = mutableListOf<FieldType.MultiSelect>()
    this.composeTestRule.setContent {
      MultiSelectFieldConfiguration(
          fieldType = FieldType.MultiSelect(testOptions, minSelections = 2),
          onUpdate = { updates.add(it) },
          enabled = true)
    }

    utils.clearText(composeTestRule, "multi_select_min")
    assertions.assertPropertyNull(updates.lastOrNull(), { it.minSelections }, "minSelections")
  }

  @Test
  fun multiSelectFieldConfiguration_allowCustomUnchecked_updatesType() {
    val updates = mutableListOf<FieldType.MultiSelect>()

    utils.testCheckboxToggle(
        composeTestRule,
        content = { onUpdate ->
          MultiSelectFieldConfiguration(
              fieldType = FieldType.MultiSelect(testOptions, allowCustom = true),
              onUpdate = onUpdate,
              enabled = true)
        },
        capturedUpdate = updates,
        checkboxTag = "multi_select_allow_custom") { updated ->
          assertions.assertBooleanFalse(updated, { it.allowCustom }, "allowCustom should be false")
        }
  }

  @Test
  fun multiSelectFieldConfiguration_emptyMaxSelections_setsToNull() {
    val updates = mutableListOf<FieldType.MultiSelect>()
    this.composeTestRule.setContent {
      MultiSelectFieldConfiguration(
          fieldType = FieldType.MultiSelect(testOptions, maxSelections = 3),
          onUpdate = { updates.add(it) },
          enabled = true)
    }

    utils.clearText(composeTestRule, "multi_select_max")
    assertions.assertPropertyNull(updates.lastOrNull(), { it.maxSelections }, "maxSelections")
  }

  @Test
  fun multiSelectFieldConfiguration_withInitialMinSelections_displays() {
    utils.testDisplaysAllFields(
        composeTestRule,
        content = {
          MultiSelectFieldConfiguration(
              fieldType = FieldType.MultiSelect(testOptions, minSelections = 1),
              onUpdate = {},
              enabled = true)
        },
        "multi_select_min")
  }

  @Test
  fun multiSelectFieldConfiguration_withInitialMaxSelections_displays() {
    utils.testDisplaysAllFields(
        composeTestRule,
        content = {
          MultiSelectFieldConfiguration(
              fieldType = FieldType.MultiSelect(testOptions, maxSelections = 3),
              onUpdate = {},
              enabled = true)
        },
        "multi_select_max")
  }

  @Test
  fun multiSelectFieldConfiguration_preservesOptions_afterUpdate() {
    val updates = mutableListOf<FieldType.MultiSelect>()

    utils.testInputUpdate(
        composeTestRule,
        content = { onUpdate ->
          MultiSelectFieldConfiguration(
              fieldType = FieldType.MultiSelect(testOptions), onUpdate = onUpdate, enabled = true)
        },
        capturedUpdate = updates,
        testTag = "multi_select_min",
        inputValue = "1") { updated ->
          assertions.assertListPreserved(testOptions, updated, { it.options }, "options")
        }
  }
}
