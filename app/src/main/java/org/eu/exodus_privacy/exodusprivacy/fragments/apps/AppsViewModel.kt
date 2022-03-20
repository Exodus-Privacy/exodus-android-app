package org.eu.exodus_privacy.exodusprivacy.fragments.apps

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.eu.exodus_privacy.exodusprivacy.objects.Application
import javax.inject.Inject

@HiltViewModel
class AppsViewModel @Inject constructor(
    private val applicationList: MutableList<Application>,
) : ViewModel() {

    val appList: MutableLiveData<List<Application>> = MutableLiveData()

    fun getAppList() {
        appList.value = applicationList
    }
}