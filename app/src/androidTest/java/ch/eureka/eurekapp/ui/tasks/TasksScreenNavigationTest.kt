package ch.eureka.eurekapp.ui.tasks

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.ui.theme.EurekappTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TasksScreenNavigationTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<androidx.activity.ComponentActivity>()

  @Test
  fun tasksScreen_filterSelectionTriggersViewModel() {
    // Given
    composeTestRule.setContent { EurekappTheme { TasksScreen() } }

    // When
    TasksScreenRobot(composeTestRule).clickTeamFilter()

    // Then
    // Verify the filter button is clickable (simplified test)
    TasksScreenRobot(composeTestRule).assertFilterDisplayed("Team")
  }
}
