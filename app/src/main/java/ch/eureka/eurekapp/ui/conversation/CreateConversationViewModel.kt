package ch.eureka.eurekapp.ui.conversation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.connection.ConnectivityObserver
import ch.eureka.eurekapp.model.connection.ConnectivityObserverProvider
import ch.eureka.eurekapp.model.data.FirestoreRepositoriesProvider
import ch.eureka.eurekapp.model.data.conversation.Conversation
import ch.eureka.eurekapp.model.data.conversation.ConversationRepository
import ch.eureka.eurekapp.model.data.project.Member
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.project.ProjectRepository
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.model.data.user.UserRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/*
Co-author: GPT-5 Codex
Co-author: Claude 4.5 Sonnet
*/

/**
 * UI state for the create conversation screen.
 *
 * @property projects List of projects the user belongs to.
 * @property selectedProject Currently selected project, or null if none selected.
 * @property members List of members in the selected project (excluding current user).
 * @property selectedMember Currently selected member to create conversation with.
 * @property isLoadingProjects Whether projects are being loaded.
 * @property isLoadingMembers Whether members are being loaded.
 * @property isCreating Whether a conversation is being created.
 * @property errorMsg Error message to display, or null if no error.
 * @property isConnected Whether the device is connected to the internet.
 * @property conversationCreated True when conversation was successfully created.
 */
data class CreateConversationState(
    val projects: List<Project> = emptyList(),
    val selectedProject: Project? = null,
    val members: List<MemberDisplayData> = emptyList(),
    val selectedMember: MemberDisplayData? = null,
    val isLoadingProjects: Boolean = false,
    val isLoadingMembers: Boolean = false,
    val isCreating: Boolean = false,
    val errorMsg: String? = null,
    val isConnected: Boolean = true,
    val conversationCreated: Boolean = false
)

/**
 * Display data for a member in the selection list.
 *
 * @property member The underlying member data.
 * @property user The user data with display name and photo.
 */
data class MemberDisplayData(val member: Member, val user: User)

/**
 * ViewModel for creating a new conversation.
 *
 * Handles the multi-step flow for creating a 1-on-1 conversation:
 * 1. Load and display projects the user belongs to
 * 2. When a project is selected, load its members (excluding current user)
 * 3. When a member is selected, enable conversation creation
 * 4. Before creating, check if a conversation already exists to prevent duplicates
 *
 * @property conversationRepository Repository for conversation data operations.
 * @property projectRepository Repository for project and member data.
 * @property userRepository Repository for user data lookups.
 * @property getCurrentUserId Function to get the current authenticated user's ID.
 * @property connectivityObserver Observer for network connectivity status.
 */
