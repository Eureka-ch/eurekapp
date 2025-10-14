package ch.eureka.eurekapp.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.data.task.FirestoreTaskRepository
import ch.eureka.eurekapp.model.data.task.TaskRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TaskUiState(
    val selectedFilter: TaskFilter = TaskFilter.MINE,
    val myTasks: List<TaskUiModel> = emptyList(),
    val allTasks: List<TaskUiModel> = emptyList(),
    val recentCompleted: List<TaskUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

enum class TaskFilter {
  MINE,
  TEAM,
  THIS_WEEK,
  ALL,
  PROJECT
}

/**
 * ViewModel for managing task state and data loading
 * Connected to TaskRepository for real-time Firebase data
 */
class TaskViewModel : ViewModel() {
  
  private val taskRepository: TaskRepository = FirestoreTaskRepository(
    FirebaseFirestore.getInstance(),
    FirebaseAuth.getInstance()
  )

  private val _uiState = MutableStateFlow(TaskUiState())
  val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

  private var currentJob: Job? = null

  init {
    loadTasks()
  }

  fun loadTasks() {
    currentJob?.cancel()
    currentJob = viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoading = true, error = null)
      
      try {
        taskRepository.getTasksForCurrentUser().collect { tasks ->
          val taskUiModels = tasks.map { task ->
            TaskUiModel(
              task = task,
              template = null, // TODO: Load template if needed
              assignee = null,  // TODO: Load assignee if needed
              progress = 0.0f,
              isCompleted = task.status == ch.eureka.eurekapp.model.data.task.TaskStatus.COMPLETED
            )
          }
          
          _uiState.value = _uiState.value.copy(
            myTasks = taskUiModels,
            isLoading = false,
            error = null
          )
        }
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(
          isLoading = false,
          error = e.message ?: "Failed to load tasks"
        )
      }
    }
  }

  fun setFilter(filter: TaskFilter) {
    _uiState.value = _uiState.value.copy(selectedFilter = filter)

    when (filter) {
      TaskFilter.MINE -> loadTasks()
      TaskFilter.TEAM -> loadTeamTasks()
      TaskFilter.THIS_WEEK -> loadThisWeekTasks()
      TaskFilter.ALL -> loadAllTasks()
      TaskFilter.PROJECT -> {
        // TODO: Get workspaceId, groupId, projectId from context
        // For now, use placeholder values
        loadProjectTasks("workspaceId", "groupId", "projectId")
      }
    }
  }

  private fun loadAllTasks() {
    currentJob?.cancel()
    currentJob = viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoading = true, error = null)
      
      try {
        // For now, use getTasksForCurrentUser as fallback
        // TODO: Implement getAllTasks() in repository if needed
        taskRepository.getTasksForCurrentUser().collect { tasks ->
          val taskUiModels = tasks.map { task ->
            TaskUiModel(
              task = task,
              template = null,
              assignee = null,
              progress = 0.0f,
              isCompleted = task.status == ch.eureka.eurekapp.model.data.task.TaskStatus.COMPLETED
            )
          }
          
          _uiState.value = _uiState.value.copy(
            allTasks = taskUiModels,
            isLoading = false,
            error = null
          )
        }
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(
          isLoading = false,
          error = e.message ?: "Failed to load all tasks"
        )
      }
    }
  }

  private fun loadTeamTasks() {
    currentJob?.cancel()
    currentJob = viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoading = true, error = null)
      
      try {
        // For now, use same data as myTasks (team tasks would need additional logic)
        taskRepository.getTasksForCurrentUser().collect { tasks ->
          val taskUiModels = tasks.map { task ->
            TaskUiModel(
              task = task,
              template = null,
              assignee = null,
              progress = 0.0f,
              isCompleted = task.status == ch.eureka.eurekapp.model.data.task.TaskStatus.COMPLETED
            )
          }
          
          _uiState.value = _uiState.value.copy(
            allTasks = taskUiModels,
            isLoading = false,
            error = null
          )
        }
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(
          isLoading = false,
          error = e.message ?: "Failed to load team tasks"
        )
      }
    }
  }

  private fun loadThisWeekTasks() {
    currentJob?.cancel()
    currentJob = viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoading = true, error = null)
      
      try {
        // Filter tasks due this week
        taskRepository.getTasksForCurrentUser().collect { tasks ->
          val thisWeekTasks = tasks.filter { task ->
            task.dueDate?.let { dueDate ->
              val now = java.util.Date()
              val diffInDays = (dueDate.toDate().time - now.time) / (1000 * 60 * 60 * 24)
              diffInDays >= 0 && diffInDays <= 7
            } ?: false
          }
          
          val taskUiModels = thisWeekTasks.map { task ->
            TaskUiModel(
              task = task,
              template = null,
              assignee = null,
              progress = 0.0f,
              isCompleted = task.status == ch.eureka.eurekapp.model.data.task.TaskStatus.COMPLETED
            )
          }
          
          _uiState.value = _uiState.value.copy(
            allTasks = taskUiModels,
            isLoading = false,
            error = null
          )
        }
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(
          isLoading = false,
          error = e.message ?: "Failed to load this week tasks"
        )
      }
    }
  }

  private fun loadProjectTasks(workspaceId: String, groupId: String, projectId: String) {
    currentJob?.cancel()
    currentJob = viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoading = true, error = null)
      
      try {
        taskRepository.getTasksInProject(projectId).collect { tasks ->
          val taskUiModels = tasks.map { task ->
            TaskUiModel(
              task = task,
              template = null,
              assignee = null,
              progress = 0.0f,
              isCompleted = task.status == ch.eureka.eurekapp.model.data.task.TaskStatus.COMPLETED
            )
          }
          
          _uiState.value = _uiState.value.copy(
            allTasks = taskUiModels,
            isLoading = false,
            error = null
          )
        }
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(
          isLoading = false,
          error = e.message ?: "Failed to load project tasks"
        )
      }
    }
  }

  // TODO: Add these methods when repository is ready
  /*
  private suspend fun createTaskUiModel(task: Task): TaskUiModel {
      // Get template
      val template = taskTemplateRepository.getTemplateById(task.templateId)

      // Get assignee (first assigned user)
      val assignee = if (task.assignedUserIds.isNotEmpty()) {
          userRepository.getUserById(task.assignedUserIds.first())
      } else {
          null
      }

      return TaskUiModel(
          task = task,
          template = template,
          assignee = assignee,
          progress = calculateProgress(task),
          isCompleted = isTaskCompleted(task)
      )
  }

  private fun calculateProgress(task: Task): Float {
      // Progress calculation logic
      return 0.5f // Placeholder
  }

  private fun isTaskCompleted(task: Task): Boolean {
      // Completion logic
      return false // Placeholder
  }
  */
}
