/*
The following code comes from the solution of the part 3 of the SwEnt bootcamp made by the SwEnt team:
https://github.com/swent-epfl/bootcamp-25-B3-Solution/blob/main/app/src/androidTest/java/com/github/se/bootcamp/utils/FirebaseEmulator.kt
Portions of the code in this file are copy-pasted from the Bootcamp solution provided by the SwEnt staff.
*/
package ch.eureka.eurekapp.utils

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import io.mockk.InternalPlatformDsl.toArray
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

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

  val storage
    get() = Firebase.storage

  const val HOST = "10.0.2.2"
  const val EMULATORS_PORT = 4400
  const val FIRESTORE_PORT = 8080
  const val AUTH_PORT = 9099
  const val STORAGE_PORT = 9199

  val projectID by lazy { FirebaseApp.getInstance().options.projectId }

  private val httpClient = OkHttpClient()

  private val firestoreEndpoint by lazy {
    "http://${HOST}:$FIRESTORE_PORT/emulator/v1/projects/$projectID/databases/(default)/documents"
  }

  private val authEndpoint by lazy {
    "http://${HOST}:$AUTH_PORT/emulator/v1/projects/$projectID/accounts"
  }

  private val storageEndpoint by lazy {
    "http://${HOST}:$STORAGE_PORT/v0/b/${projectID}.appspot.com/o"
  }

  private val emulatorsEndpoint = "http://$HOST:$EMULATORS_PORT/emulators"

  private fun areEmulatorsRunning(): Boolean =
      runCatching {
            Log.d("FirebaseEmulator", "Checking if emulators are running at $emulatorsEndpoint")
            val client = httpClient
            val request = Request.Builder().url(emulatorsEndpoint).build()
            val isSuccessful = client.newCall(request).execute().isSuccessful
            Log.d("FirebaseEmulator", "Emulators running check result: $isSuccessful")
            isSuccessful
          }
          .getOrNull() == true

  val isRunning = areEmulatorsRunning()

  init {
    Log.d("FirebaseEmulator", "Initializing FirebaseEmulator")
    if (isRunning) {
      Log.d("FirebaseEmulator", "Emulators detected, configuring Firebase to use emulators")
      auth.useEmulator(HOST, AUTH_PORT)
      Log.d("FirebaseEmulator", "Auth emulator configured at $HOST:$AUTH_PORT")
      firestore.useEmulator(HOST, FIRESTORE_PORT)
      Log.d("FirebaseEmulator", "Firestore emulator configured at $HOST:$FIRESTORE_PORT")
      storage.useEmulator(HOST, STORAGE_PORT)
      Log.d("FirebaseEmulator", "Storage emulator configured at $HOST:$STORAGE_PORT")
      assert(Firebase.firestore.firestoreSettings.host.contains(HOST)) {
        "Failed to connect to Firebase Firestore Emulator."
      }
      Log.d("FirebaseEmulator", "All emulators successfully initialized")
    } else {
      Log.w("FirebaseEmulator", "Emulators are not running, using production Firebase")
    }
  }

  private fun clearEmulator(endpoint: String) {
    runCatching {
      Log.d("FirebaseEmulator", "Clearing emulator at $endpoint")
      val client = httpClient
      val request = Request.Builder().url(endpoint).delete().build()
      val response = client.newCall(request).execute()

      if (response.isSuccessful) {
        Log.d("FirebaseEmulator", "Successfully cleared emulator at $endpoint")
      } else {
        Log.e(
            "FirebaseEmulator",
            "Failed to clear emulator at $endpoint: ${response.code} ${response.message}")
      }

      assert(response.isSuccessful) { "Failed to clear emulator at $endpoint" }
    }
  }

  fun clearAuthEmulator() {
    Log.d("FirebaseEmulator", "Clearing Auth emulator")
    clearEmulator(authEndpoint)
  }

  fun clearFirestoreEmulator() {
    Log.d("FirebaseEmulator", "Clearing Firestore emulator")
    clearEmulator(firestoreEndpoint)
  }

  fun clearStorageEmulator() {
    Log.d("FirebaseEmulator", "Clearing Storage emulator")
    clearEmulator(storageEndpoint)
  }

  /**
   * Seeds a Google user in the Firebase Auth Emulator using a fake JWT id_token.
   *
   * @param fakeIdToken A JWT-shaped string, must contain at least "sub".
   * @param email The email address to associate with the account.
   */
  fun createGoogleUser(fakeIdToken: String) {
    Log.d("FirebaseEmulator", "Creating Google user with fake ID token")
    val url =
        "http://$HOST:$AUTH_PORT/identitytoolkit.googleapis.com/v1/accounts:signInWithIdp?key=fake-api-key"

    // postBody must be x-www-form-urlencoded style string, wrapped in JSON
    val postBody = "id_token=$fakeIdToken&providerId=google.com"

    val requestJson =
        JSONObject().apply {
          put("postBody", postBody)
          put("requestUri", "http://localhost")
          put("returnIdpCredential", true)
          put("returnSecureToken", true)
        }

    val mediaType = "application/json; charset=utf-8".toMediaType()
    val body = requestJson.toString().toRequestBody(mediaType)

    val request =
        Request.Builder().url(url).post(body).addHeader("Content-Type", "application/json").build()

    val response = httpClient.newCall(request).execute()
    if (response.isSuccessful) {
      Log.d("FirebaseEmulator", "Successfully created Google user in Auth Emulator")
    } else {
      Log.e(
          "FirebaseEmulator", "Failed to create Google user: ${response.code} ${response.message}")
    }
    assert(response.isSuccessful) {
      "Failed to create user in Auth Emulator: ${response.code} ${response.message}"
    }
  }

  fun changeEmail(fakeIdToken: String, newEmail: String) {
    Log.d("FirebaseEmulator", "Changing user email to $newEmail")
    val response =
        httpClient
            .newCall(
                Request.Builder()
                    .url(
                        "http://$HOST:$AUTH_PORT/identitytoolkit.googleapis.com/v1/accounts:update?key=fake-api-key")
                    .post(
                        """
            {
                "idToken": "$fakeIdToken",
                "email": "$newEmail",
                "returnSecureToken": true
            }
        """
                            .trimIndent()
                            .toRequestBody())
                    .build())
            .execute()
    if (response.isSuccessful) {
      Log.d("FirebaseEmulator", "Successfully changed user email to $newEmail")
    } else {
      Log.e("FirebaseEmulator", "Failed to change email: ${response.code} ${response.message}")
    }
    assert(response.isSuccessful) {
      "Failed to change email in Auth Emulator: ${response.code} ${response.message}"
    }
  }

  val users: String
    get() {
      val request =
          Request.Builder()
              .url(
                  "http://$HOST:$AUTH_PORT/identitytoolkit.googleapis.com/v1/accounts:query?key=fake-api-key")
              .build()

      Log.d("FirebaseEmulator", "Fetching users with request: ${request.url.toString()}")
      val response = httpClient.newCall(request).execute()
      Log.d("FirebaseEmulator", "Response received: ${response.toArray()}")
      return response.body.toString()
    }
}
