package ch.eureka.eurekapp.screens.subscreens.tasks

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRightAlt
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskStatus
import ch.eureka.eurekapp.model.tasks.TaskDependenciesViewModel
import ch.eureka.eurekapp.ui.designsystem.tokens.EColors.BorderGrayColor
import ch.eureka.eurekapp.ui.designsystem.tokens.EColors.DarkBackground
import ch.eureka.eurekapp.ui.designsystem.tokens.EColors.GrayTextColor2
import ch.eureka.eurekapp.ui.theme.DarkColorScheme
import ch.eureka.eurekapp.ui.theme.LightColorScheme
import ch.eureka.eurekapp.ui.theme.Typography

/**
 * ⚠️ Disclaimer: The following documentation was written by AI (ChatGPT - GPT-5).
 *
 * Contains utility functions that generate test tags for the Task Dependencies screen, improving
 * test automation and component targeting.
 */
object TaskDependenciesScreenTestTags {
  fun getFilteringNameTestTag(identifier: String): String {
    return "filtering_name_test_tag_$identifier"
  }

  fun getDependentTaskTestTag(task: Task): String {
    return "dependent_task_test_tag_${task.taskID}"
  }
}

/**
 * ⚠️ Disclaimer: This composable description was written by AI (ChatGPT - GPT-5).
 *
 * Main composable for displaying task dependencies and user-based filters.
 *
 * @param projectId ID of the current project.
 * @param taskId ID of the root task being visualized.
 * @param taskDependenciesViewModel ViewModel used to retrieve task and user data.
 */
@Composable
fun TaskDependenciesScreen(
    projectId: String,
    taskId: String,
    taskDependenciesViewModel: TaskDependenciesViewModel = viewModel()
) {
  val projectUsers =
      taskDependenciesViewModel.getProjectUsers(projectId).collectAsState(emptyList())

  val task = taskDependenciesViewModel.getTaskFromRepository(projectId, taskId).collectAsState(null)

  var nameFilter by remember { mutableStateOf("All") }

  Column(
      modifier = Modifier.padding(vertical = 2.dp, horizontal = 10.dp),
      horizontalAlignment = Alignment.CenterHorizontally) {
        ScreenTitle()
        /*
        Filter section
        **/
        SurfaceComposable(
            title = "Filtering",
            composable = {
              FlowRow(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                  horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        modifier =
                            Modifier.testTag(
                                TaskDependenciesScreenTestTags.getFilteringNameTestTag("All")),
                        onClick = { nameFilter = "All" },
                        colors =
                            ButtonDefaults.outlinedButtonColors(
                                contentColor =
                                    if (nameFilter == "All") LightColorScheme.onPrimary
                                    else DarkColorScheme.background,
                                containerColor =
                                    if (nameFilter == "All") LightColorScheme.primary
                                    else LightColorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(width = 1.dp, BorderGrayColor)) {
                          Text("All", style = Typography.labelSmall)
                        }
                    projectUsers.value.mapNotNull { user ->
                      if (user == null) {
                        return@mapNotNull null
                      } else {
                        OutlinedButton(
                            modifier =
                                Modifier.testTag(
                                    TaskDependenciesScreenTestTags.getFilteringNameTestTag(
                                        user.displayName)),
                            onClick = { nameFilter = user.uid },
                            colors =
                                ButtonDefaults.outlinedButtonColors(
                                    contentColor =
                                        if (nameFilter == user.uid) LightColorScheme.onPrimary
                                        else DarkColorScheme.background,
                                    containerColor =
                                        if (nameFilter == user.uid) LightColorScheme.primary
                                        else LightColorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(width = 1.dp, BorderGrayColor)) {
                              Text(user.displayName, style = Typography.labelSmall)
                            }
                      }
                    }
                  }
            })
        val verticalScrollState = rememberScrollState()
        val horizontalScrollState = rememberScrollState()

        /*
        Dependencies screen
        **/
        SurfaceComposable(
            title = "Dependencies",
            composable = {
              if (task.value != null) {
                Row(
                    modifier = Modifier.padding(vertical = 5.dp, horizontal = 5.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically) {
                      Box(
                          modifier =
                              Modifier.weight(1f)
                                  .verticalScroll(verticalScrollState)
                                  .horizontalScroll(horizontalScrollState)
                                  .fillMaxWidth(),
                          contentAlignment = Alignment.TopCenter) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                              TreeView(
                                  isParent = true,
                                  projectId = projectId,
                                  task = task.value,
                                  taskDependenciesViewModel = taskDependenciesViewModel,
                                  filterName = nameFilter)
                            }
                          }
                    }
              }
            })
      }
}

