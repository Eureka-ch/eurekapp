package ch.eureka.eurekapp.model.data.invitation

import androidx.lifecycle.ViewModel

class CreateInvitationViewModel(
    val invitationRepository: InvitationRepository = InvitationRepositoryProvider.repository
) : ViewModel() {

  suspend fun createInvitation(
      invitation: Invitation,
      onSuccessCallback: () -> Unit,
      onFailureCallback: (Throwable) -> Unit
  ) {
    val invitationTransmit = invitationRepository.createInvitation(invitation)
    if (invitationTransmit.isSuccess) {
      onSuccessCallback()
    } else {
      onFailureCallback(invitationTransmit.exceptionOrNull()!!)
    }
  }
}
