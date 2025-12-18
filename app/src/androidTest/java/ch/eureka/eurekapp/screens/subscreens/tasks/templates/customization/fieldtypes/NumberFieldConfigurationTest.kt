package ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.fieldtypes

/* Portions of this code were generated with the help of Claude Sonnet 4.5. */

import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.utils.BaseFieldConfigurationTest
import org.junit.Test

/**
 * Android UI tests for NumberFieldConfiguration.
 *
 * Portions of this code were generated with the help of Claude Sonnet 4.5.
 */
class NumberFieldConfigurationTest : BaseFieldConfigurationTest() {

  @Test
  fun numberFieldConfiguration_displaysAllFields() {
    utils.testDisplaysAllFields(
        composeTestRule,
        content = {
          NumberFieldConfiguration(fieldType = FieldType.Number(), onUpdate = {}, enabled = true)
        },
        NumberFieldConfigurationTestTags.MIN,
        NumberFieldConfigurationTestTags.MAX,
        NumberFieldConfigurationTestTags.STEP,
        NumberFieldConfigurationTestTags.DECIMALS,
        NumberFieldConfigurationTestTags.UNIT)
  }

  @Test
  fun numberFieldConfiguration_minInputUpdatesType() {
    val updates = mutableListOf<FieldType.Number>()

    utils.testInputUpdate(
        composeTestRule,
        content = { onUpdate ->
          NumberFieldConfiguration(
              fieldType = FieldType.Number(), onUpdate = onUpdate, enabled = true)
        },
        capturedUpdate = updates,
        testTag = NumberFieldConfigurationTestTags.MIN,
        inputValue = "10.5") { updated ->
          assertions.assertPropertyEquals(10.5, updated, { it.min }, "min")
        }
  }

  @Test
  fun numberFieldConfiguration_maxInputUpdatesType() {
    val updates = mutableListOf<FieldType.Number>()

    utils.testInputUpdate(
        composeTestRule,
        content = { onUpdate ->
          NumberFieldConfiguration(
              fieldType = FieldType.Number(), onUpdate = onUpdate, enabled = true)
        },
        capturedUpdate = updates,
        testTag = NumberFieldConfigurationTestTags.MAX,
        inputValue = "100.0") { updated ->
          assertions.assertPropertyEquals(100.0, updated, { it.max }, "max")
        }
  }

  @Test
  fun numberFieldConfiguration_stepInputUpdatesType() {
    val updates = mutableListOf<FieldType.Number>()

    utils.testInputUpdate(
        composeTestRule,
        content = { onUpdate ->
          NumberFieldConfiguration(
              fieldType = FieldType.Number(), onUpdate = onUpdate, enabled = true)
        },
        capturedUpdate = updates,
        testTag = NumberFieldConfigurationTestTags.STEP,
        inputValue = "0.5") { updated ->
          assertions.assertPropertyEquals(0.5, updated, { it.step }, "step")
        }
  }

  @Test
  fun numberFieldConfiguration_unitInputUpdatesType() {
    val updates = mutableListOf<FieldType.Number>()

    utils.testInputUpdate(
        composeTestRule,
        content = { onUpdate ->
          NumberFieldConfiguration(
              fieldType = FieldType.Number(), onUpdate = onUpdate, enabled = true)
        },
        capturedUpdate = updates,
        testTag = NumberFieldConfigurationTestTags.UNIT,
        inputValue = "kg") { updated ->
          assertions.assertPropertyEquals("kg", updated, { it.unit }, "unit")
        }
  }

  @Test
  fun numberFieldConfiguration_minLessThanMaxNoError() {
    this.composeTestRule.setContent {
      NumberFieldConfiguration(
          fieldType = FieldType.Number(min = 10.0, max = 100.0), onUpdate = {}, enabled = true)
    }

    utils.assertNoError(
        composeTestRule, "Minimum value must be less than or equal to maximum value")
  }

  @Test
  fun numberFieldConfiguration_minEqualToMaxNoError() {
    this.composeTestRule.setContent {
      NumberFieldConfiguration(
          fieldType = FieldType.Number(min = 50.0, max = 50.0), onUpdate = {}, enabled = true)
    }

    utils.assertNoError(
        composeTestRule, "Minimum value must be less than or equal to maximum value")
  }

  @Test
  fun numberFieldConfiguration_negativeMinAccepted() {
    val updates = mutableListOf<FieldType.Number>()

    utils.testInputUpdate(
        composeTestRule,
        content = { onUpdate ->
          NumberFieldConfiguration(
              fieldType = FieldType.Number(), onUpdate = onUpdate, enabled = true)
        },
        capturedUpdate = updates,
        testTag = NumberFieldConfigurationTestTags.MIN,
        inputValue = "-25.5") { updated ->
          assertions.assertPropertyEquals(-25.5, updated, { it.min }, "min")
        }
  }

