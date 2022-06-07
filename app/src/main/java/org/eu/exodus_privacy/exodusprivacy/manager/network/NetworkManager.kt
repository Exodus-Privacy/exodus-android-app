package org.eu.exodus_privacy.exodusprivacy.manager.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private var observers = mutableListOf<(connected: Boolean) -> Unit>()

    init {
        ContextCompat.getSystemService(
            context,
            ConnectivityManager::class.java
        )?.registerDefaultNetworkCallback(
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    doForEachObserver(checkURL())
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    doForEachObserver(false)
                }
            }
        )
    }

    fun addObserver(observer: (connected: Boolean) -> Unit) {
        this.observers.add(observer)
    }

    fun removeObserver(observer: (connected: Boolean) -> Unit) {
        this.observers.remove(observer)
    }

    // Prevent concurrent modification exception
    private fun doForEachObserver(connected: Boolean) {
        val iterator = observers.iterator()
        while (iterator.hasNext()) {
            iterator.next().invoke(connected)
        }
    }

    private fun checkURL(): Boolean {
        return try {
            URL(ExodusAPIInterface.BASE_URL)
                .openConnection()
                .connect()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun checkConnection() {
        observers.forEach {
            it.invoke(checkURL())
        }
    }
}
