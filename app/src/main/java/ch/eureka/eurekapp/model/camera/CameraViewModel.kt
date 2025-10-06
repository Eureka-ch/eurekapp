package ch.eureka.eurekapp.model.camera

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CameraViewModel(private val cameraRepository: CameraRepository) : ViewModel() {
  private var _photoState: MutableStateFlow<CameraModel> = MutableStateFlow(CameraModel())
  val photoState: StateFlow<CameraModel> = _photoState.asStateFlow()

  suspend fun takePhoto() {
    val newPhoto = cameraRepository.getNewPhoto()
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

    if (cameraRepository.deletePhoto(currentState.picture)) {
      _photoState.value = currentState.copy(picture = null)
      return true
    }

    return false
  }
}
