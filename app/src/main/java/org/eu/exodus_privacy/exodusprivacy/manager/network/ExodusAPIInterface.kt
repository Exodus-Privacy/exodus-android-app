package org.eu.exodus_privacy.exodusprivacy.manager.network

import org.eu.exodus_privacy.exodusprivacy.manager.network.data.AppDetails
import org.eu.exodus_privacy.exodusprivacy.manager.network.data.Trackers
import retrofit2.http.GET
import retrofit2.http.Path

interface ExodusAPIInterface {

    companion object {
        const val BASE_URL = "https://reports.exodus-privacy.eu.org/api/"
    }

    @GET("trackers")
    suspend fun getAllTrackers(): Trackers

    @GET("search/{packageName}/details")
    suspend fun getAppDetails(
        @Path("packageName") packageName: String
    ): List<AppDetails>
}