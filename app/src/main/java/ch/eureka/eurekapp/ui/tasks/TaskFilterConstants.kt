package ch.eureka.eurekapp.ui.tasks

/**
 * Constants for task filter options Centralizes filter option strings for better maintainability
 * and localization
 */
object TaskFilterConstants {
  const val FILTER_MY_TASKS = "My tasks"
  const val FILTER_TEAM = "Team"
  const val FILTER_THIS_WEEK = "This week"
  const val FILTER_ALL = "All"
  const val FILTER_PROJECT = "Project"

  val FILTER_OPTIONS =
      listOf(FILTER_MY_TASKS, FILTER_TEAM, FILTER_THIS_WEEK, FILTER_ALL, FILTER_PROJECT)
}
