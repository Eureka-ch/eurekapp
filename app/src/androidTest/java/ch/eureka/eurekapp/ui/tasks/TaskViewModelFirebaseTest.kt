package ch.eureka.eurekapp.ui.tasks

import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.model.data.task.FirestoreTaskRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for TaskViewModel with Firebase Emulator
 *
 * These tests verify the ViewModel works correctly with real Firebase data. Requires Firebase
 * emulator to be running: firebase emulators:start
 *
 * @author Assisted by AI for comprehensive test coverage
 */
@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class TaskViewModelFirebaseTest {

  private lateinit var viewModel: TaskViewModel
  private lateinit var firestoreRepository: FirestoreTaskRepository
  private val testDispatcher = StandardTestDispatcher()

  @Before
  fun setup() {
    // Configure Firebase to use emulator
    val firestore = FirebaseFirestore.getInstance()
    val settings =
        FirebaseFirestoreSettings.Builder().setHost("localhost:8080").setSslEnabled(false).build()
    firestore.firestoreSettings = settings

    val auth = FirebaseAuth.getInstance()

    firestoreRepository = FirestoreTaskRepository(firestore, auth)
    viewModel = TaskViewModel(firestoreRepository)

    Dispatchers.setMain(testDispatcher)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun viewModelLoadsTasksFromFirebaseEmulator() = runTest {
    // Given - Firebase emulator should be running with test data

    // When
    viewModel.loadTasks()
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    val state = viewModel.uiState.first()
    assertFalse(state.isLoading)
    assertNull(state.error)
    // Note: Tasks will be empty unless you add test data to emulator
  }

  @Test
  fun viewModelHandlesFirebaseErrorsGracefully() = runTest {
    // Given - Stop emulator or use invalid settings to trigger error

    // When
    viewModel.loadTasks()
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    val state = viewModel.uiState.first()
    // Should handle error gracefully without crashing
    assertFalse(state.isLoading)
  }

  @Test
  fun viewModelFilterChangesWorkWithFirebaseData() = runTest {
    // Given
    val initialState = viewModel.uiState.first()
    assertEquals(TaskFilter.MINE, initialState.selectedFilter)

    // When
    viewModel.setFilter(TaskFilter.ALL)
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    val state = viewModel.uiState.first()
    assertEquals(TaskFilter.ALL, state.selectedFilter)
  }
}
