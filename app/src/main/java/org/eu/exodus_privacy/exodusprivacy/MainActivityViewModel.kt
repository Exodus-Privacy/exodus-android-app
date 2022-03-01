package org.eu.exodus_privacy.exodusprivacy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.eu.exodus_privacy.exodusprivacy.manager.database.ExodusDatabaseRepository
import org.eu.exodus_privacy.exodusprivacy.manager.database.tracker.TrackerData
import org.eu.exodus_privacy.exodusprivacy.manager.network.ExodusAPIRepository
import org.eu.exodus_privacy.exodusprivacy.utils.DataStoreModule
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val exodusAPIRepository: ExodusAPIRepository,
    private val exodusDatabaseRepository: ExodusDatabaseRepository,
    private val dataStoreModule: DataStoreModule
) : ViewModel() {

    val policyAgreement = dataStoreModule.policyAgreement.asLiveData()
    val appSetup = dataStoreModule.appSetup.asLiveData()

    fun fetchAndSaveTrackers() {
        viewModelScope.launch {
            val trackersList = exodusAPIRepository.getAllTrackers()
            for ((key, value) in trackersList.trackers) {
                val trackerData = TrackerData(
                    key.toInt(),
                    value.categories,
                    value.code_signature,
                    value.creation_date,
                    value.description,
                    value.name,
                    value.network_signature,
                    value.website
                )
                exodusDatabaseRepository.saveTrackerData(trackerData)
            }
        }
    }

    fun saveAppSetup(status: Boolean) {
        viewModelScope.launch {
            dataStoreModule.saveAppSetup(status)
        }
    }
}
