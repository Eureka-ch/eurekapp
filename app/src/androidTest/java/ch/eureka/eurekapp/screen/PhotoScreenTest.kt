package ch.eureka.eurekapp.screen

import android.Manifest
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.rule.GrantPermissionRule
import ch.eureka.eurekapp.Camera
import ch.eureka.eurekapp.ui.photos.PhotoScreenTestTags
import org.junit.Rule
import org.junit.Test

class PhotoScreenNoPermissionTest {
  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun testCameraUI() {
    composeTestRule.setContent { MaterialTheme { Camera() } }

    composeTestRule.onNodeWithTag(PhotoScreenTestTags.NO_PERMISSION).assertIsDisplayed()

    composeTestRule.onNodeWithTag(PhotoScreenTestTags.GRANT_PERMISSION).assertIsDisplayed()
  }
}

class PhotoScreenTest {
  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @get:Rule
  var permissionRule: GrantPermissionRule? = GrantPermissionRule.grant(Manifest.permission.CAMERA)

  @Test
  fun testCameraUI() {
    composeTestRule.setContent { MaterialTheme { Camera() } }

    composeTestRule.onNodeWithTag(PhotoScreenTestTags.TAKE_PHOTO).assertIsDisplayed()

    composeTestRule.onNodeWithTag(PhotoScreenTestTags.DELETE_PHOTO).assertIsDisplayed()

    composeTestRule.onNodeWithTag(PhotoScreenTestTags.PREVIEW).assertIsDisplayed()

    composeTestRule.onNodeWithTag(PhotoScreenTestTags.DELETE_PHOTO).performClick()

    Thread.sleep(1000)

    composeTestRule.onNodeWithTag(PhotoScreenTestTags.TAKE_PHOTO).assertIsDisplayed()

    composeTestRule.onNodeWithTag(PhotoScreenTestTags.DELETE_PHOTO).assertIsDisplayed()

    composeTestRule.onNodeWithTag(PhotoScreenTestTags.PREVIEW).assertIsDisplayed()

    composeTestRule.onNodeWithTag(PhotoScreenTestTags.TAKE_PHOTO).performClick()

    Thread.sleep(1000)

    composeTestRule.onNodeWithTag(PhotoScreenTestTags.TAKE_PHOTO).assertIsDisplayed()

    composeTestRule.onNodeWithTag(PhotoScreenTestTags.DELETE_PHOTO).assertIsDisplayed()

    composeTestRule.onNodeWithTag(PhotoScreenTestTags.PHOTO).assertIsDisplayed()

    composeTestRule.onNodeWithTag(PhotoScreenTestTags.TAKE_PHOTO).performClick()

    Thread.sleep(1000)

    composeTestRule.onNodeWithTag(PhotoScreenTestTags.TAKE_PHOTO).assertIsDisplayed()

    composeTestRule.onNodeWithTag(PhotoScreenTestTags.DELETE_PHOTO).assertIsDisplayed()

    composeTestRule.onNodeWithTag(PhotoScreenTestTags.PHOTO).assertIsDisplayed()

    composeTestRule.onNodeWithTag(PhotoScreenTestTags.DELETE_PHOTO).performClick()

    Thread.sleep(1000)

    composeTestRule.onNodeWithTag(PhotoScreenTestTags.TAKE_PHOTO).assertIsDisplayed()

    composeTestRule.onNodeWithTag(PhotoScreenTestTags.DELETE_PHOTO).assertIsDisplayed()

    composeTestRule.onNodeWithTag(PhotoScreenTestTags.PREVIEW).assertIsDisplayed()
  }
}
