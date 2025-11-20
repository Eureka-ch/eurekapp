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
        "number_min",
        "number_max",
        "number_step",
        "number_decimals",
        "number_unit")
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
        testTag = "number_min",
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
        testTag = "number_max",
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
        testTag = "number_step",
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
        testTag = "number_unit",
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
        testTag = "number_min",
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
        testTag = "number_decimals",
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
        "number_min",
        "number_max",
        "number_step",
        "number_decimals",
        "number_unit")
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
        testTag = "number_decimals",
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
        "number_min")
  }

  @Test
  fun numberFieldConfiguration_withInitialMaxDisplays() {
    utils.testDisplaysAllFields(
        composeTestRule,
        content = {
          NumberFieldConfiguration(
              fieldType = FieldType.Number(max = 100.0), onUpdate = {}, enabled = true)
        },
        "number_max")
  }

  @Test
  fun numberFieldConfiguration_withInitialStepDisplays() {
    utils.testDisplaysAllFields(
        composeTestRule,
        content = {
          NumberFieldConfiguration(
              fieldType = FieldType.Number(step = 0.1), onUpdate = {}, enabled = true)
        },
        "number_step")
  }

  @Test
  fun numberFieldConfigurationWithInitialUnitDisplays() {
    utils.testDisplaysAllFields(
        composeTestRule,
        content = {
          NumberFieldConfiguration(
              fieldType = FieldType.Number(unit = "meters"), onUpdate = {}, enabled = true)
        },
        "number_unit")
  }

  @Test
  fun numberFieldConfigurationDecimalsInputUpdatesType() {
    val updates = mutableListOf<FieldType.Number>()

    utils.testInputUpdate(
        composeTestRule,
        content = { onUpdate ->
          NumberFieldConfiguration(
              fieldType = FieldType.Number(), onUpdate = onUpdate, enabled = true)
        },
        capturedUpdate = updates,
        testTag = "number_decimals",
        inputValue = "2") { updated ->
          assertions.assertPropertyEquals(2, updated, { it.decimals }, "decimals")
        }
  }

  @Test
  fun numberFieldConfigurationBlankUnitSetsToNull() {
    val updates = mutableListOf<FieldType.Number>()
    this.composeTestRule.setContent {
      NumberFieldConfiguration(
          fieldType = FieldType.Number(unit = "kg"), onUpdate = { updates.add(it) }, enabled = true)
    }

    utils.clearText(composeTestRule, "number_unit")
    assertions.assertPropertyNull(updates.lastOrNull(), { it.unit }, "unit")
  }

  @Test
  fun numberFieldConfigurationEmptyMinSetsToNull() {
    val updates = mutableListOf<FieldType.Number>()
    this.composeTestRule.setContent {
      NumberFieldConfiguration(
          fieldType = FieldType.Number(min = 10.0), onUpdate = { updates.add(it) }, enabled = true)
    }

    utils.clearText(composeTestRule, "number_min")
    assertions.assertPropertyNull(updates.lastOrNull(), { it.min }, "min")
  }
}
