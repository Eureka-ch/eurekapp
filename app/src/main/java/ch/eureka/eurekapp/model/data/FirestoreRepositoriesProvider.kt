package ch.eureka.eurekapp.model.data

import ch.eureka.eurekapp.model.data.file.FileStorageRepository
import ch.eureka.eurekapp.model.data.file.FirebaseFileStorageRepository
import ch.eureka.eurekapp.model.data.project.FirestoreProjectRepository
import ch.eureka.eurekapp.model.data.task.FirestoreTaskRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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

  var taskRepository = _taskRepository
  var fileRepository = _fileRepository
  var projectRepository = _projectRepository

  fun userChange() {
    taskRepository =
        FirestoreTaskRepository(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance())
    fileRepository =
        FirebaseFileStorageRepository(FirebaseStorage.getInstance(), FirebaseAuth.getInstance())
    projectRepository =
        FirestoreProjectRepository(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance())
  }
}
