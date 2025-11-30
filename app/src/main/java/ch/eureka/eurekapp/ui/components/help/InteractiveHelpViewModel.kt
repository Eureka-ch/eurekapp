package ch.eureka.eurekapp.ui.components.help

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for managing interactive help state. Handles user name resolution and dialog state
 * following MVVM pattern.
 */
class InteractiveHelpViewModel(
    private val getCurrentUserDisplayName: () -> String? = {
      FirebaseAuth.getInstance().currentUser?.displayName
    }
) : ViewModel() {

  private val _isDialogOpen = MutableStateFlow(false)
  val isDialogOpen: StateFlow<Boolean> = _isDialogOpen.asStateFlow()

  /**
   * Resolves the user name to display in help content. Uses provided name if available, otherwise
   * falls back to Firebase current user.
   */
  fun resolveUserName(userProvidedName: String?): String {
    return when {
      !userProvidedName.isNullOrBlank() -> userProvidedName
      else -> getCurrentUserDisplayName().orEmpty()
    }.ifBlank { "there" }
  }

  fun openDialog() {
    _isDialogOpen.value = true
  }

  fun closeDialog() {
    _isDialogOpen.value = false
  }
}
