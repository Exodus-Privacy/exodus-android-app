package org.eu.exodus_privacy.exodusprivacy

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.eu.exodus_privacy.exodusprivacy.manager.network.NetworkManager
import org.eu.exodus_privacy.exodusprivacy.utils.DataStoreModule
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val dataStoreModule: DataStoreModule,
) : ViewModel() {

    @Inject
    lateinit var networkManager: NetworkManager

    val policyAgreement = dataStoreModule.policyAgreement.asLiveData()
    val appSetup = dataStoreModule.appSetup.asLiveData()
    val notificationPermissions = dataStoreModule.notificationPerms.asLiveData()

    fun saveNotificationPermissions(status: Set<String>) {
        viewModelScope.launch {
            dataStoreModule.saveNotificationPerms(status)
        }
    }

    val networkConnection: LiveData<Boolean>
        get() {
            return networkManager.connectionObserver
        }
}