  @Test
  fun numberFieldConfiguration_invalidDecimalsInputFallsBackToZero() {
    val updates = mutableListOf<FieldType.Number>()

    utils.testInputUpdate(
        composeTestRule,
        content = { onUpdate ->
          NumberFieldConfiguration(
              fieldType = FieldType.Number(), onUpdate = onUpdate, enabled = true)
        },
        capturedUpdate = updates,
        testTag = NumberFieldConfigurationTestTags.DECIMALS,
        inputValue = "abc") { updated ->
          assertions.assertPropertyEquals(0, updated, { it.decimals }, "decimals")
        }
  }

  @Test
  fun numberFieldConfiguration_disabledAllFieldsDisabled() {
    utils.testDisabledState(
        composeTestRule,
        content = {
          NumberFieldConfiguration(fieldType = FieldType.Number(), onUpdate = {}, enabled = false)
        },
        NumberFieldConfigurationTestTags.MIN,
        NumberFieldConfigurationTestTags.MAX,
        NumberFieldConfigurationTestTags.STEP,
        NumberFieldConfigurationTestTags.DECIMALS,
        NumberFieldConfigurationTestTags.UNIT)
  }

  @Test
  fun numberFieldConfiguration_zeroDecimalsAccepted() {
    val updates = mutableListOf<FieldType.Number>()

    utils.testInputUpdate(
        composeTestRule,
        content = { onUpdate ->
          NumberFieldConfiguration(
              fieldType = FieldType.Number(), onUpdate = onUpdate, enabled = true)
        },
        capturedUpdate = updates,
        testTag = NumberFieldConfigurationTestTags.DECIMALS,
        inputValue = "0") { updated ->
          assertions.assertPropertyEquals(0, updated, { it.decimals }, "decimals")
        }
  }

  @Test
  fun numberFieldConfiguration_withInitialMinDisplays() {
    utils.testDisplaysAllFields(
        composeTestRule,
        content = {
          NumberFieldConfiguration(
              fieldType = FieldType.Number(min = 5.0), onUpdate = {}, enabled = true)
        },
        NumberFieldConfigurationTestTags.MIN)
  }

  @Test
  fun numberFieldConfiguration_withInitialMaxDisplays() {
    utils.testDisplaysAllFields(
        composeTestRule,
        content = {
          NumberFieldConfiguration(
              fieldType = FieldType.Number(max = 100.0), onUpdate = {}, enabled = true)
        },
        NumberFieldConfigurationTestTags.MAX)
  }

  @Test
  fun numberFieldConfiguration_withInitialStepDisplays() {
    utils.testDisplaysAllFields(
        composeTestRule,
        content = {
          NumberFieldConfiguration(
              fieldType = FieldType.Number(step = 0.1), onUpdate = {}, enabled = true)
        },
        NumberFieldConfigurationTestTags.STEP)
  }

  @Test
  fun numberFieldConfiguration_withInitialUnitDisplays() {
    utils.testDisplaysAllFields(
        composeTestRule,
        content = {
          NumberFieldConfiguration(
              fieldType = FieldType.Number(unit = "meters"), onUpdate = {}, enabled = true)
        },
        NumberFieldConfigurationTestTags.UNIT)
  }

  @Test
  fun numberFieldConfiguration_decimalsInputUpdatesType() {
    val updates = mutableListOf<FieldType.Number>()

    utils.testInputUpdate(
        composeTestRule,
        content = { onUpdate ->
          NumberFieldConfiguration(
              fieldType = FieldType.Number(), onUpdate = onUpdate, enabled = true)
        },
        capturedUpdate = updates,
        testTag = NumberFieldConfigurationTestTags.DECIMALS,
        inputValue = "2") { updated ->
          assertions.assertPropertyEquals(2, updated, { it.decimals }, "decimals")
        }
  }

  @Test
  fun numberFieldConfiguration_blankUnitSetsToNull() {
    val updates = mutableListOf<FieldType.Number>()
    this.composeTestRule.setContent {
      NumberFieldConfiguration(
          fieldType = FieldType.Number(unit = "kg"), onUpdate = { updates.add(it) }, enabled = true)
    }

    utils.clearText(composeTestRule, NumberFieldConfigurationTestTags.UNIT)
    assertions.assertPropertyNull(updates.lastOrNull(), { it.unit }, "unit")
  }

  @Test
  fun numberFieldConfiguration_emptyMinSetsToNull() {
    val updates = mutableListOf<FieldType.Number>()
    this.composeTestRule.setContent {
      NumberFieldConfiguration(
          fieldType = FieldType.Number(min = 10.0), onUpdate = { updates.add(it) }, enabled = true)
    }

    utils.clearText(composeTestRule, NumberFieldConfigurationTestTags.MIN)
    assertions.assertPropertyNull(updates.lastOrNull(), { it.min }, "min")
  }
}
