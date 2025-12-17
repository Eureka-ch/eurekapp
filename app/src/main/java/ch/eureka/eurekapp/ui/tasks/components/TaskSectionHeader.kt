package ch.eureka.eurekapp.ui.tasks.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import ch.eureka.eurekapp.R
import ch.eureka.eurekapp.ui.designsystem.tokens.Spacing

@Composable
fun TaskSectionHeader(modifier: Modifier = Modifier, title: String, taskCount: Int? = null) {
  Column(modifier = modifier.fillMaxWidth().padding(vertical = Spacing.sm)) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface)

    if (taskCount != null) {
      val taskText =
          when (taskCount) {
            0 -> stringResource(R.string.task_section_no_tasks)
            1 -> stringResource(R.string.task_section_task_singular)
            else -> stringResource(R.string.task_section_task_plural)
          }
      Text(
          text = "$taskCount $taskText",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(top = Spacing.xs))
    }
  }
}
