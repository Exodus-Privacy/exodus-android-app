package org.eu.exodus_privacy.exodusprivacy.manager.network

import org.eu.exodus_privacy.exodusprivacy.manager.network.data.AppDetails
import org.eu.exodus_privacy.exodusprivacy.manager.network.data.Trackers
import javax.inject.Inject

class ExodusAPIRepository @Inject constructor(
    private val exodusAPIInterface: ExodusAPIInterface
) {

    suspend fun getAllTrackers(): Trackers {
        return exodusAPIInterface.getAllTrackers()
    }

    suspend fun getAppDetails(packageName: String): List<AppDetails> {
        return exodusAPIInterface.getAppDetails(packageName)
    }
}