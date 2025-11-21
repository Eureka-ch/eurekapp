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
        MultiSelectFieldConfigurationTestTags.MIN,
        MultiSelectFieldConfigurationTestTags.MAX,
        MultiSelectFieldConfigurationTestTags.ALLOW_CUSTOM)
  }

  @Test
  fun multiSelectFieldConfiguration_minSelectionsInputUpdatesType() {
    val updates = mutableListOf<FieldType.MultiSelect>()

    utils.testInputUpdate(
        composeTestRule,
        content = { onUpdate ->
          MultiSelectFieldConfiguration(
              fieldType = FieldType.MultiSelect(testOptions), onUpdate = onUpdate, enabled = true)
        },
        capturedUpdate = updates,
        testTag = MultiSelectFieldConfigurationTestTags.MIN,
        inputValue = "2") { updated ->
          assertions.assertPropertyEquals(2, updated, { it.minSelections }, "minSelections")
        }
  }

  @Test
  fun multiSelectFieldConfiguration_maxSelectionsInputUpdatesType() {
    val updates = mutableListOf<FieldType.MultiSelect>()

    utils.testInputUpdate(
        composeTestRule,
        content = { onUpdate ->
          MultiSelectFieldConfiguration(
              fieldType = FieldType.MultiSelect(testOptions), onUpdate = onUpdate, enabled = true)
        },
        capturedUpdate = updates,
        testTag = MultiSelectFieldConfigurationTestTags.MAX,
        inputValue = "3") { updated ->
          assertions.assertPropertyEquals(3, updated, { it.maxSelections }, "maxSelections")
        }
  }

  @Test
  fun multiSelectFieldConfiguration_allowCustomCheckboxUpdatesType() {
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
        checkboxTag = MultiSelectFieldConfigurationTestTags.ALLOW_CUSTOM) { updated ->
          assertions.assertBooleanTrue(updated, { it.allowCustom }, "allowCustom should be true")
        }
  }

  @Test
  fun multiSelectFieldConfiguration_minLessThanMaxNoError() {
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
  fun multiSelectFieldConfiguration_minEqualToMaxNoError() {
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
  fun multiSelectFieldConfiguration_disabledAllFieldsDisabled() {
    utils.testDisabledState(
        composeTestRule,
        content = {
          MultiSelectFieldConfiguration(
              fieldType = FieldType.MultiSelect(testOptions), onUpdate = {}, enabled = false)
        },
        MultiSelectFieldConfigurationTestTags.MIN,
        MultiSelectFieldConfigurationTestTags.MAX,
        MultiSelectFieldConfigurationTestTags.ALLOW_CUSTOM)
  }

  @Test
  fun multiSelectFieldConfiguration_emptyMinSelectionsSetsToNull() {
    val updates = mutableListOf<FieldType.MultiSelect>()
    this.composeTestRule.setContent {
      MultiSelectFieldConfiguration(
          fieldType = FieldType.MultiSelect(testOptions, minSelections = 2),
          onUpdate = { updates.add(it) },
          enabled = true)
    }

    utils.clearText(composeTestRule, MultiSelectFieldConfigurationTestTags.MIN)
    assertions.assertPropertyNull(updates.lastOrNull(), { it.minSelections }, "minSelections")
  }

  @Test
  fun multiSelectFieldConfiguration_allowCustomUncheckedUpdatesType() {
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
        checkboxTag = MultiSelectFieldConfigurationTestTags.ALLOW_CUSTOM) { updated ->
          assertions.assertBooleanFalse(updated, { it.allowCustom }, "allowCustom should be false")
        }
  }

  @Test
  fun multiSelectFieldConfiguration_emptyMaxSelectionsSetsToNull() {
    val updates = mutableListOf<FieldType.MultiSelect>()
    this.composeTestRule.setContent {
      MultiSelectFieldConfiguration(
          fieldType = FieldType.MultiSelect(testOptions, maxSelections = 3),
          onUpdate = { updates.add(it) },
          enabled = true)
    }

    utils.clearText(composeTestRule, MultiSelectFieldConfigurationTestTags.MAX)
    assertions.assertPropertyNull(updates.lastOrNull(), { it.maxSelections }, "maxSelections")
  }

  @Test
  fun multiSelectFieldConfiguration_withInitialMinSelectionsDisplays() {
    utils.testDisplaysAllFields(
        composeTestRule,
        content = {
          MultiSelectFieldConfiguration(
              fieldType = FieldType.MultiSelect(testOptions, minSelections = 1),
              onUpdate = {},
              enabled = true)
        },
        MultiSelectFieldConfigurationTestTags.MIN)
  }

  @Test
  fun multiSelectFieldConfiguration_withInitialMaxSelectionsDisplays() {
    utils.testDisplaysAllFields(
        composeTestRule,
        content = {
          MultiSelectFieldConfiguration(
              fieldType = FieldType.MultiSelect(testOptions, maxSelections = 3),
              onUpdate = {},
              enabled = true)
        },
        MultiSelectFieldConfigurationTestTags.MAX)
  }

  @Test
  fun multiSelectFieldConfiguration_preservesOptionsAfterUpdate() {
    val updates = mutableListOf<FieldType.MultiSelect>()

    utils.testInputUpdate(
        composeTestRule,
        content = { onUpdate ->
          MultiSelectFieldConfiguration(
              fieldType = FieldType.MultiSelect(testOptions), onUpdate = onUpdate, enabled = true)
        },
        capturedUpdate = updates,
        testTag = MultiSelectFieldConfigurationTestTags.MIN,
        inputValue = "1") { updated ->
          assertions.assertListPreserved(testOptions, updated, { it.options }, "options")
        }
  }
}
