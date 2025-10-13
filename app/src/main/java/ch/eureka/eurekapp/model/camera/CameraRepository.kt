package ch.eureka.eurekapp.model.camera

import android.net.Uri
import androidx.camera.core.Preview

// Portions of this code were generated with the help of Grok.

/** Interface defining the contract for camera operations, primarily for capturing new photos. */
interface CameraRepository {
  /**
   * Captures a new photo using the device's camera and saves it to the MediaStore.
   *
   * @return The Uri of the saved photo, or `null` if capture fails
   */
  suspend fun getNewPhoto(): Uri?

  /**
   * Deletes the photo at the specified Uri
   *
   * @return true if the delete was successful, false otherwise
   */
  suspend fun deletePhoto(photoUri: Uri): Boolean

  /** Cleans up camera resources. */
  fun dispose()

  /** The preview of the camera for displaying the live camera feed */
  var preview: Preview
}
