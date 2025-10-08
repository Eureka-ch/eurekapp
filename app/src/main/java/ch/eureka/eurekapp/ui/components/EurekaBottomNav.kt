package ch.eureka.eurekapp.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

/** Bottom navigation bar that appears on all main screens */
@Composable
fun EurekaBottomNav(currentRoute: String, onNavigate: (String) -> Unit) {
  val navItems =
      listOf(
          NavItem("Tasks", null),
          NavItem("Ideas", null),
          NavItem("Home", null),
          NavItem("Meetings", null),
          NavItem("Profile", null))

  NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
    navItems.forEach { item ->
      NavigationBarItem(
          icon = {
            Text(
                text = item.label.take(1),
                color =
                    if (currentRoute == item.label) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant)
          },
          label = {
            Text(
                item.label,
                color =
                    if (currentRoute == item.label) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant)
          },
          selected = currentRoute == item.label,
          onClick = { onNavigate(item.label) })
    }
  }
}

data class NavItem(val label: String, val icon: ImageVector?)
