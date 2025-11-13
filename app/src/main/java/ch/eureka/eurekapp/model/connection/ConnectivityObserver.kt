package ch.eureka.eurekapp.model.connection

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

// Portions of this code were generated with the help of Grok.

/**
 * A class that observes network connectivity changes and provides a Flow of connection status.
 *
 * @param context The application context for accessing system services.
 */
open class ConnectivityObserver internal constructor(context: Context) {
  private val connectivityManager =
      context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

  /**
   * A Flow that emits true when the device is connected to the internet, false otherwise. It emits
   * the current state immediately and updates on changes.
   */
  open val isConnected: Flow<Boolean> =
      callbackFlow {
            val callback =
                object : ConnectivityManager.NetworkCallback() {
                  override fun onAvailable(network: Network) {
                    trySend(true)
                  }

                  override fun onUnavailable() {
                    trySend(false)
                  }

                  override fun onLost(network: Network) {
                    // Network is completely lost, check current state
                    val currentNetwork = connectivityManager.activeNetwork
                    val capabilities = connectivityManager.getNetworkCapabilities(currentNetwork)
                    val isCurrentlyConnected =
                        capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ==
                            true
                    trySend(isCurrentlyConnected)
                  }

                  override fun onCapabilitiesChanged(
                      network: Network,
                      networkCapabilities: NetworkCapabilities
                  ) {
                    // Capabilities changed, re-evaluate internet access
                    val hasInternet =
                        networkCapabilities.hasCapability(
                            NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    trySend(hasInternet)
                  }
                }

            connectivityManager.registerDefaultNetworkCallback(callback)

            // Send initial state
            val currentNetwork = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(currentNetwork)
            val isCurrentlyConnected =
                capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
            trySend(isCurrentlyConnected)

            awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
          }
          .distinctUntilChanged()

  companion object {
    /**
     * Returns an instance of ConnectivityObserver.
     *
     * @param context The application context.
     * @return A new ConnectivityObserver instance.
     */
    fun getInstance(context: Context): ConnectivityObserver {
      return ConnectivityObserver(context.applicationContext)
    }
  }
}
