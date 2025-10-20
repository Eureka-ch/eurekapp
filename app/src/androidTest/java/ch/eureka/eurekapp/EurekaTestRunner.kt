package ch.eureka.eurekapp

import android.os.Bundle
import androidx.test.runner.AndroidJUnitRunner
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

/**
 * Custom test runner that configures Firebase emulators for all instrumented tests.
 *
 * This ensures that all connected tests use Firebase emulators instead of production Firebase,
 * providing isolation and preventing accidental data modification.
 */
class EurekaTestRunner : AndroidJUnitRunner() {
  override fun onCreate(arguments: Bundle?) {
    super.onCreate(arguments)

    // Configure Firebase to use local emulators
    FirebaseFirestore.getInstance().useEmulator("10.0.2.2", 8080)
    FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9099)
    FirebaseStorage.getInstance().useEmulator("10.0.2.2", 9199)
  }
}
