package org.eu.exodus_privacy.exodusprivacy.manager.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val TAG = NetworkManager::class.java.simpleName

    val isOnline: Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d(TAG, "Network available.")
                channel.trySend(true)
            }

            override fun onLost(network: Network) {
                Log.w(TAG, "Network not available.")
                channel.trySend(false)
            }
        }

        val connectivityManager = ContextCompat
            .getSystemService(context, ConnectivityManager::class.java)

        connectivityManager?.registerNetworkCallback(
            NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build(),
            callback,
        )

        channel.trySend(connectivityManager.isCurrentlyConnected())

        awaitClose {
            connectivityManager?.unregisterNetworkCallback(callback)
        }
    }.conflate()

    fun isExodusReachable(): Boolean {
        return try {
            URL(ExodusAPIInterface.BASE_URL)
                .openConnection()
                .connect()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Could Not Reach Exodus API URL.", e)
            false
        }
    }

    private fun ConnectivityManager?.isCurrentlyConnected() = when (this) {
        null -> false
        else ->
            activeNetwork
                ?.let(::getNetworkCapabilities)
                ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                ?: false
    }
}
