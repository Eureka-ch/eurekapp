/* Portions of this file were written with the help of Gemini and ChatGPT (GPT-5) */
package ch.eureka.eurekapp.model.data

import android.annotation.SuppressLint
import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import ch.eureka.eurekapp.model.data.activity.ActivityRepository
import ch.eureka.eurekapp.model.data.activity.FirestoreActivityRepository
import ch.eureka.eurekapp.model.data.chat.FirestoreChatRepository
import ch.eureka.eurekapp.model.data.conversation.ConversationRepository
import ch.eureka.eurekapp.model.data.conversation.FirestoreConversationRepository
import ch.eureka.eurekapp.model.data.file.FileStorageRepository
import ch.eureka.eurekapp.model.data.file.FirebaseFileStorageRepository
import ch.eureka.eurekapp.model.data.ideas.FirestoreIdeasRepository
import ch.eureka.eurekapp.model.data.ideas.IdeasRepository
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
  private const val ERROR_MSG = "RepositoryProvider.initialize(context) must be called first"

  /**
   * Initializes the provider with the application context.
   *
   * @param context The Android application context.
   */
  fun initialize(context: Context) {
    applicationContext = context.applicationContext
  }

  private val _taskRepository: FirestoreTaskRepository by lazy {
    FirestoreTaskRepository(
        FirebaseFirestore.getInstance(), FirebaseAuth.getInstance(), _projectRepository)
  }

  private val _projectRepository: FirestoreProjectRepository by lazy {
    FirestoreProjectRepository(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance())
  }

  private val _fileRepository: FileStorageRepository by lazy {
    FirebaseFileStorageRepository(FirebaseStorage.getInstance(), FirebaseAuth.getInstance())
  }

  private val _chatRepository: FirestoreChatRepository by lazy {
    FirestoreChatRepository(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance())
  }

  private val _invitationRepository: FirestoreInvitationRepository by lazy {
    FirestoreInvitationRepository(FirebaseFirestore.getInstance())
  }

  private val _meetingRepository: FirestoreMeetingRepository by lazy {
    FirestoreMeetingRepository(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance())
  }

  private val _taskTemplateRepository: FirestoreTaskTemplateRepository by lazy {
    FirestoreTaskTemplateRepository(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance())
  }

  private val _userRepository: FirestoreUserRepository by lazy {
    FirestoreUserRepository(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance())
  }

  private val _speechToTextRepository: SpeechToTextRepository by lazy {
    CloudFunctionSpeechToTextRepository(
        FirebaseFirestore.getInstance(),
        FirebaseAuth.getInstance(),
        FirebaseFunctions.getInstance())
  }

  private val _userPreferencesRepository: UserPreferencesRepository by lazy {
    val context = applicationContext ?: throw IllegalStateException(ERROR_MSG)
    UserPreferencesRepository(context)
  }

  val userPreferencesRepository: UserPreferencesRepository
    get() = _userPreferencesRepository

  val workManager: WorkManager
    get() {
      val context = applicationContext ?: throw IllegalStateException(ERROR_MSG)
      return WorkManager.getInstance(context)
    }

  private val _unifiedSelfNotesRepository: UnifiedSelfNotesRepository by lazy {
    val context = applicationContext ?: throw IllegalStateException(ERROR_MSG)

    val database =
        Room.databaseBuilder(context, AppDatabase::class.java, "eureka_local.db")
            .fallbackToDestructiveMigration(false)
            .build()

    val firestoreRepo =
        FirestoreSelfNotesRepository(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance())

    UnifiedSelfNotesRepository(
        context = context, // Pass the context for network checks
        localDao = database.messageDao(),
        firestoreRepo = firestoreRepo,
        userPreferences = _userPreferencesRepository,
        auth = FirebaseAuth.getInstance()
        // Removed applicationScope argument as sync logic has been removed
        )
  }

  private val _conversationRepository: ConversationRepository by lazy {
    FirestoreConversationRepository(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance())
  }

  private val _activityRepository: ActivityRepository by lazy {
    FirestoreActivityRepository(FirebaseFirestore.getInstance())
  }

  private val _ideasRepository: IdeasRepository by lazy {
    FirestoreIdeasRepository(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance())
  }

  val taskRepository: FirestoreTaskRepository
    get() = _taskRepository

  val projectRepository: FirestoreProjectRepository
    get() = _projectRepository

  val fileRepository: FileStorageRepository
    get() = _fileRepository

  val chatRepository: FirestoreChatRepository
    get() = _chatRepository

  val invitationRepository: FirestoreInvitationRepository
    get() = _invitationRepository

  val meetingRepository: FirestoreMeetingRepository
    get() = _meetingRepository

  val taskTemplateRepository: FirestoreTaskTemplateRepository
    get() = _taskTemplateRepository

  val userRepository: FirestoreUserRepository
    get() = _userRepository

  val speechToTextRepository: SpeechToTextRepository
    get() = _speechToTextRepository

  val unifiedSelfNotesRepository: UnifiedSelfNotesRepository
    get() = _unifiedSelfNotesRepository

  val conversationRepository: ConversationRepository
    get() = _conversationRepository

  val activityRepository: ActivityRepository
    get() = _activityRepository

  val ideasRepository: IdeasRepository
    get() = _ideasRepository
}