@Composable
private fun ScreenTitle() {
  Column(
      modifier = Modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.Start,
  ) {
    Text(text = "Dependencies", style = Typography.titleLarge, fontWeight = FontWeight(600))
    Text(
        text = "Visualise order and detect blocking",
        style = Typography.titleMedium,
        color = GrayTextColor2)
  }
}

/**
 * ⚠️ Disclaimer: This documentation was written by AI (ChatGPT - GPT-5).
 *
 * Displays a task and its dependent tasks recursively in a tree structure.
 */
@Composable
fun TreeView(
    modifier: Modifier = Modifier,
    isParent: Boolean = false,
    projectId: String,
    task: Task?,
    taskDependenciesViewModel: TaskDependenciesViewModel,
    filterName: String
) {
  if (task != null) {
    var tasksDependentOn =
        taskDependenciesViewModel.getDependentTasksForTask(projectId, task).collectAsState(listOf())
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth().padding(horizontal = 10.dp)) {
          if (!isParent) {
            Icon(Icons.AutoMirrored.Filled.ArrowRightAlt, null)
          }
          TaskSurfaceComponent(task, filterName)
          EqualWidthChildrenColumn(
              tasksDependentOn.value
                  .filter { task2 ->
                    return@filter task2 != null &&
                        (task2.status == TaskStatus.TODO || task2.status == TaskStatus.IN_PROGRESS)
                  }
                  .map { task2 ->
                    {
                      key(task2.hashCode()) {
                        if (task2 != null) {
                          TreeView(
                              modifier =
                                  Modifier.testTag(
                                      TaskDependenciesScreenTestTags.getDependentTaskTestTag(
                                          task2)),
                              projectId = projectId,
                              task = task2,
                              taskDependenciesViewModel = taskDependenciesViewModel,
                              filterName = filterName)
                        }
                      }
                    }
                  })
        }
  }
}

/**
 * ⚠️ Disclaimer: This documentation was written by AI (ChatGPT - GPT-5).
 *
 * Ensures that child composables in a column have uniform width and height.
 */
@Composable
fun EqualWidthChildrenColumn(children: List<@Composable () -> Unit>) {
  SubcomposeLayout { constraints ->
    val placeables =
        children.mapNotNull { child ->
          val measurables = subcompose(child.hashCode(), child)
          val placeable =
              measurables
                  .firstOrNull()
                  ?.measure(constraints.copy(minHeight = 0, maxHeight = Constraints.Infinity))
          placeable
        }

    val maxWidth = placeables.maxOfOrNull { it.width } ?: 0
    val maxHeight = placeables.maxOfOrNull { it.height } ?: 0

    val equalHeightConstraints = constraints.copy(minHeight = maxHeight, maxHeight = maxHeight)

    val equalHeightPlaceables =
        children.mapNotNull { child ->
          val measurables = subcompose("layout_$child", child)
          measurables.firstOrNull()?.measure(equalHeightConstraints)
        }

    layout(width = maxWidth, height = maxHeight * equalHeightPlaceables.size) {
      var yPosition = 0
      equalHeightPlaceables.forEach { placeable ->
        placeable.placeRelative(x = 0, y = yPosition)
        yPosition += maxHeight
      }
    }
  }
}

/**
 * ⚠️ Disclaimer: This documentation was written by AI (ChatGPT - GPT-5).
 *
 * Represents an individual task as a clickable surface component.
 */
@Composable
fun TaskSurfaceComponent(task: Task, filterName: String = "All") {
  val taskContainsFilterName = task.assignedUserIds.contains(filterName)
  OutlinedButton(
      modifier = Modifier,
      onClick = {},
      colors =
          ButtonDefaults.outlinedButtonColors(
              contentColor = DarkColorScheme.background,
              containerColor =
                  if (taskContainsFilterName) LightColorScheme.primary
                  else LightColorScheme.surface),
      shape = RoundedCornerShape(16.dp),
      border = BorderStroke(width = 1.dp, BorderGrayColor)) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically) {
              Text(
                  task.title,
                  style = Typography.labelSmall,
                  color =
                      if (taskContainsFilterName) LightColorScheme.onPrimary else DarkBackground)
            }
      }
}

@Composable
fun SurfaceComposable(composable: @Composable () -> Unit, title: String) {
  Row(
      modifier = Modifier.padding(10.dp),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier =
                Modifier.border(
                        border = BorderStroke(width = 1.dp, color = BorderGrayColor),
                        shape = RoundedCornerShape(16.dp))
                    .fillMaxWidth(0.95f),
            shadowElevation = 3.dp,
            color = Color.White,
            shape = RoundedCornerShape(16.dp)) {
              Column(modifier = Modifier.padding(vertical = 7.dp)) {
                Text(
                    title,
                    style = Typography.titleMedium,
                    fontWeight = FontWeight(500),
                    modifier = Modifier.padding(horizontal = 13.dp))
                composable()
              }
            }
      }
}
