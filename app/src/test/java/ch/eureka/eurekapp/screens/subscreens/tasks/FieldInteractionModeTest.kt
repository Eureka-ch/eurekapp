package ch.eureka.eurekapp.screens.subscreens.tasks

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for FieldInteractionMode and its extension functions.
 *
 * Portions of this code were generated with the help of Claude Sonnet 4.5. This code was written
 * with help of Claude.
 */
class FieldInteractionModeTest {

  @Test
  fun editOnly_returnsTrueWithIsEditing() {
    val mode = FieldInteractionMode.EditOnly
    assertTrue(mode.isEditing)
  }

  @Test
  fun editOnly_returnsFalseWithIsViewOnly() {
    val mode = FieldInteractionMode.EditOnly
    assertFalse(mode.isViewOnly)
  }

  @Test
  fun editOnly_returnsFalseWithCanToggle() {
    val mode = FieldInteractionMode.EditOnly
    assertFalse(mode.canToggle)
  }

  @Test
  fun editOnly_returnsSameModeWithToggleEditingState() {
    val mode = FieldInteractionMode.EditOnly
    val toggled = mode.toggleEditingState()
    assertTrue(toggled is FieldInteractionMode.EditOnly)
  }

  @Test
  fun viewOnly_returnsFalseWithIsEditing() {
    val mode = FieldInteractionMode.ViewOnly
    assertFalse(mode.isEditing)
  }

  @Test
  fun viewOnly_returnsTrueWithIsViewOnly() {
    val mode = FieldInteractionMode.ViewOnly
    assertTrue(mode.isViewOnly)
  }

  @Test
  fun viewOnly_returnsFalseWithCanToggle() {
    val mode = FieldInteractionMode.ViewOnly
    assertFalse(mode.canToggle)
  }

  @Test
  fun viewOnly_returnsSameModeWithToggleEditingState() {
    val mode = FieldInteractionMode.ViewOnly
    val toggled = mode.toggleEditingState()
    assertTrue(toggled is FieldInteractionMode.ViewOnly)
  }

  @Test
  fun toggleable_returnsTrueWithIsEditingWhenEditingTrue() {
    val mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = true)
    assertTrue(mode.isEditing)
  }

  @Test
  fun toggleable_returnsFalseWithIsViewOnlyWhenEditingTrue() {
    val mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = true)
    assertFalse(mode.isViewOnly)
  }

  @Test
  fun toggleable_returnsFalseWithIsEditingWhenEditingFalse() {
    val mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = false)
    assertFalse(mode.isEditing)
  }

  @Test
  fun toggleable_returnsTrueWithIsViewOnlyWhenEditingFalse() {
    val mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = false)
    assertTrue(mode.isViewOnly)
  }

  @Test
  fun toggleable_returnsTrueWithCanToggle() {
    val modeEditing = FieldInteractionMode.Toggleable(isCurrentlyEditing = true)
    val modeViewing = FieldInteractionMode.Toggleable(isCurrentlyEditing = false)
    assertTrue(modeEditing.canToggle)
    assertTrue(modeViewing.canToggle)
  }

  @Test
  fun toggleable_returnsFalseWithToggleEditingStateWhenEditingTrue() {
    val mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = true)
    val toggled = mode.toggleEditingState()
    assertTrue(toggled is FieldInteractionMode.Toggleable)
    assertFalse((toggled as FieldInteractionMode.Toggleable).isCurrentlyEditing)
  }

  @Test
  fun toggleable_returnsTrueWithToggleEditingStateWhenEditingFalse() {
    val mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = false)
    val toggled = mode.toggleEditingState()
    assertTrue(toggled is FieldInteractionMode.Toggleable)
    assertTrue((toggled as FieldInteractionMode.Toggleable).isCurrentlyEditing)
  }

  @Test
  fun toggleable_returnsOriginalStateWithDoubleToggle() {
    val mode = FieldInteractionMode.Toggleable(isCurrentlyEditing = true)
    val toggledTwice = mode.toggleEditingState().toggleEditingState()
    assertTrue(toggledTwice is FieldInteractionMode.Toggleable)
    assertTrue((toggledTwice as FieldInteractionMode.Toggleable).isCurrentlyEditing)
  }
}
