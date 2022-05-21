package org.eu.exodus_privacy.exodusprivacy.fragments.trackerdetail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.eu.exodus_privacy.exodusprivacy.manager.database.ExodusDatabaseRepository
import org.eu.exodus_privacy.exodusprivacy.manager.database.tracker.TrackerData
import javax.inject.Inject

@HiltViewModel
class TrackerDetailViewModel @Inject constructor(
    private val exodusDatabaseRepository: ExodusDatabaseRepository
) : ViewModel() {

    val tracker: MutableLiveData<TrackerData> = MutableLiveData()

    fun getTracker(trackerID: Int) {
        viewModelScope.launch {
            tracker.value = exodusDatabaseRepository.getTrackers(trackerID)
        }
    }
}
