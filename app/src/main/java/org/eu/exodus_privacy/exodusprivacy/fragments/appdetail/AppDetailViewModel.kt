package org.eu.exodus_privacy.exodusprivacy.fragments.appdetail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.eu.exodus_privacy.exodusprivacy.manager.database.ExodusDatabaseRepository
import org.eu.exodus_privacy.exodusprivacy.manager.database.app.ExodusApplication
import javax.inject.Inject

@HiltViewModel
class AppDetailViewModel @Inject constructor(
    private val exodusDatabaseRepository: ExodusDatabaseRepository
) : ViewModel() {

    val app: MutableLiveData<ExodusApplication> = MutableLiveData()

    fun getApp(packageName: String) {
        viewModelScope.launch {
            app.value = exodusDatabaseRepository.getApp(packageName)
        }
    }
}
