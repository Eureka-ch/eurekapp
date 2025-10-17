package ch.eureka.eurekapp.model.data

import ch.eureka.eurekapp.model.data.file.FileStorageRepository
import ch.eureka.eurekapp.model.data.file.FirebaseFileStorageRepository
import ch.eureka.eurekapp.model.data.project.FirestoreProjectRepository
import ch.eureka.eurekapp.model.data.task.FirestoreTaskRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage

object FirestoreRepositoriesProvider {
  private val _taskRepository: FirestoreTaskRepository by lazy {
    FirestoreTaskRepository(Firebase.firestore, Firebase.auth)
  }

  private val _projectRepository: FirestoreProjectRepository by lazy {
    FirestoreProjectRepository(Firebase.firestore, Firebase.auth)
  }
  private val _fileRepository: FileStorageRepository by lazy {
    FirebaseFileStorageRepository(Firebase.storage, Firebase.auth)
  }

  var taskRepository = _taskRepository
  var fileRepository = _fileRepository
  var projectRepository = _projectRepository

  fun userChange() {
    taskRepository = FirestoreTaskRepository(Firebase.firestore, Firebase.auth)
    fileRepository = FirebaseFileStorageRepository(Firebase.storage, Firebase.auth)
    projectRepository = FirestoreProjectRepository(Firebase.firestore, Firebase.auth)
  }
}
