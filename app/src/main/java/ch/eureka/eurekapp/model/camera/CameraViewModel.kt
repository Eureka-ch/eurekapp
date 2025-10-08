package ch.eureka.eurekapp.model.camera

import android.app.Application
import androidx.camera.core.Preview
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CameraViewModel(application: Application) : AndroidViewModel(application) {
  private var _photoState: MutableStateFlow<CameraModel> = MutableStateFlow(CameraModel())
  val photoState: StateFlow<CameraModel> = _photoState.asStateFlow()
  private var cameraRepository: LocalCameraRepository? = null
  private val _preview: MutableStateFlow<Preview?> = MutableStateFlow(null)
  val preview: StateFlow<Preview?> = _preview.asStateFlow()
  suspend fun takePhoto() {
    val newPhoto = cameraRepository?.getNewPhoto()
    newPhoto?.let { uri ->
      val currentState = _photoState.value
      _photoState.value = currentState.copy(picture = uri)
    }
  }

  suspend fun deletePhoto(): Boolean {
    val currentState = _photoState.value
    if (currentState.picture == null) {
      return false
    }

    if (cameraRepository?.deletePhoto(currentState.picture) == true) {
      _photoState.value = currentState.copy(picture = null)
      return true
    }

    return false
  }

  fun getPreview(): Preview {
    return cameraRepository?.preview
        ?: throw IllegalStateException("CameraRepository not initialized.")
  }

  fun startCamera(lifecycleOwner: LifecycleOwner) {
    val context = getApplication<Application>().applicationContext
    cameraRepository = LocalCameraRepository(context, lifecycleOwner)
    _preview.value = cameraRepository?.preview
  }

  fun unbindCamera(){
    cameraRepository?.dispose()
  }
}
