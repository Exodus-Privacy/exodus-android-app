package org.eu.exodus_privacy.exodusprivacy.fragments.trackers

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.eu.exodus_privacy.exodusprivacy.manager.database.ExodusDatabaseRepository
import org.eu.exodus_privacy.exodusprivacy.manager.database.tracker.TrackerData
import javax.inject.Inject

@HiltViewModel
class TrackersViewModel @Inject constructor(
    exodusDatabaseRepository: ExodusDatabaseRepository
) : ViewModel() {

    val trackersList: LiveData<List<TrackerData>> = exodusDatabaseRepository.getAllTrackers()
}
