package org.eu.exodus_privacy.exodusprivacy.manager.network

import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.eu.exodus_privacy.exodusprivacy.manager.network.data.AppDetails
import org.eu.exodus_privacy.exodusprivacy.manager.network.data.Trackers
import org.eu.exodus_privacy.exodusprivacy.utils.IoDispatcher
import javax.inject.Inject

class ExodusAPIRepository @Inject constructor(
    private val exodusAPIInterface: ExodusAPIInterface,
    private val networkManager: NetworkManager,
    @IoDispatcher val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val TAG = ExodusAPIRepository::class.java.simpleName

    suspend fun getAllTrackers(): Trackers {
        return withContext(ioDispatcher) {
            if (networkManager.isExodusReachable()) { // Do network calls in coroutine
                val result = exodusAPIInterface.getAllTrackers()
                return@withContext if (result.isSuccessful && result.body() != null) {
                    result.body()!!
                } else {
                    Log.w(TAG, "Failed to get trackers, response code: ${result.code()}. Returning empty Trackers object.")
                    Trackers()
                }
            } else {
                Trackers()
            }
        }
    }

    suspend fun getAppDetails(packageName: String): List<AppDetails> {
        return withContext(ioDispatcher) {
            if (networkManager.isExodusReachable()) {
                val result = exodusAPIInterface.getAppDetails(packageName)
                return@withContext if (result.isSuccessful && result.body() != null) {
                    result.body()!!
                } else {
                    Log.w(TAG, "Failed to get app details, response code: ${result.code()}. Returning emptyList.")
                    emptyList()
                }
            } else {
                emptyList()
            }
        }
    }
}
