package ch.eureka.eurekapp.model.data.project

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Provides a single [ch.eureka.eurekapp.model.project.ProjectRepository] instance for the whole
 * codebase*
 */
object ProjectRepositoryProvider {
  private val _repository: ProjectRepository by lazy {
    FirestoreProjectRepository(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance())
  }

  val repository: ProjectRepository = _repository
}
