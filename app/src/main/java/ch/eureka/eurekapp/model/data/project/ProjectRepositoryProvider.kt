package ch.eureka.eurekapp.model.data.project


/**Provides a single [ch.eureka.eurekapp.model.project.ProjectRepository] instance for the whole codebase**/
object ProjectRepositoryProvider {
    private val _repository: ProjectRepository by lazy { FirestoreProjectRepository() }

    val repository: ProjectRepository = _repository
}