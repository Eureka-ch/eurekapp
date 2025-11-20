// Portions of this code were generated with the help of Grok.
package ch.eureka.eurekapp.utils

import android.content.Context
import ch.eureka.eurekapp.model.connection.ConnectivityObserver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Mock implementation of ConnectivityObserver for testing purposes. Allows manual control of
 * connectivity state.
 *
 * @param context The Android context, passed to the parent class.
 */
class MockConnectivityObserver(context: Context) : ConnectivityObserver(context) {
  private val _isConnected = MutableStateFlow(true)
  override val isConnected: Flow<Boolean> = _isConnected

  /**
   * Manually set the connectivity state for testing.
   *
   * @param connected true if device should be considered online, false otherwise
   */
  fun setConnected(connected: Boolean) {
    _isConnected.value = connected
  }
}
