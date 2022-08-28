package org.eu.exodus_privacy.exodusprivacy.fragments.dialog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.eu.exodus_privacy.exodusprivacy.utils.DataStoreModule
import javax.inject.Inject

@HiltViewModel
class ExodusDialogViewModel @Inject constructor(
    private val dataStoreModule: DataStoreModule
) : ViewModel() {

    val policyAgreement = dataStoreModule.policyAgreement.asLiveData()

    fun savePolicyAgreement(status: Boolean) {
        viewModelScope.launch {
            dataStoreModule.savePolicyAgreement(status)
        }
    }
}
