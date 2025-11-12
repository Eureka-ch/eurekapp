package ch.eureka.eurekapp.screens.subscreens.tasks

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for FieldInteractionMode and its extension functions.
 *
 * Portions of this code were generated with the help of AI.
 */
class FieldInteractionModeTest {

  @Test
  fun editOnly_isEditing_returnsTrue() {
    val mode = FieldInteractionMode.EditOnly
    assertTrue(mode.isEditing)
  }

  @Test
  fun editOnly_isViewOnly_returnsFalse() {
    val mode = FieldInteractionMode.EditOnly
    assertFalse(mode.isViewOnly)
  }

  @Test
  fun editOnly_canToggle_returnsFalse() {
    val mode = FieldInteractionMode.EditOnly
    assertFalse(mode.canToggle)
  }

  @Test
  fun editOnly_toggleEditingState_returnsSameMode() {
    val mode = FieldInteractionMode.EditOnly
    val toggled = mode.toggleEditingState()
    assertTrue(toggled is FieldInteractionMode.EditOnly)
  }

  @Test
  fun viewOnly_isEditing_returnsFalse() {
    val mode = FieldInteractionMode.ViewOnly
    assertFalse(mode.isEditing)
  }

  @Test
  fun viewOnly_isViewOnly_returnsTrue() {
    val mode = FieldInteractionMode.ViewOnly
    assertTrue(mode.isViewOnly)
  }

  @Test
  fun viewOnly_canToggle_returnsFalse() {
    val mode = FieldInteractionMode.ViewOnly
    assertFalse(mode.canToggle)
  }

  @Test
  fun viewOnly_toggleEditingState_returnsSameMode() {
    val mode = FieldInteractionMode.ViewOnly
    val toggled = mode.toggleEditingState()
    assertTrue(toggled is FieldInteractionMode.ViewOnly)
  }

  @Test
  fun toggleable_whenEditingTrue_isEditing_returnsTrue() {
    val mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = true)
    assertTrue(mode.isEditing)
  }

  @Test
  fun toggleable_whenEditingTrue_isViewOnly_returnsFalse() {
    val mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = true)
    assertFalse(mode.isViewOnly)
  }

  @Test
  fun toggleable_whenEditingFalse_isEditing_returnsFalse() {
    val mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = false)
    assertFalse(mode.isEditing)
  }

  @Test
  fun toggleable_whenEditingFalse_isViewOnly_returnsTrue() {
    val mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = false)
    assertTrue(mode.isViewOnly)
  }

  @Test
  fun toggleable_canToggle_returnsTrue() {
    val modeEditing = FieldInteractionMode.Toggleable(isCurrentlyEditing = true)
    val modeViewing = FieldInteractionMode.Toggleable(isCurrentlyEditing = false)
    assertTrue(modeEditing.canToggle)
    assertTrue(modeViewing.canToggle)
  }

  @Test
  fun toggleable_whenEditingTrue_toggleEditingState_returnsFalse() {
    val mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = true)
    val toggled = mode.toggleEditingState()
    assertTrue(toggled is FieldInteractionMode.Toggleable)
    assertFalse((toggled as FieldInteractionMode.Toggleable).isCurrentlyEditing)
  }

  @Test
  fun toggleable_whenEditingFalse_toggleEditingState_returnsTrue() {
    val mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = false)
    val toggled = mode.toggleEditingState()
    assertTrue(toggled is FieldInteractionMode.Toggleable)
    assertTrue((toggled as FieldInteractionMode.Toggleable).isCurrentlyEditing)
  }

  @Test
  fun toggleable_doubleToggle_returnsOriginalState() {
    val mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = true)
    val toggledTwice = mode.toggleEditingState().toggleEditingState()
    assertTrue(toggledTwice is FieldInteractionMode.Toggleable)
    assertTrue((toggledTwice as FieldInteractionMode.Toggleable).isCurrentlyEditing)
  }
}
