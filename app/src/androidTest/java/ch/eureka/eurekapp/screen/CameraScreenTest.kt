// Portions of this code were generated with the help of Grok and Claude.
package ch.eureka.eurekapp.screen

import android.Manifest
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.rule.GrantPermissionRule
import ch.eureka.eurekapp.screens.Camera
import ch.eureka.eurekapp.screens.CameraScreenTestTags
import org.junit.Rule
import org.junit.Test

class CameraScreenNoPermissionTest {
  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  private var onBackClickCalled = false

  @Test
  fun cameraScreenNoPermission_cameraUI() {
    onBackClickCalled = false
    composeTestRule.setContent {
      MaterialTheme { Camera(onBackClick = { onBackClickCalled = true }, onPhotoSaved = {}) }
    }

    composeTestRule.onNodeWithTag(CameraScreenTestTags.NO_PERMISSION).assertIsDisplayed()

    composeTestRule.onNodeWithTag(CameraScreenTestTags.GRANT_PERMISSION).assertIsDisplayed()

    composeTestRule.onNodeWithTag(CameraScreenTestTags.BACK_BUTTON).assertIsDisplayed()
  }

  @Test
  fun cameraScreenNoPermission_backButtonFunctionality() {
    onBackClickCalled = false
    composeTestRule.setContent {
      MaterialTheme { Camera(onBackClick = { onBackClickCalled = true }, onPhotoSaved = {}) }
    }

    composeTestRule.onNodeWithTag(CameraScreenTestTags.BACK_BUTTON).performClick()

    assert(onBackClickCalled) { "onBackClick should be called" }
  }
}

class CameraScreenTest {
  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @get:Rule
  var permissionRule: GrantPermissionRule? = GrantPermissionRule.grant(Manifest.permission.CAMERA)

  private var onBackClickCalled = false

  @Test
  fun cameraScreen_cameraUI() {
    onBackClickCalled = false
    composeTestRule.setContent {
      MaterialTheme { Camera(onBackClick = { onBackClickCalled = true }, onPhotoSaved = {}) }
    }

    composeTestRule.onNodeWithTag(CameraScreenTestTags.TAKE_PHOTO).assertIsDisplayed()

    composeTestRule.onNodeWithTag(CameraScreenTestTags.DELETE_PHOTO).assertIsNotDisplayed()

    composeTestRule.onNodeWithTag(CameraScreenTestTags.SAVE_PHOTO).assertIsNotDisplayed()

    composeTestRule.onNodeWithTag(CameraScreenTestTags.PREVIEW).assertIsDisplayed()

    composeTestRule.onNodeWithTag(CameraScreenTestTags.BACK_BUTTON).assertIsDisplayed()

    // Click take photo button
    composeTestRule.onNodeWithTag(CameraScreenTestTags.TAKE_PHOTO).performClick()

    composeTestRule.waitUntil(timeoutMillis = 6_000) {
      composeTestRule
          .onAllNodesWithTag(CameraScreenTestTags.SAVE_PHOTO)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithTag(CameraScreenTestTags.DELETE_PHOTO).assertIsDisplayed()

    composeTestRule.onNodeWithTag(CameraScreenTestTags.SAVE_PHOTO).assertIsDisplayed()

    composeTestRule.onNodeWithTag(CameraScreenTestTags.PHOTO).assertIsDisplayed()

    composeTestRule.onNodeWithTag(CameraScreenTestTags.DELETE_PHOTO).performClick()

    composeTestRule.waitUntil(timeoutMillis = 6_000) {
      composeTestRule
          .onAllNodesWithTag(CameraScreenTestTags.TAKE_PHOTO)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithTag(CameraScreenTestTags.TAKE_PHOTO).assertIsDisplayed()

    composeTestRule.onNodeWithTag(CameraScreenTestTags.DELETE_PHOTO).assertIsNotDisplayed()

    composeTestRule.onNodeWithTag(CameraScreenTestTags.SAVE_PHOTO).assertIsNotDisplayed()

    composeTestRule.onNodeWithTag(CameraScreenTestTags.PREVIEW).assertIsDisplayed()
  }

  @Test
  fun cameraScreen_backButtonFunctionality() {
    onBackClickCalled = false
    composeTestRule.setContent {
      MaterialTheme { Camera(onBackClick = { onBackClickCalled = true }, onPhotoSaved = {}) }
    }

    composeTestRule.onNodeWithTag(CameraScreenTestTags.BACK_BUTTON).performClick()

    assert(onBackClickCalled) { "onBackClick should be called" }
  }
}
