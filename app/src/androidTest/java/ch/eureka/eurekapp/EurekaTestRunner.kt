package ch.eureka.eurekapp

import android.os.Bundle
import androidx.test.runner.AndroidJUnitRunner
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage

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
    Firebase.firestore.useEmulator("10.0.2.2", 8080)
    Firebase.auth.useEmulator("10.0.2.2", 9099)
    Firebase.storage.useEmulator("10.0.2.2", 9199)
  }
}
