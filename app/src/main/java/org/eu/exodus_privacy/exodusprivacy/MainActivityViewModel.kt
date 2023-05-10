package org.eu.exodus_privacy.exodusprivacy

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import org.eu.exodus_privacy.exodusprivacy.manager.network.NetworkManager
import org.eu.exodus_privacy.exodusprivacy.manager.storage.ExodusConfig
import org.eu.exodus_privacy.exodusprivacy.manager.storage.DataStoreRepository
import org.eu.exodus_privacy.exodusprivacy.utils.IoDispatcher
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val configStorage: DataStoreRepository<ExodusConfig>,
    private val networkManager: NetworkManager
) : ViewModel() {

    var config = mapOf<String, ExodusConfig>()

    init {
        loadConfigs()
    }

    private val TAG = MainActivityViewModel::class.java.simpleName
    // TODO: Somehow need to load the data and close the collection, maybe do like before
    private fun loadConfigs() {
        runBlocking {
            Log.d(TAG, "Collecting config: $config from Storage.")
            config = configStorage.getAll().first()
            Log.d(TAG, "Done Collecting.")
        }
    }

    fun saveNotificationPermission(status: Boolean) {
        val newConfig = config as MutableMap
        newConfig["notification_perm"] = ExodusConfig("notification_requested", status)
        viewModelScope.launch {
            configStorage.insert(newConfig)
        }
    }

    fun saveAppSetup(status: Boolean) {
        val newConfig = config as MutableMap
        newConfig["app_setup"] = ExodusConfig("is_setup_complete", status)
        viewModelScope.launch {
            configStorage.insert(newConfig)
        }
    }

    val networkConnection: LiveData<Boolean>
        get() {
            return networkManager.connectionObserver
        }
}
