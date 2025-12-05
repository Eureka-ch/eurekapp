// Portions of this code were generated with the help of Grok.
package ch.eureka.eurekapp.testutils

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import ch.eureka.eurekapp.navigation.Route
import ch.eureka.eurekapp.screens.Camera

fun NavGraphBuilder.testCameraRoute(
    navController: NavHostController,
    onBackClick: () -> Unit = { navController.popBackStack() },
    onPhotoSaved: (String) -> Unit = { uri ->
      navController.previousBackStackEntry?.savedStateHandle?.set("photoUri", uri)
      navController.popBackStack()
    }
) {
  composable<Route.Camera> { Camera(onBackClick = onBackClick, onPhotoSaved = onPhotoSaved) }
}
