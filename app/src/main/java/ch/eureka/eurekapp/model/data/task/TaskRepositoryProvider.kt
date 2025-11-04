package ch.eureka.eurekapp.model.data.task

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object TaskRepositoryProvider {
    val _repository = FirestoreTaskRepository(
        firestore = FirebaseFirestore.getInstance(),
        auth = FirebaseAuth.getInstance()
    )
}