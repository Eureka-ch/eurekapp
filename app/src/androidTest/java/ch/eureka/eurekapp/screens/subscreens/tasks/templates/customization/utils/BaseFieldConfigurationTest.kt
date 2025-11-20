package ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.utils

import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule

/** Base test class for field configuration tests. Provides common setup and utilities. */
abstract class BaseFieldConfigurationTest {

  @get:Rule val composeTestRule = createComposeRule()

  /** Utility reference for convenience. */
  protected val utils = FieldConfigurationTestUtils

  /** Assertions reference for convenience. */
  protected val assertions = FieldConfigurationAssertions
}
