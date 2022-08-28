package org.eu.exodus_privacy.exodusprivacy.manager.network

import android.util.Log
import org.eu.exodus_privacy.exodusprivacy.manager.network.data.AppDetails
import org.eu.exodus_privacy.exodusprivacy.manager.network.data.Trackers
import javax.inject.Inject

class ExodusAPIRepository @Inject constructor(
    private val exodusAPIInterface: ExodusAPIInterface
) {

    private val TAG = ExodusAPIRepository::class.java.simpleName

    suspend fun getAllTrackers(): Trackers {
        val result = exodusAPIInterface.getAllTrackers()
        return if (result.isSuccessful && result.body() != null) {
            result.body()!!
        } else {
            Log.d(TAG, "Failed to get trackers, response code: ${result.code()}")
            Trackers()
        }
    }

    suspend fun getAppDetails(packageName: String): List<AppDetails> {
        val result = exodusAPIInterface.getAppDetails(packageName)
        return if (result.isSuccessful && result.body() != null) {
            result.body()!!
        } else {
            Log.d(TAG, "Failed to get app details, response code: ${result.code()}")
            emptyList()
        }
    }
}
