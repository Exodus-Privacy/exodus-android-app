package org.eu.exodus_privacy.exodusprivacy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import org.eu.exodus_privacy.exodusprivacy.utils.DataStoreModule
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    dataStoreModule: DataStoreModule
) : ViewModel() {

    val policyAgreement = dataStoreModule.policyAgreement.asLiveData()
    val appSetup = dataStoreModule.appSetup.asLiveData()
}
