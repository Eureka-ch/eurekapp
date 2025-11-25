/*
Note: This file was co-authored by Claude Code.
Note: This file was co-authored by Grok.
Portions of the code in this file are inspired by the Bootcamp solution B3 provided by the SwEnt staff.
*/
package ch.eureka.eurekapp.model.data.activity

/**
 * Enum representing the type of action performed on an entity.
 *
 * Note: This file was co-authored by Claude Code.
 */
enum class ActivityType {
  /** Entity was created */
  CREATED,

  /** Entity was updated/modified */
  UPDATED,

  /** Entity was deleted/removed */
  DELETED,

  /** File was uploaded */
  UPLOADED,

  /** Content was shared with others */
  SHARED,

  /** Comment or note was added */
  COMMENTED,

  /** Status was changed (e.g., meeting status, task status) */
  STATUS_CHANGED,

  /** User joined or was added */
  JOINED,

  /** User left or was removed */
  LEFT
}
