/*
 * This file was co-authored by Claude Code.
 */
package ch.eureka.eurekapp.ui.activity

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.R

/**
 * A collapsible search bar for filtering activities.
 *
 * This component provides a text field that can be shown/hidden for searching activities. It
 * includes a clear button when text is entered.
 *
 * @param query The current search query.
 * @param onQueryChange Callback when the search query changes.
 * @param expanded Whether the search bar is currently visible.
 * @param modifier Optional modifier for styling.
 */
@Composable
fun ActivitySearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    expanded: Boolean,
    modifier: Modifier = Modifier
) {
  AnimatedVisibility(
      visible = expanded,
      enter = expandVertically(),
      exit = shrinkVertically(),
      modifier = modifier) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text(stringResource(R.string.activity_search_placeholder)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = stringResource(R.string.activity_search_icon)) },
            trailingIcon = {
              if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                  Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.activity_search_clear_icon))
                }
              }
            },
            singleLine = true)
      }
}
