package ch.eureka.eurekapp.model.data.invitation

import androidx.lifecycle.ViewModel

class CreateInvitationViewModel(
    val invitationRepository: InvitationRepository = InvitationRepositoryProvider.repository
): ViewModel() {

    suspend fun createInvitation(invitation: Invitation, onSuccessCallback: () -> Unit,
                                 onFailureCallback: () -> Unit){
        if(invitationRepository.createInvitation(invitation).isSuccess){
            onSuccessCallback()
        }else{
            onFailureCallback()
        }
    }
}