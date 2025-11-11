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

open class ConnectivityObserver internal constructor(context: Context) {
  private val connectivityManager =
      context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

  open val isConnected: Flow<Boolean> =
      callbackFlow {
            val callback =
                object : ConnectivityManager.NetworkCallback() {
                  override fun onAvailable(network: Network) {
                    trySend(true)
                  }

                  override fun onLost(network: Network) {
                    trySend(false)
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
    fun getInstance(context: Context): ConnectivityObserver {
      return ConnectivityObserver(context.applicationContext)
    }
  }
}
