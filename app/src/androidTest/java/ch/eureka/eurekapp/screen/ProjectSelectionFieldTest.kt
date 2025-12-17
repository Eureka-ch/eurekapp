// Portions of this file were written with the help of Grok.
package ch.eureka.eurekapp.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.screens.subscreens.tasks.CommonTaskTestTags
import ch.eureka.eurekapp.screens.subscreens.tasks.ProjectSelectionField
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProjectSelectionFieldTest {
  @get:Rule val composeRule = createComposeRule()

  @Test
  fun projectSelectionField_showsErrorWhenProjectsExistAndNoSelection() {
    val projects = listOf(Project(projectId = "p1", name = "Proj 1"))

    composeRule.setContent {
      ProjectSelectionField(projects = projects, selectedProjectId = "", onProjectSelected = {})
    }

    composeRule.onNodeWithTag(CommonTaskTestTags.PROJECT_SELECTION_ERROR).assertIsDisplayed()
  }
}
