package org.eu.exodus_privacy.exodusprivacy.fragments.apps

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.eu.exodus_privacy.exodusprivacy.manager.database.ExodusDatabaseRepository
import org.eu.exodus_privacy.exodusprivacy.manager.database.app.ExodusApplication
import javax.inject.Inject

@HiltViewModel
class AppsViewModel @Inject constructor(
    exodusDatabaseRepository: ExodusDatabaseRepository
) : ViewModel() {

    val appList: LiveData<List<ExodusApplication>> = exodusDatabaseRepository.getAllApps()
}
