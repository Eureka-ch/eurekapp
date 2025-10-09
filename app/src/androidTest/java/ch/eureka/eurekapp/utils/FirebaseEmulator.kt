/*
 * Portions of the code in this file are copy-pasted from the Bootcamp solution provided by the SwEnt staff.
 */
package ch.eureka.eurekapp.utils

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * An object to manage the connection to Firebase Emulators for Android tests.
 *
 * This object will automatically use the emulators if they are running when the tests start.
 */
object FirebaseEmulator {
  val auth
    get() = Firebase.auth

  val firestore
    get() = Firebase.firestore

  const val HOST = "10.0.2.2"
  const val EMULATORS_PORT = 4400
  const val FIRESTORE_PORT = 8080
  const val AUTH_PORT = 9099

  val projectID by lazy { FirebaseApp.getInstance().options.projectId }

  private val httpClient = OkHttpClient()

  private val firestoreEndpoint by lazy {
    "http://${HOST}:$FIRESTORE_PORT/emulator/v1/projects/$projectID/databases/(default)/documents"
  }

  private val authEndpoint by lazy {
    "http://${HOST}:$AUTH_PORT/emulator/v1/projects/$projectID/accounts"
  }

  private val emulatorsEndpoint = "http://$HOST:$EMULATORS_PORT/emulators"

  private fun areEmulatorsRunning(): Boolean =
      runCatching {
            val client = httpClient
            val request = Request.Builder().url(emulatorsEndpoint).build()
            client.newCall(request).execute().isSuccessful
          }
          .getOrNull() == true

  val isRunning = areEmulatorsRunning()

  init {
    if (isRunning) {
      auth.useEmulator(HOST, AUTH_PORT)
      firestore.useEmulator(HOST, FIRESTORE_PORT)
      assert(Firebase.firestore.firestoreSettings.host.contains(HOST)) {
        "Failed to connect to Firebase Firestore Emulator."
      }
    }
  }

  private fun clearEmulator(endpoint: String) {
    Log.d("FirebaseEmulator", "Attempting to clear emulator at: $endpoint")
    val client = httpClient
    val request = Request.Builder().url(endpoint).delete().build()
    val response = client.newCall(request).execute()

    Log.d("FirebaseEmulator", "Clear response: code=${response.code}, message=${response.message}")
    assert(response.isSuccessful) {
      "Failed to clear emulator at $endpoint: ${response.code} ${response.message}"
    }
    Log.d("FirebaseEmulator", "Successfully cleared emulator at $endpoint")
  }

  fun clearAuthEmulator() {
    Log.d("FirebaseEmulator", "Clearing Auth emulator (projectID=$projectID)")
    clearEmulator(authEndpoint)
  }

  fun clearFirestoreEmulator() {
    Log.d("FirebaseEmulator", "Clearing Firestore emulator (projectID=$projectID)")
    clearEmulator(firestoreEndpoint)
  }
}
