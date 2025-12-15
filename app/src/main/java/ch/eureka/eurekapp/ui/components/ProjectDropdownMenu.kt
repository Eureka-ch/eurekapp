/* Portions of this file were written with the help of Gemini, GPT-5 Codex and Claude 4.5 Sonnet. */
package ch.eureka.eurekapp.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.ui.designsystem.tokens.Spacing

/** Test tags for the [ProjectDropdownMenu] composable. */
object ProjectDropDownMenuTestTag {
  const val PROJECT_DROPDOWN_MENU = "ProjectDropDown"
  const val DROPDOWN_MENU_ITEM = "DropDownMenuItem"
}

/**
 * A reusable Dropdown Menu component for selecting a Project.
 *
 * This component displays a label, a text field showing the current selection, and a dropdown list
 * of available projects. It handles loading states by showing a circular progress indicator.
 *
 * @param projectsList The list of [Project] items to display in the dropdown menu.
 * @param selectedProject The currently selected [Project], or null if no selection has been made.
 * @param isLoadingProjects If true, displays a loading indicator in the text field instead of the
 *   dropdown arrow.
 * @param projectDropdownExpanded Controls the visibility of the dropdown menu.
 * @param onExpandedChange Callback triggered when the dropdown menu needs to be expanded or
 *   collapsed.
 * @param onProjectSelect Callback triggered when a specific [Project] is selected from the list.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDropdownMenu(
    projectsList: List<Project>,
    selectedProject: Project?,
    isLoadingProjects: Boolean,
    projectDropdownExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onProjectSelect: (Project) -> Unit,
) {
  Text(
      text = "Project", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
  Spacer(modifier = Modifier.height(Spacing.xs))

  ExposedDropdownMenuBox(
      expanded = projectDropdownExpanded,
      onExpandedChange = onExpandedChange,
      modifier = Modifier.testTag(ProjectDropDownMenuTestTag.PROJECT_DROPDOWN_MENU)) {
        OutlinedTextField(
            value = selectedProject?.name ?: "",
            onValueChange = {},
            readOnly = true,
            placeholder = { Text("Select a project") },
            trailingIcon = {
              if (isLoadingProjects) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(end = 8.dp), strokeWidth = 2.dp)
              } else {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = projectDropdownExpanded)
              }
            },
            modifier =
                Modifier.fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable))

        ExposedDropdownMenu(
            expanded = projectDropdownExpanded, onDismissRequest = { onExpandedChange(false) }) {
              projectsList.forEach { project ->
                DropdownMenuItem(
                    text = { Text(project.name) },
                    onClick = { onProjectSelect(project) },
                    modifier = Modifier.testTag(ProjectDropDownMenuTestTag.DROPDOWN_MENU_ITEM))
              }
            }
      }
}
