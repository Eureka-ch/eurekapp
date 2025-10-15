package ch.eureka.eurekapp.screen

import android.Manifest
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.rule.GrantPermissionRule
import ch.eureka.eurekapp.screens.Camera
import ch.eureka.eurekapp.screens.CameraScreenTestTags
import org.junit.Rule
import org.junit.Test

class CameraScreenNoPermissionTest {
  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun testCameraUI() {
    composeTestRule.setContent { MaterialTheme { Camera() } }

    composeTestRule.onNodeWithTag(CameraScreenTestTags.NO_PERMISSION).assertIsDisplayed()

    composeTestRule.onNodeWithTag(CameraScreenTestTags.GRANT_PERMISSION).assertIsDisplayed()
  }
}

class CameraScreenTest {
  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @get:Rule
  var permissionRule: GrantPermissionRule? = GrantPermissionRule.grant(Manifest.permission.CAMERA)

  @Test
  fun testCameraUI() {
    composeTestRule.setContent { MaterialTheme { Camera() } }

    composeTestRule.onNodeWithTag(CameraScreenTestTags.TAKE_PHOTO).assertIsDisplayed()

    composeTestRule.onNodeWithTag(CameraScreenTestTags.DELETE_PHOTO).assertIsNotDisplayed()

    composeTestRule.onNodeWithTag(CameraScreenTestTags.SAVE_PHOTO).assertIsNotDisplayed()

    composeTestRule.onNodeWithTag(CameraScreenTestTags.PREVIEW).assertIsDisplayed()

    composeTestRule.onNodeWithTag(CameraScreenTestTags.TAKE_PHOTO).performClick()

    Thread.sleep(5000)

    composeTestRule.onNodeWithTag(CameraScreenTestTags.TAKE_PHOTO).assertIsNotDisplayed()

    composeTestRule.onNodeWithTag(CameraScreenTestTags.DELETE_PHOTO).assertIsDisplayed()

    composeTestRule.onNodeWithTag(CameraScreenTestTags.SAVE_PHOTO).assertIsDisplayed()

    composeTestRule.onNodeWithTag(CameraScreenTestTags.PHOTO).assertIsDisplayed()

    composeTestRule.onNodeWithTag(CameraScreenTestTags.DELETE_PHOTO).performClick()


    Thread.sleep(5000)

    composeTestRule.onNodeWithTag(CameraScreenTestTags.TAKE_PHOTO).assertIsDisplayed()

    composeTestRule.onNodeWithTag(CameraScreenTestTags.DELETE_PHOTO).assertIsNotDisplayed()

    composeTestRule.onNodeWithTag(CameraScreenTestTags.SAVE_PHOTO).assertIsNotDisplayed()

    composeTestRule.onNodeWithTag(CameraScreenTestTags.PREVIEW).assertIsDisplayed()

  }
}
