package ch.eureka.eurekapp.screens.subscreens.tasks

/**
 * Sealed interface representing the interaction mode for template field components.
 *
 * This type-safe approach prevents illegal state combinations and makes field behavior explicit.
 *
 * Portions of this code were generated with the help of AI.
 *
 * @see EditOnly for creation flows where fields are always editable
 * @see ViewOnly for read-only display without editing capability
 * @see Toggleable for edit flows where users can switch between viewing and editing
 */
sealed interface FieldInteractionMode {
  /**
   * Edit-only mode for field creation flows.
   *
   * Fields are always editable and cannot be switched to view mode. Used in creation screens where
   * users must provide values.
   */
  data object EditOnly : FieldInteractionMode

  /**
   * View-only mode for read-only field display.
   *
   * Fields are always in view mode and cannot be edited. Used for displaying completed or locked
   * content.
   */
  data object ViewOnly : FieldInteractionMode

  /**
   * Toggleable mode allowing switching between view and edit states.
   *
   * Fields can switch between viewing and editing modes. Used in edit screens where users can
   * toggle editing for specific fields.
   *
   * @property isCurrentlyEditing Whether the field is currently in editing mode
   */
  data class Toggleable(val isCurrentlyEditing: Boolean) : FieldInteractionMode
}

/** Returns true if the field is currently in editing mode. */
val FieldInteractionMode.isEditing: Boolean
  get() =
      when (this) {
        is FieldInteractionMode.EditOnly -> true
        is FieldInteractionMode.ViewOnly -> false
        is FieldInteractionMode.Toggleable -> this.isCurrentlyEditing
      }

/** Returns true if the field is currently in view-only mode (not editable). */
val FieldInteractionMode.isViewOnly: Boolean
  get() = !isEditing

/** Returns true if the field can switch between view and edit modes. */
val FieldInteractionMode.canToggle: Boolean
  get() = this is FieldInteractionMode.Toggleable

/**
 * Toggles the editing state if in Toggleable mode, otherwise returns the same mode.
 *
 * @return A new mode with toggled editing state, or the same mode if not toggleable
 */
fun FieldInteractionMode.toggleEditingState(): FieldInteractionMode =
    when (this) {
      is FieldInteractionMode.Toggleable ->
          FieldInteractionMode.Toggleable(!this.isCurrentlyEditing)
      else -> this
    }
