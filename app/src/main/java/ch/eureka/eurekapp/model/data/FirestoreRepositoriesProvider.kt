package ch.eureka.eurekapp.model.data

import ch.eureka.eurekapp.model.data.chat.FirestoreChatRepository
import ch.eureka.eurekapp.model.data.file.FileStorageRepository
import ch.eureka.eurekapp.model.data.file.FirebaseFileStorageRepository
import ch.eureka.eurekapp.model.data.invitation.FirestoreInvitationRepository
import ch.eureka.eurekapp.model.data.meeting.FirestoreMeetingRepository
import ch.eureka.eurekapp.model.data.project.FirestoreProjectRepository
import ch.eureka.eurekapp.model.data.task.FirestoreTaskRepository
import ch.eureka.eurekapp.model.data.template.FirestoreTaskTemplateRepository
import ch.eureka.eurekapp.model.data.transcription.CloudFunctionSpeechToTextRepository
import ch.eureka.eurekapp.model.data.transcription.SpeechToTextRepository
import ch.eureka.eurekapp.model.data.user.FirestoreUserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage

object FirestoreRepositoriesProvider {
  private val _taskRepository: FirestoreTaskRepository by lazy {
    FirestoreTaskRepository(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance())
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
}
