/* Portions of this file were written with the help of Gemini and ChatGPT (GPT-5) */
package ch.eureka.eurekapp.model.data

import android.annotation.SuppressLint
import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import ch.eureka.eurekapp.model.data.chat.FirestoreChatRepository
import ch.eureka.eurekapp.model.data.file.FileStorageRepository
import ch.eureka.eurekapp.model.data.file.FirebaseFileStorageRepository
import ch.eureka.eurekapp.model.data.invitation.FirestoreInvitationRepository
import ch.eureka.eurekapp.model.data.meeting.FirestoreMeetingRepository
import ch.eureka.eurekapp.model.data.notes.FirestoreSelfNotesRepository
import ch.eureka.eurekapp.model.data.notes.UnifiedSelfNotesRepository
import ch.eureka.eurekapp.model.data.prefs.UserPreferencesRepository
import ch.eureka.eurekapp.model.data.project.FirestoreProjectRepository
import ch.eureka.eurekapp.model.data.task.FirestoreTaskRepository
import ch.eureka.eurekapp.model.data.template.FirestoreTaskTemplateRepository
import ch.eureka.eurekapp.model.data.transcription.CloudFunctionSpeechToTextRepository
import ch.eureka.eurekapp.model.data.transcription.SpeechToTextRepository
import ch.eureka.eurekapp.model.data.user.FirestoreUserRepository
import ch.eureka.eurekapp.model.database.AppDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage

/**
 * Central singleton provider for all Data Layer repositories in the application.
 *
 * This object acts as a simple Service Locator, managing the lifecycle and initialization of
 * repositories. It handles both Cloud-based repositories (Firestore, Firebase Storage) and
 * Local/Hybrid repositories (Room, DataStore).
 *
 * Usage:
 * 1. Must be initialized in [ch.eureka.eurekapp.MainActivity.onCreate] via [initialize].
 * 2. Repositories can then be accessed globally via the public properties.
 */
@SuppressLint("StaticFieldLeak") // Context is application context, so leak is not an issue
object RepositoriesProvider {

  private var applicationContext: Context? = null

  /**
   * Initializes the provider with the application context.
   *
   * This method is mandatory and must be called before accessing any repositories that rely on
   * local storage (e.g., Room Database, DataStore, WorkManager).
   *
   * @param context The Android application context.
   */
  fun initialize(context: Context) {
    applicationContext = context.applicationContext
  }

  /** Backing field for the Task repository. */
  private val _taskRepository: FirestoreTaskRepository by lazy {
    FirestoreTaskRepository(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance())
  }

  /** Backing field for the Project repository. */
  private val _projectRepository: FirestoreProjectRepository by lazy {
    FirestoreProjectRepository(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance())
  }

  /** Backing field for the File Storage repository. */
  private val _fileRepository: FileStorageRepository by lazy {
    FirebaseFileStorageRepository(FirebaseStorage.getInstance(), FirebaseAuth.getInstance())
  }

  /** Backing field for the Chat repository. */
  private val _chatRepository: FirestoreChatRepository by lazy {
    FirestoreChatRepository(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance())
  }

  /** Backing field for the Invitation repository. */
  private val _invitationRepository: FirestoreInvitationRepository by lazy {
    FirestoreInvitationRepository(FirebaseFirestore.getInstance())
  }

  /** Backing field for the Meeting repository. */
  private val _meetingRepository: FirestoreMeetingRepository by lazy {
    FirestoreMeetingRepository(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance())
  }

  /** Backing field for the Task Template repository. */
  private val _taskTemplateRepository: FirestoreTaskTemplateRepository by lazy {
    FirestoreTaskTemplateRepository(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance())
  }

  /** Backing field for the User repository. */
  private val _userRepository: FirestoreUserRepository by lazy {
    FirestoreUserRepository(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance())
  }

  /** Backing field for the Speech-to-Text repository. */
  private val _speechToTextRepository: SpeechToTextRepository by lazy {
    CloudFunctionSpeechToTextRepository(
        FirebaseFirestore.getInstance(),
        FirebaseAuth.getInstance(),
        FirebaseFunctions.getInstance())
  }

  /**
   * Backing field for the User Preferences repository. Throws [IllegalStateException] if
   * [initialize] has not been called.
   */
  private val _userPreferencesRepository: UserPreferencesRepository by lazy {
    val context =
        applicationContext
            ?: throw IllegalStateException(
                "RepositoryProvider.initialize(context) must be called first")
    UserPreferencesRepository(context)
  }

  /** Provides access to the [UserPreferencesRepository] for managing DataStore settings. */
  val userPreferencesRepository: UserPreferencesRepository
    get() = _userPreferencesRepository

  /**
   * Provides access to the system [WorkManager] for background tasks. Throws
   * [IllegalStateException] if [initialize] has not been called.
   */
  val workManager: WorkManager
    get() {
      val context =
          applicationContext
              ?: throw IllegalStateException(
                  "RepositoryProvider.initialize(context) must be called first")
      return WorkManager.getInstance(context)
    }

  /**
   * Backing field for the Unified Self Notes repository. Initializes the local Room database and
   * combines it with the Firestore repository. Throws [IllegalStateException] if [initialize] has
   * not been called.
   */
  private val _unifiedSelfNotesRepository: UnifiedSelfNotesRepository by lazy {
    val context =
        applicationContext
            ?: throw IllegalStateException(
                "RepositoryProvider.initialize(context) must be called first")

    val database = Room.databaseBuilder(context, AppDatabase::class.java, "eureka_local.db").build()

    val firestoreRepo =
        FirestoreSelfNotesRepository(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance())

    UnifiedSelfNotesRepository(
        context = context, // Pass the context for network checks
        localDao = database.messageDao(),
        firestoreRepo = firestoreRepo,
        userPreferences = _userPreferencesRepository,
        auth = FirebaseAuth.getInstance())
  }

  /** Provides access to the repository for managing Tasks. */
  val taskRepository: FirestoreTaskRepository
    get() = _taskRepository

  /** Provides access to the repository for managing Projects. */
  val projectRepository: FirestoreProjectRepository
    get() = _projectRepository

  /** Provides access to the repository for managing File uploads and downloads. */
  val fileRepository: FileStorageRepository
    get() = _fileRepository

  /** Provides access to the repository for Chat messages. */
  val chatRepository: FirestoreChatRepository
    get() = _chatRepository

  /** Provides access to the repository for managing Project Invitations. */
  val invitationRepository: FirestoreInvitationRepository
    get() = _invitationRepository

  /** Provides access to the repository for managing Meetings. */
  val meetingRepository: FirestoreMeetingRepository
    get() = _meetingRepository

  /** Provides access to the repository for managing Task Templates. */
  val taskTemplateRepository: FirestoreTaskTemplateRepository
    get() = _taskTemplateRepository

  /** Provides access to the repository for managing Users. */
  val userRepository: FirestoreUserRepository
    get() = _userRepository

  /** Provides access to the repository for Speech-to-Text services. */
  val speechToTextRepository: SpeechToTextRepository
    get() = _speechToTextRepository

  /**
   * Provides access to the Unified Self Notes repository. This repository handles the logic for
   * "Notes to Self", including:
   * - Local storage via Room (Offline first)
   * - Cloud synchronization via Firestore
   * - Privacy toggle preferences
   */
  val unifiedSelfNotesRepository: UnifiedSelfNotesRepository
    get() = _unifiedSelfNotesRepository
}
