/* Portions of this file were written with the help of GPT-5 Codex and Gemini. */
package ch.eureka.eurekapp.model.data.ideas

import ch.eureka.eurekapp.model.data.FirestorePaths
import ch.eureka.eurekapp.model.data.chat.Message
import ch.eureka.eurekapp.model.data.project.ProjectRepository
import ch.eureka.eurekapp.ui.ideas.Idea
import ch.eureka.eurekapp.ui.ideas.IdeasRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

class FirestoreIdeasRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val projectRepository: ProjectRepository
) : IdeasRepository {

  override fun getIdeasForProject(projectId: String): Flow<List<Idea>> = callbackFlow {
    val currentUserId = auth.currentUser?.uid
    if (currentUserId == null) {
      trySend(emptyList())
      close()
      return@callbackFlow
    }

    val listener =
        firestore
            .collection(FirestorePaths.PROJECTS)
            .document(projectId)
            .collection(FirestorePaths.IDEAS)
            .whereArrayContains("participantIds", currentUserId)
            .addSnapshotListener { snapshot, error ->
              if (error != null) {
                close(error)
                return@addSnapshotListener
              }
              val ideas =
                  snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Idea::class.java)?.copy(ideaId = doc.id)
                  } ?: emptyList()
              trySend(ideas)
            }
    awaitClose { listener.remove() }
  }

  override suspend fun createIdea(idea: Idea): Result<String> = runCatching {
    firestore
        .collection(FirestorePaths.PROJECTS)
        .document(idea.projectId)
        .collection(FirestorePaths.IDEAS)
        .document(idea.ideaId)
        .set(idea)
        .await()

    idea.ideaId
  }

  override suspend fun deleteIdea(projectId: String, ideaId: String): Result<Unit> = runCatching {
    // Delete the idea document
    firestore
        .collection(FirestorePaths.PROJECTS)
        .document(projectId)
        .collection(FirestorePaths.IDEAS)
        .document(ideaId)
        .delete()
        .await()

    // Delete all messages in the idea (optional cleanup)
    val messagesSnapshot =
        firestore
            .collection(FirestorePaths.PROJECTS)
            .document(projectId)
            .collection(FirestorePaths.IDEAS)
            .document(ideaId)
            .collection(FirestorePaths.IDEA_MESSAGES)
            .get()
            .await()

    messagesSnapshot.documents.forEach { it.reference.delete() }
  }

  override fun getMessagesForIdea(ideaId: String): Flow<List<Message>> {
    // We need to find which project this idea belongs to
    // For now, we'll listen on all projects the user has access to
    val currentUserId = auth.currentUser?.uid
    if (currentUserId == null) {
      return flowOf(emptyList())
    }

    return projectRepository.getProjectsForCurrentUser(skipCache = false).flatMapLatest { projects
      ->
      if (projects.isEmpty()) {
        flowOf(emptyList())
      } else {
        // Try to find the idea in any project
        val messageFlows =
            projects.map { project -> getMessagesForIdeaInProject(project.projectId, ideaId) }
        combine(messageFlows) { messageArrays ->
          messageArrays.flatMap { it.toList() }.sortedByDescending { it.createdAt }
        }
      }
    }
  }

  private fun getMessagesForIdeaInProject(projectId: String, ideaId: String): Flow<List<Message>> =
      callbackFlow {
        val listener =
            firestore
                .collection(FirestorePaths.PROJECTS)
                .document(projectId)
                .collection(FirestorePaths.IDEAS)
                .document(ideaId)
                .collection(FirestorePaths.IDEA_MESSAGES)
                .orderBy("createdAt")
                .addSnapshotListener { snapshot, error ->
                  if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                  }
                  val messages =
                      snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(Message::class.java)?.copy(messageID = doc.id)
                      } ?: emptyList()
                  trySend(messages)
                }
        awaitClose { listener.remove() }
      }

  override suspend fun sendMessage(ideaId: String, message: Message): Result<Unit> = runCatching {
    val currentUserId = auth.currentUser?.uid ?: throw Exception("User not authenticated")

    // Find the project ID for this idea
    val projects = projectRepository.getProjectsForCurrentUser(skipCache = false)
    // We need to iterate through projects to find where this idea exists
    // For simplicity, we'll search in all projects
    val allProjects = mutableListOf<String>()
    projects.collect { projectList -> allProjects.addAll(projectList.map { it.projectId }) }

    var foundProject: String? = null
    for (projectId in allProjects) {
      val ideaDoc =
          firestore
              .collection(FirestorePaths.PROJECTS)
              .document(projectId)
              .collection(FirestorePaths.IDEAS)
              .document(ideaId)
              .get()
              .await()

      if (ideaDoc.exists()) {
        foundProject = projectId
        break
      }
    }

    if (foundProject == null) {
      throw Exception("Idea not found in any project")
    }

    firestore
        .collection(FirestorePaths.PROJECTS)
        .document(foundProject)
        .collection(FirestorePaths.IDEAS)
        .document(ideaId)
        .collection(FirestorePaths.IDEA_MESSAGES)
        .document(message.messageID)
        .set(message)
        .await()
  }

  override suspend fun addParticipant(
      projectId: String,
      ideaId: String,
      userId: String
  ): Result<Unit> = runCatching {
    firestore
        .collection(FirestorePaths.PROJECTS)
        .document(projectId)
        .collection(FirestorePaths.IDEAS)
        .document(ideaId)
        .update("participantIds", FieldValue.arrayUnion(userId))
        .await()
  }
}
