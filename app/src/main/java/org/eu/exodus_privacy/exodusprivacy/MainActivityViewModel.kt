package org.eu.exodus_privacy.exodusprivacy

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.eu.exodus_privacy.exodusprivacy.manager.network.ExodusAPIRepository
import org.eu.exodus_privacy.exodusprivacy.manager.network.data.Trackers
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val exodusAPIRepository: ExodusAPIRepository
) : ViewModel() {

    val trackersList: MutableLiveData<Trackers> = MutableLiveData()

    fun getAllTrackers() {
        viewModelScope.launch {
            trackersList.postValue(exodusAPIRepository.getAllTrackers())
        }
    }
}