class CreateConversationViewModel(
    private val conversationRepository: ConversationRepository =
        FirestoreRepositoriesProvider.conversationRepository,
    private val projectRepository: ProjectRepository = FirestoreRepositoriesProvider.projectRepository,
    private val userRepository: UserRepository = FirestoreRepositoriesProvider.userRepository,
    private val getCurrentUserId: () -> String? = { FirebaseAuth.getInstance().currentUser?.uid },
    connectivityObserver: ConnectivityObserver = ConnectivityObserverProvider.connectivityObserver
) : ViewModel() {

  private val _uiState = MutableStateFlow(CreateConversationState())
  val uiState: StateFlow<CreateConversationState> = _uiState

  private val _isConnected =
      connectivityObserver.isConnected.stateIn(viewModelScope, SharingStarted.Eagerly, true)

  init {
    // Observe connectivity changes and update UI state accordingly
    viewModelScope.launch {
      _isConnected.collect { isConnected -> _uiState.update { it.copy(isConnected = isConnected) } }
    }
    // Load available projects on ViewModel creation
    loadProjects()
  }

  /**
   * Load all projects for the current user.
   *
   * Fetches projects from Firestore where the current user is a member. Called automatically on
   * ViewModel initialization.
   */
  fun loadProjects() {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoadingProjects = true) }
      try {
        val projects = projectRepository.getProjectsForCurrentUser().first()
        _uiState.update { it.copy(isLoadingProjects = false, projects = projects) }
      } catch (e: Exception) {
        _uiState.update { it.copy(isLoadingProjects = false, errorMsg = e.message) }
      }
    }
  }

  /**
   * Select a project and load its members.
   *
   * Clears any previously selected member and triggers loading of project members. The current user
   * is excluded from the member list.
   *
   * @param project The project to select.
   */
  fun selectProject(project: Project) {
    // Reset member selection and list when project changes
    _uiState.update {
      it.copy(selectedProject = project, selectedMember = null, members = emptyList())
    }
    // Fetch members for the newly selected project
    loadMembersForProject(project.projectId)
  }

  /**
   * Load members for the selected project, excluding the current user.
   *
   * Fetches all members from the project's members subcollection, then resolves each member's user
   * data (display name, photo) from the users collection.
   *
   * @param projectId The ID of the project to load members for.
   */
  private fun loadMembersForProject(projectId: String) {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoadingMembers = true) }
      try {
        val currentUserId = getCurrentUserId() ?: ""

        // Fetch all members from the project's members subcollection
        val members = projectRepository.getMembers(projectId).first()

        // Filter out the current user - they can't create a conversation with themselves
        val otherMembers = members.filter { it.userId != currentUserId }

        // Resolve user data (display name, photo) for each member
        val memberDisplayDataList = otherMembers.mapNotNull { member ->
          val user = userRepository.getUserById(member.userId).first()
          // Only include members whose user data could be resolved
          user?.let { MemberDisplayData(member = member, user = it) }
        }

        _uiState.update { it.copy(isLoadingMembers = false, members = memberDisplayDataList) }
      } catch (e: Exception) {
        _uiState.update { it.copy(isLoadingMembers = false, errorMsg = e.message) }
      }
    }
  }

  /**
   * Select a member to create a conversation with.
   *
   * @param member The member to select for conversation creation.
   */
  fun selectMember(member: MemberDisplayData) {
    _uiState.update { it.copy(selectedMember = member) }
  }

  /**
   * Create a conversation with the selected member in the selected project.
   *
   * First checks if a conversation already exists between the two users in this project. If so,
   * displays an error message. Otherwise, creates a new conversation document in Firestore.
   *
   * Sets [CreateConversationState.conversationCreated] to true on success, which can be observed by
   * the UI to navigate back to the conversation list.
   */
  fun createConversation() {
    val state = _uiState.value

    // Validate required selections - return early if any are missing
    val selectedProject = state.selectedProject ?: return
    val selectedMember = state.selectedMember ?: return
    val currentUserId = getCurrentUserId() ?: return

    viewModelScope.launch {
      _uiState.update { it.copy(isCreating = true) }

      // Check if a conversation already exists between these two users in this project
      // This prevents duplicate conversations
      val existingConversation = conversationRepository.findExistingConversation(
          projectId = selectedProject.projectId,
          userId1 = currentUserId,
          userId2 = selectedMember.user.uid
      )

      if (existingConversation != null) {
        // Conversation already exists - show error and don't create a duplicate
        _uiState.update {
          it.copy(isCreating = false, errorMsg = "Conversation already exists with this member")
        }
        return@launch
      }

      // Build the new conversation object with both participants
      val conversation = Conversation(
          projectId = selectedProject.projectId,
          memberIds = listOf(currentUserId, selectedMember.user.uid),
          createdBy = currentUserId,
          createdAt = Timestamp.now()
      )

      // Save to Firestore and handle result
      conversationRepository.createConversation(conversation)
          .onSuccess {
            // Signal success to UI for navigation
            _uiState.update { it.copy(isCreating = false, conversationCreated = true) }
          }
          .onFailure { e ->
            _uiState.update { it.copy(isCreating = false, errorMsg = e.message) }
          }
    }
  }

  /** Clear the error message. */
  fun clearErrorMsg() {
    _uiState.update { it.copy(errorMsg = null) }
  }

  /**
   * Reset the conversation created flag.
   *
   * Should be called after the UI has handled the successful creation (e.g., navigated away) to
   * prevent re-triggering navigation on configuration changes.
   */
  fun resetConversationCreated() {
    _uiState.update { it.copy(conversationCreated = false) }
  }
}
