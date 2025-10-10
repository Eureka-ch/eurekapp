package ch.eureka.eurekapp.model.camera

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

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

/**
 * A local implementation of CameraRepository using CameraX for in-app photo capture.
 *
 * @param context The application Context for accessing content resolver and executors.
 * @param lifecycleOwner The LifecycleOwner (e.g., Activity) to bind the camera lifecycle to.
 * @param locale The Locale for formatting photo filenames (default: French Swiss).
 * @param photoLocation The relative path for saving photos in MediaStore.
 */
class LocalCameraRepository(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val locale: Locale = Locale("fr", "CH"),
    private val photoLocation: String = "Pictures/EurekApp",
    private val cameraSelection: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
) : CameraRepository {
  private var imageCapture: ImageCapture? = null
  private lateinit var cameraExecutor: Executor
  override lateinit var preview: Preview
  private val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> =
      ProcessCameraProvider.getInstance(context)
  private val cameraProvider: ProcessCameraProvider
    get() = cameraProviderFuture.get()

  companion object {
    private const val NAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
  }

  init {
    initialization()
  }

  private fun initialization() {
    preview = Preview.Builder().build()
    imageCapture = ImageCapture.Builder().build()
    cameraExecutor = ContextCompat.getMainExecutor(context)

    cameraProvider.unbindAll()
    cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelection, preview, imageCapture)
  }

  override suspend fun getNewPhoto(): Uri? {
    if (imageCapture == null) {
      return null
    }

    val name = SimpleDateFormat(NAME_FORMAT, locale).format(System.currentTimeMillis())
    val contentValues =
        ContentValues().apply {
          put(MediaStore.MediaColumns.DISPLAY_NAME, name)
          put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
          if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            put(MediaStore.Images.Media.RELATIVE_PATH, photoLocation)
          }
        }

    val outputOptions =
        ImageCapture.OutputFileOptions.Builder(
                context.contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
            .build()

    return suspendCancellableCoroutine { continuation ->
      imageCapture?.takePicture(
          outputOptions,
          cameraExecutor,
          object : ImageCapture.OnImageSavedCallback {
            override fun onError(exc: ImageCaptureException) {
              continuation.resumeWithException(exc)
            }

            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
              continuation.resume(output.savedUri)
            }
          })
    }
  }

  override suspend fun deletePhoto(photoUri: Uri): Boolean {
    val rowsDeleted = context.contentResolver.delete(photoUri, null, null)
    return rowsDeleted > 0
  }

  override fun dispose() {
    cameraProvider.unbindAll()
  }
}
