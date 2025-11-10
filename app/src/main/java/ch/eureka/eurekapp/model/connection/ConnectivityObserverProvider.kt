package ch.eureka.eurekapp.model.connection

import android.content.Context

object ConnectivityObserverProvider {
    private var _connectivityObserver: ConnectivityObserver? = null

    fun initialize(context: Context) {
        if (_connectivityObserver == null) {
            _connectivityObserver = ConnectivityObserver.getInstance(context.applicationContext)
        }
    }

    val connectivityObserver: ConnectivityObserver
        get() = _connectivityObserver
            ?: throw IllegalStateException("ConnectivityObserverProvider must be initialized first")
}
