//Portions of this code were generated with the help of Gemini Pro 3
package ch.eureka.eurekapp.model.notifications

import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.model.data.user.UserRepository
import ch.eureka.eurekapp.model.data.user.UserNotificationSettingsKeys
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationSettingsViewModelTest {

    // UnconfinedTestDispatcher runs coroutines eagerly, avoiding timing issues in Unit tests
    private val testDispatcher = UnconfinedTestDispatcher()

    @MockK
    lateinit var userRepository: UserRepository

    // We use a real instance instead of @MockK because mocking data class 'copy' methods is unstable
    private lateinit var realUser: User

    private lateinit var viewModel: NotificationSettingsViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)

        // Initialize with default/empty values.
        // NOTE: Ensure your User class constructor matches this signature.
        realUser = User(notificationSettings = emptyMap())

        viewModel = NotificationSettingsViewModel(userRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun saveUserSettingFailureUserNull() = runTest(testDispatcher) {
        coEvery { userRepository.getCurrentUser() } returns flowOf(null)

        var errorMsg = ""
        viewModel.saveUserSetting(
            UserNotificationSettingsKeys.ON_MEETING_SCHEDULED_NOTIFY,
            true,
            onFailure = { errorMsg = it },
            onSuccess = { }
        )
        // No need for advanceUntilIdle() with UnconfinedTestDispatcher

        assertEquals("No user was associated with this app", errorMsg)
    }

    @Test
    fun saveUserSettingSuccess() = runTest(testDispatcher) {
        coEvery { userRepository.getCurrentUser() } returns flowOf(realUser)
        coEvery { userRepository.saveUser(any()) } returns Result.success(Unit)

        var success = false
        viewModel.saveUserSetting(
            UserNotificationSettingsKeys.ON_MEETING_SCHEDULED_NOTIFY,
            true,
            onFailure = { },
            onSuccess = { success = true }
        )

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(true, success)
        coVerify { userRepository.saveUser(any()) }
    }

    @Test
    fun saveUserSettingFailureRepository() = runTest(testDispatcher) {
        coEvery { userRepository.getCurrentUser() } returns flowOf(realUser)
        coEvery { userRepository.saveUser(any()) } returns Result.failure(Exception("DB Error"))

        var errorMsg = ""
        viewModel.saveUserSetting(
            UserNotificationSettingsKeys.ON_MEETING_SCHEDULED_NOTIFY,
            true,
            onFailure = { errorMsg = it },
            onSuccess = { }
        )

        assertEquals("DB Error", errorMsg)
    }

    @Test
    fun getUserSettingFound() = runTest(testDispatcher) {
        val key = UserNotificationSettingsKeys.ON_MEETING_SCHEDULED_NOTIFY
        // Create a specific user state for this test
        val specificUser = User(notificationSettings = mapOf(key.name to false))

        coEvery { userRepository.getCurrentUser() } returns flowOf(specificUser)

        val result = viewModel.getUserSetting(key).first()
        assertEquals(false, result)
    }

    @Test
    fun getUserSettingDefault() = runTest(testDispatcher) {
        val key = UserNotificationSettingsKeys.ON_MEETING_SCHEDULED_NOTIFY
        // Use the default user (empty map)
        coEvery { userRepository.getCurrentUser() } returns flowOf(realUser)

        // Default for this key is true
        val result = viewModel.getUserSetting(key).first()
        assertEquals(true, result)
    }
}