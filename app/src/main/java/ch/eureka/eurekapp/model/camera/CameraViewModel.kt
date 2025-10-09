package ch.eureka.eurekapp.model.camera

import android.app.Application
import androidx.camera.core.Preview
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * The view model for the camera operations
 *
 * @param application The application being run (necessary for having a context)
 */
class CameraViewModel(application: Application) : AndroidViewModel(application) {
  private var _photoState: MutableStateFlow<CameraModel> = MutableStateFlow(CameraModel())
  val photoState: StateFlow<CameraModel> = _photoState.asStateFlow()
  private var cameraRepository: LocalCameraRepository? = null
  private val _preview: MutableStateFlow<Preview?> = MutableStateFlow(null)
  val preview: StateFlow<Preview?> = _preview.asStateFlow()

  /** Takes a photo using the cameraRepository (if initialized) */
  fun takePhoto() {
    viewModelScope.launch {
      val newPhoto = cameraRepository?.getNewPhoto()
      newPhoto?.let { uri ->
        val currentState = _photoState.value
        _photoState.value = currentState.copy(picture = uri)
      }
    }
  }

  /**
   * Deletes the photo from the current state (if there is any)
   *
   * @return true if the delete was successful, false otherwise
   */
  fun deletePhoto(): Boolean {
    val currentState = _photoState.value
    if (currentState.picture == null) {
      return false
    }
    return runBlocking {
      if (cameraRepository?.deletePhoto(currentState.picture) == true) {
        _photoState.value = currentState.copy(picture = null)
        true
      } else {
        false
      }
    }
  }

  /**
   * Returns the preview of the currently used camera
   *
   * @return The preview
   * @throws IllegalArgumentException if the cameraRepository is not initialized
   */
  fun getPreview(): Preview {
    return cameraRepository?.preview
        ?: throw IllegalStateException("CameraRepository not initialized.")
  }

  /**
   * Initialized the cameraRepository and therefore starts the camera
   *
   * @param lifecycleOwner The owner of the camera
   */
  fun startCamera(lifecycleOwner: LifecycleOwner) {
    val context = getApplication<Application>().applicationContext
    cameraRepository = LocalCameraRepository(context, lifecycleOwner)
    _preview.value = cameraRepository?.preview
  }

  /** Unbinds (cleans up after) a camera session */
  fun unbindCamera() {
    cameraRepository?.dispose()
  }
}
