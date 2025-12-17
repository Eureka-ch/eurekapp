package ch.eureka.eurekapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import ch.eureka.eurekapp.ui.designsystem.tokens.EColors

/** Compact red gradient top bar avec texte/icônes blancs, sans décalage vertical custom. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EurekaTopBar(
    modifier: Modifier = Modifier,
    title: String = "EUREKA",
    titleTestTag: String? = null,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
  val gradient =
      Brush.verticalGradient(listOf(EColors.TopBarGradientStart, EColors.TopBarGradientEnd))

  Box(modifier = modifier.fillMaxWidth().background(gradient)) {
    TopAppBar(
        title = {
          Text(
              text = title,
              style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
              color = EColors.WhiteTextColor,
              modifier = titleTestTag?.let { Modifier.testTag(it) } ?: Modifier)
        },
        navigationIcon = {
          navigationIcon?.let {
            CompositionLocalProvider(LocalContentColor provides EColors.WhiteTextColor) { it() }
          }
        },
        actions = {
          CompositionLocalProvider(LocalContentColor provides EColors.WhiteTextColor) { actions() }
        },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = EColors.Transparent,
                titleContentColor = EColors.WhiteTextColor,
                actionIconContentColor = EColors.WhiteTextColor,
                navigationIconContentColor = EColors.WhiteTextColor),
        modifier = Modifier.fillMaxWidth())
  }
}
