/**
 * Unit tests for TasksScreen composable
 *
 * Tests the main screen functionality and indirectly covers TaskCard.
 *
 * @author Assisted by AI for comprehensive test coverage
 */
package ch.eureka.eurekapp.ui.tasks

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.ui.theme.EurekappTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TasksScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<androidx.activity.ComponentActivity>()

  @Test
  fun tasksScreenRendersWithoutCrashing() {
    composeTestRule.setContent {
      EurekappTheme { 
        TasksScreen(
          onCreateTaskClick = {}, 
          onAutoAssignClick = {}, 
          onNavigate = {}
        ) 
      }
    }
    
    
  }
}
