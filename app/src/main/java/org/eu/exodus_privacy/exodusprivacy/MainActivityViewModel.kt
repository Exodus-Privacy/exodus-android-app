package org.eu.exodus_privacy.exodusprivacy

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
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

    val networkConnection: LiveData<Boolean>
        get() {
            return networkManager.connectionObserver
        }
}
