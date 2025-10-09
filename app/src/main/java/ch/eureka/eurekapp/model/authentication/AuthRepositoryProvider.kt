package ch.eureka.eurekapp.model.authentication

/** Provides a single [AuthRepository] instance for the whole codebase */
object AuthRepositoryProvider {
  private val _repository: AuthRepository by lazy { AuthRepositoryFirebase() }

  val repository: AuthRepository = _repository
}
