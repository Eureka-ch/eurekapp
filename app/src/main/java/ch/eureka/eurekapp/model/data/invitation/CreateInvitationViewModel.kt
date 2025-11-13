package ch.eureka.eurekapp.model.data.invitation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class CreateInvitationViewModel(
    val invitationRepository: InvitationRepository = InvitationRepositoryProvider.repository
) : ViewModel() {

  fun createInvitation(
      invitation: Invitation,
      onSuccessCallback: () -> Unit,
      onFailureCallback: (Throwable) -> Unit
  ) {
    viewModelScope.launch {
        val invitationTransmit = invitationRepository.createInvitation(invitation)
        if (invitationTransmit.isSuccess) {
            onSuccessCallback()
        } else {
            onFailureCallback(invitationTransmit.exceptionOrNull()!!)
        }
    }
  }
}
