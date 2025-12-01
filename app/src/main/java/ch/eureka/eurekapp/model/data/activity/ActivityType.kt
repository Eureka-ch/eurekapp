/*
Note: This file was co-authored by Claude Code.
Note: This file was co-authored by Grok.
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
  LEFT,

  /** User was assigned to an entity (e.g., task) */
  ASSIGNED,

  /** User was unassigned from an entity (e.g., task) */
  UNASSIGNED,

  /** User role was changed */
  ROLE_CHANGED,

  /** File was downloaded */
  DOWNLOADED
}
