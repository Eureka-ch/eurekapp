package ch.eureka.eurekapp.screens.subscreens.tasks

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.assertCountEquals
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.screens.subscreens.tasks.CommonTaskTestTags.ADD_DEPENDENCY_BUTTON
import org.junit.Rule
import org.junit.Test

class TaskDependenciesSelectionFieldTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun showsMessageWhenProjectHasTasksButNoSelectableDependencies() {
    val currentTaskId = "task-123"
    val availableTasks = listOf(Task(taskID = currentTaskId, title = "Current task"))

    composeTestRule.setContent {
      TaskDependenciesSelectionField(
          availableTasks = availableTasks,
          selectedDependencyIds = emptyList(),
          onDependencyAdded = {},
          onDependencyRemoved = {},
          currentTaskId = currentTaskId,
          cycleError = null)
    }

    composeTestRule
        .onNodeWithText("No other tasks in this project can be added as dependencies.")
        .assertIsDisplayed()
    composeTestRule.onAllNodesWithTag(ADD_DEPENDENCY_BUTTON).assertCountEquals(0)
  }
}

