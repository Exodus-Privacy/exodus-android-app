package org.eu.exodus_privacy.exodusprivacy.manager.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val _networkState = MutableStateFlow(checkURL())
    val networkState: StateFlow<Boolean> = _networkState

    private val _connectionObserver = MutableLiveData<Boolean>()
    val connectionObserver: LiveData<Boolean> = _connectionObserver

    init {
        ContextCompat.getSystemService(
            context,
            ConnectivityManager::class.java
        )?.registerNetworkCallback(
            NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build(),
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    _networkState.value = checkURL()
                    checkConnection()
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    _networkState.value = false
                    checkConnection()
                }
            }
        )
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
        _connectionObserver.postValue(_networkState.value)
    }
}
