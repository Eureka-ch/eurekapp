/* Portions of this file were written with the help of Claude. */
package ch.eureka.eurekapp.screen.createinvitationsubscreen

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import ch.eureka.eurekapp.model.data.invitation.Invitation
import ch.eureka.eurekapp.model.data.invitation.InvitationRepository
import ch.eureka.eurekapp.screens.subscreens.projects.invitation.CreateInvitationSubScreen
import ch.eureka.eurekapp.screens.subscreens.projects.invitation.CreateInvitationSubscreen
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test

class CreateProjectScreenTest : TestCase() {
  @get:Rule val composeRule = createComposeRule()

  class MockInvitationRepository : InvitationRepository {
    override fun getInvitationByToken(token: String): Flow<Invitation?> {
      TODO("Not yet implemented")
    }

    override fun getProjectInvitations(projectId: String): Flow<List<Invitation>> {
      TODO("Not yet implemented")
    }

    override suspend fun createInvitation(invitation: Invitation): Result<Unit> {
      return Result.success(Unit)
    }

    override suspend fun markInvitationAsUsed(token: String, userId: String): Result<Unit> {
      TODO("Not yet implemented")
    }
  }

  @Test
  fun createInvitationSubscreen_invitationUI() {
    runBlocking {
      composeRule.setContent { CreateInvitationSubscreen("kddjfdshf") {} }

      composeRule.onNodeWithTag(CreateInvitationSubScreen.CREATE_INVITATION_BUTTON).performClick()

      Thread.sleep(5000)
      composeRule.waitForIdle()

      composeRule.onNodeWithTag(CreateInvitationSubScreen.COPY_INVITATION_BUTTON).performClick()
    }
  }
}
