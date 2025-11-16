package ch.eureka.eurekapp.model.connection

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

// Portions of this code were generated with the help of Grok.

@OptIn(ExperimentalCoroutinesApi::class)
class ConnectivityObserverTest {

  @Mock private lateinit var context: Context

  @Mock private lateinit var connectivityManager: ConnectivityManager

  @Mock private lateinit var network: Network

  @Mock private lateinit var networkCapabilities: NetworkCapabilities

  private lateinit var connectivityObserver: ConnectivityObserver
  private lateinit var callbackCaptor: ArgumentCaptor<ConnectivityManager.NetworkCallback>

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    callbackCaptor = ArgumentCaptor.forClass(ConnectivityManager.NetworkCallback::class.java)
    `when`(context.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(connectivityManager)
  }

  @Test
  fun initialStateEmitsTrueWhenConnected() = runBlocking {
    `when`(connectivityManager.activeNetwork).thenReturn(network)
    `when`(connectivityManager.getNetworkCapabilities(network)).thenReturn(networkCapabilities)
    `when`(networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))
        .thenReturn(true)

    connectivityObserver = ConnectivityObserver(context)
    val result = connectivityObserver.isConnected.first()
    assertEquals(true, result)
  }

  @Test
  fun initialStateEmitsFalseWhenNotConnected() = runBlocking {
    `when`(connectivityManager.activeNetwork).thenReturn(null)

    connectivityObserver = ConnectivityObserver(context)
    val result = connectivityObserver.isConnected.first()
    assertEquals(false, result)
  }

  @Test
  fun onUnavailableEmitsFalse() =
      runTest(UnconfinedTestDispatcher()) {
        `when`(connectivityManager.activeNetwork).thenReturn(network)
        `when`(connectivityManager.getNetworkCapabilities(network)).thenReturn(networkCapabilities)
        `when`(networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))
            .thenReturn(true)

        connectivityObserver = ConnectivityObserver(context)

        val emissions = mutableListOf<Boolean>()
        val job = launch { connectivityObserver.isConnected.take(2).toList(emissions) }

        verify(connectivityManager).registerDefaultNetworkCallback(callbackCaptor.capture())

        // Trigger onUnavailable callback
        callbackCaptor.value.onUnavailable()
        job.join()

        assertEquals(2, emissions.size)
        assertEquals(true, emissions[0]) // initial
        assertEquals(false, emissions[1]) // after onUnavailable
      }

  @Test
  fun onLostEmitsFalseWhenNoNetwork() =
      runTest(UnconfinedTestDispatcher()) {
        `when`(connectivityManager.activeNetwork).thenReturn(network)
        `when`(connectivityManager.getNetworkCapabilities(network)).thenReturn(networkCapabilities)
        `when`(networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))
            .thenReturn(true)

        connectivityObserver = ConnectivityObserver(context)

        val emissions = mutableListOf<Boolean>()
        val job = launch { connectivityObserver.isConnected.take(2).toList(emissions) }

        verify(connectivityManager).registerDefaultNetworkCallback(callbackCaptor.capture())

        // Simulate network lost and no active network
        `when`(connectivityManager.activeNetwork).thenReturn(null)
        callbackCaptor.value.onLost(network)
        job.join()

        assertEquals(2, emissions.size)
        assertEquals(true, emissions[0]) // initial
        assertEquals(false, emissions[1]) // after onLost (no network)
      }
}
