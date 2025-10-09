package ch.eureka.eurekapp.model.template

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

  override fun getTemplateById(workspaceId: String, templateId: String): Flow<TaskTemplate?> =
      callbackFlow {
        val listener =
            firestore
                .collection("workspaces")
                .document(workspaceId)
                .collection("taskTemplates")
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

  override fun getTemplatesInWorkspace(workspaceId: String): Flow<List<TaskTemplate>> =
      callbackFlow {
        val listener =
            firestore
                .collection("workspaces")
                .document(workspaceId)
                .collection("taskTemplates")
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

  override fun getTemplatesForContext(
      workspaceId: String,
      contextId: String,
      contextType: TemplateContextType
  ): Flow<List<TaskTemplate>> = callbackFlow {
    val listener =
        firestore
            .collection("workspaces")
            .document(workspaceId)
            .collection("taskTemplates")
            .whereEqualTo("contextId", contextId)
            .whereEqualTo("contextType", contextType.name)
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
        .collection("workspaces")
        .document(template.workspaceId)
        .collection("taskTemplates")
        .document(template.templateID)
        .set(template)
        .await()
    template.templateID
  }

  override suspend fun updateTemplate(template: TaskTemplate): Result<Unit> = runCatching {
    firestore
        .collection("workspaces")
        .document(template.workspaceId)
        .collection("taskTemplates")
        .document(template.templateID)
        .set(template)
        .await()
  }

  override suspend fun deleteTemplate(workspaceId: String, templateId: String): Result<Unit> =
      runCatching {
        firestore
            .collection("workspaces")
            .document(workspaceId)
            .collection("taskTemplates")
            .document(templateId)
            .delete()
            .await()
      }
}
