package org.eu.exodus_privacy.exodusprivacy

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import org.eu.exodus_privacy.exodusprivacy.objects.Status
import org.eu.exodus_privacy.exodusprivacy.utils.DataStoreModule
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    dataStoreModule: DataStoreModule,
    private val notificationManager: NotificationManager,
    private val notificationChannel: NotificationChannel
) : ViewModel() {

    val policyAgreement = dataStoreModule.policyAgreement.asLiveData()
    val appSetup = dataStoreModule.appSetup.asLiveData()

    val dbStatus: MutableLiveData<Status> = MutableLiveData()

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannels() {
        notificationManager.createNotificationChannel(notificationChannel)
    }
}
