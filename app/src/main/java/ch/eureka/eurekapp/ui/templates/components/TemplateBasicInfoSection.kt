// Portions of this code were generated with the help of Claude Sonnet 4.5 in Claude Code

package ch.eureka.eurekapp.ui.templates.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import ch.eureka.eurekapp.R
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.ui.designsystem.tokens.EurekaStyles

object TemplateBasicInfoSectionTestTags {
  const val TITLE_INPUT = "template_title_input"
  const val DESCRIPTION_INPUT = "template_description_input"
}

/**
 * Section for editing template basic information.
 *
 * @param title Template title
 * @param description Template description
 * @param titleError Validation error for title
 * @param onTitleChange Callback when title changes
 * @param onDescriptionChange Callback when description changes
 */
@Composable
fun TemplateBasicInfoSection(
    title: String,
    description: String,
    titleError: String?,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
  Column(modifier = modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
    OutlinedTextField(
        value = title,
        onValueChange = onTitleChange,
        label = { Text(stringResource(R.string.template_basic_title_label)) },
        isError = titleError != null,
        supportingText = titleError?.let { { Text(it) } },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().testTag(TemplateBasicInfoSectionTestTags.TITLE_INPUT),
        colors = EurekaStyles.textFieldColors())

    OutlinedTextField(
        value = description,
        onValueChange = onDescriptionChange,
        label = { Text(stringResource(R.string.template_basic_description_label)) },
        minLines = 3,
        modifier =
            Modifier.fillMaxWidth().testTag(TemplateBasicInfoSectionTestTags.DESCRIPTION_INPUT),
        colors = EurekaStyles.textFieldColors())
  }
}
