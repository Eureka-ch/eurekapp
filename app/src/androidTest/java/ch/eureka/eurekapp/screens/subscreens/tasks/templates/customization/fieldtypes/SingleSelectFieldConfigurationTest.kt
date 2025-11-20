package ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.fieldtypes

/* Portions of this code were generated with the help of Claude Sonnet 4.5. */

import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.onNodeWithTag
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.utils.BaseFieldConfigurationTest
import org.junit.Test

/**
 * Android UI tests for SingleSelectFieldConfiguration.
 *
 * Portions of this code were generated with the help of Claude Sonnet 4.5.
 */
class SingleSelectFieldConfigurationTest : BaseFieldConfigurationTest() {

  private val testOptions = utils.createTestOptions()

  @Test
  fun singleSelectFieldConfigurationDisplaysAllFields() {
    utils.testDisplaysAllFields(
        composeTestRule,
        content = {
          SingleSelectFieldConfiguration(
              fieldType = FieldType.SingleSelect(testOptions), onUpdate = {}, enabled = true)
        },
        "single_select_allow_custom")
  }

  @Test
  fun singleSelectFieldConfigurationAllowCustomCheckboxTogglesToTrue() {
    val updates = mutableListOf<FieldType.SingleSelect>()

    utils.testCheckboxToggle(
        composeTestRule,
        content = { onUpdate ->
          SingleSelectFieldConfiguration(
              fieldType = FieldType.SingleSelect(testOptions, allowCustom = false),
              onUpdate = onUpdate,
              enabled = true)
        },
        capturedUpdate = updates,
        checkboxTag = "single_select_allow_custom") { updated ->
          assertions.assertBooleanTrue(updated, { it.allowCustom }, "allowCustom should be true")
        }
  }

  @Test
  fun singleSelectFieldConfigurationAllowCustomCheckboxTogglesToFalse() {
    val updates = mutableListOf<FieldType.SingleSelect>()

    utils.testCheckboxToggle(
        composeTestRule,
        content = { onUpdate ->
          SingleSelectFieldConfiguration(
              fieldType = FieldType.SingleSelect(testOptions, allowCustom = true),
              onUpdate = onUpdate,
              enabled = true)
        },
        capturedUpdate = updates,
        checkboxTag = "single_select_allow_custom") { updated ->
          assertions.assertBooleanFalse(updated, { it.allowCustom }, "allowCustom should be false")
        }
  }

  @Test
  fun singleSelectFieldConfigurationDisabledCheckboxDisabled() {
    utils.testDisabledState(
        composeTestRule,
        content = {
          SingleSelectFieldConfiguration(
              fieldType = FieldType.SingleSelect(testOptions), onUpdate = {}, enabled = false)
        },
        "single_select_allow_custom")
  }

  @Test
  fun singleSelectFieldConfigurationAllowCustomTrueCheckboxChecked() {
    this.composeTestRule.setContent {
      SingleSelectFieldConfiguration(
          fieldType = FieldType.SingleSelect(testOptions, allowCustom = true),
          onUpdate = {},
          enabled = true)
    }

    composeTestRule.onNodeWithTag(SingleSelectFieldConfigurationTestTags.ALLOW_CUSTOM).assertIsOn()
  }

  @Test
  fun singleSelectFieldConfigurationAllowCustomFalseCheckboxUnchecked() {
    this.composeTestRule.setContent {
      SingleSelectFieldConfiguration(
          fieldType = FieldType.SingleSelect(testOptions, allowCustom = false),
          onUpdate = {},
          enabled = true)
    }

    composeTestRule.onNodeWithTag(SingleSelectFieldConfigurationTestTags.ALLOW_CUSTOM).assertIsOff()
  }

  @Test
  fun singleSelectFieldConfigurationWithManyOptionsDisplays() {
    val manyOptions = utils.createTestOptions(10)
    utils.testDisplaysAllFields(
        composeTestRule,
        content = {
          SingleSelectFieldConfiguration(
              fieldType = FieldType.SingleSelect(manyOptions), onUpdate = {}, enabled = true)
        },
        "single_select_allow_custom")
  }

  @Test
  fun singleSelectFieldConfigurationPreservesOptionsAfterToggle() {
    val updates = mutableListOf<FieldType.SingleSelect>()

    utils.testCheckboxToggle(
        composeTestRule,
        content = { onUpdate ->
          SingleSelectFieldConfiguration(
              fieldType = FieldType.SingleSelect(testOptions, allowCustom = false),
              onUpdate = onUpdate,
              enabled = true)
        },
        capturedUpdate = updates,
        checkboxTag = "single_select_allow_custom") { updated ->
          assertions.assertListPreserved(testOptions, updated, { it.options }, "options")
        }
  }
}
