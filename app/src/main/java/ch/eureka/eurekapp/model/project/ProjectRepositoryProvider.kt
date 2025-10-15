package ch.eureka.eurekapp.model.project

/**Provides a single [ProjectRepository] instance for the whole codebase**/
object ProjectRepositoryProvider {
    private val _repository: ProjectRepository by lazy { FirestoreProjectRepository() }

    val repository: ProjectRepository = _repository
}