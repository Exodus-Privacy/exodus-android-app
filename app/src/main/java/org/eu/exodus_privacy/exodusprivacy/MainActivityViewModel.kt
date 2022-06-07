package org.eu.exodus_privacy.exodusprivacy

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.eu.exodus_privacy.exodusprivacy.manager.network.NetworkManager
import org.eu.exodus_privacy.exodusprivacy.utils.DataStoreModule
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    dataStoreModule: DataStoreModule,
) : ViewModel() {

    @Inject
    lateinit var networkManager: NetworkManager

    val policyAgreement = dataStoreModule.policyAgreement.asLiveData()
    val appSetup = dataStoreModule.appSetup.asLiveData()

    private var networkScope = CoroutineScope(Dispatchers.IO)
    private val _networkConnection: MutableLiveData<Boolean> = MutableLiveData()
    private val networkObserver: (connected: Boolean) -> Unit = {
        viewModelScope.launch {
            _networkConnection.value = it
        }
    }

    val networkConnection: LiveData<Boolean>
        get() {
            val liveData = _networkConnection
            networkScope.launch {
                networkManager.addObserver(networkObserver)
            }
            return liveData
        }

    override fun onCleared() {
        networkScope.launch {
            networkManager.removeObserver(networkObserver)
        }
        super.onCleared()
    }
}
