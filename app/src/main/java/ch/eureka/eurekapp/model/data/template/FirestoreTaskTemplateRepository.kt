package ch.eureka.eurekapp.model.data.template

import ch.eureka.eurekapp.model.data.FirestorePaths
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreTaskTemplateRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : TaskTemplateRepository {

  override fun getTemplateById(projectId: String, templateId: String): Flow<TaskTemplate?> =
      callbackFlow {
        val listener =
            firestore
                .collection(FirestorePaths.PROJECTS)
                .document(projectId)
                .collection(FirestorePaths.TASK_TEMPLATES)
                .document(templateId)
                .addSnapshotListener { snapshot, error ->
                  if (error != null) {
                    close(error)
                    return@addSnapshotListener
                  }
                  trySend(snapshot?.toObject(TaskTemplate::class.java))
                }
        awaitClose { listener.remove() }
      }

  override fun getTemplatesInProject(projectId: String): Flow<List<TaskTemplate>> = callbackFlow {
    val listener =
        firestore
            .collection(FirestorePaths.PROJECTS)
            .document(projectId)
            .collection(FirestorePaths.TASK_TEMPLATES)
            .addSnapshotListener { snapshot, error ->
              if (error != null) {
                close(error)
                return@addSnapshotListener
              }
              val templates =
                  snapshot?.documents?.mapNotNull { it.toObject(TaskTemplate::class.java) }
                      ?: emptyList()
              trySend(templates)
            }
    awaitClose { listener.remove() }
  }

  override suspend fun createTemplate(template: TaskTemplate): Result<String> = runCatching {
    firestore
        .collection(FirestorePaths.PROJECTS)
        .document(template.projectId)
        .collection(FirestorePaths.TASK_TEMPLATES)
        .document(template.templateID)
        .set(template)
        .await()
    template.templateID
  }

  override suspend fun updateTemplate(template: TaskTemplate): Result<Unit> = runCatching {
    firestore
        .collection(FirestorePaths.PROJECTS)
        .document(template.projectId)
        .collection(FirestorePaths.TASK_TEMPLATES)
        .document(template.templateID)
        .set(template)
        .await()
  }

  override suspend fun deleteTemplate(projectId: String, templateId: String): Result<Unit> =
      runCatching {
        firestore
            .collection(FirestorePaths.PROJECTS)
            .document(projectId)
            .collection(FirestorePaths.TASK_TEMPLATES)
            .document(templateId)
            .delete()
            .await()
      }
}
