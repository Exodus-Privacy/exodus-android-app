package org.eu.exodus_privacy.exodusprivacy

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import dagger.Module
import dagger.Provides
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.eu.exodus_privacy.exodusprivacy.manager.network.ExodusAPIInterface
import org.eu.exodus_privacy.exodusprivacy.manager.network.ExodusAPIRepository
import org.eu.exodus_privacy.exodusprivacy.manager.network.ExodusModule
import org.junit.Assert.assertEquals
import kotlin.test.BeforeTest
import org.junit.Rule
import kotlin.test.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.test.AfterTest
import kotlin.time.Duration.Companion.seconds

const val FAKE_PATH = "/api/requests/"
const val FAKE_PORT = 34567
const val FAKE_RESPONSE =
    """
        { "trackers": {
    "1": {
      "id": 1,
      "name": "Teemo",
      "description": "Placeholder",
      "creation_date": "2017-09-24",
      "code_signature": "com.databerries.|com.geolocstation.",
      "network_signature": "databerries\\.com",
      "website": "https://www.teemo.co",
      "categories": [
        "Analytics"
      ],
      "documentation": []
    }
  }
}   
"""

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [ExodusModule::class]
)
object FakeExodusModule {
    @Singleton
    @Provides
    fun provideExodusAPIInstance(okHttpClient: OkHttpClient): ExodusAPIInterface {
        return Retrofit.Builder()
            .baseUrl("http://localhost:$FAKE_PORT$FAKE_PATH")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(ExodusAPIInterface::class.java)
    }

    @Singleton
    @Provides
    fun provideInterceptor(): Interceptor {
        return Interceptor { chain ->
            val builder = chain.request().newBuilder()
            builder.header("Authorization", "Token ${BuildConfig.EXODUS_API_KEY}")
            return@Interceptor chain.proceed(builder.build())
        }
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(interceptor: Interceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()
    }
}

@HiltAndroidTest
class ExodusAPIRepositoryTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    private val mockWebServer = MockWebServer()
    private val socketPolicy = SocketPolicy.NO_RESPONSE
    private val testDispatcher = StandardTestDispatcher()

    @Inject
    lateinit var exodusAPIRepository: ExodusAPIRepository
    private lateinit var context: Context

    @BeforeTest
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().context
        mockWebServer.start(FAKE_PORT)
        mockWebServer.url(FAKE_PATH)
    }

    @AfterTest
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun exodusAPIRepositoryShouldTimeOut() = runTest(testDispatcher, timeout = 12.seconds) {
        // given
        hiltRule.inject()

        // when
        val mockResponse = MockResponse()
            .setBody(FAKE_RESPONSE)
            .setResponseCode(200)
            .setSocketPolicy(socketPolicy)
        mockWebServer.enqueue(mockResponse)

        // then
        val exception =
            try {
                exodusAPIRepository.getAllTrackers()
            } catch (
                exception: java.net.SocketTimeoutException
            ) {
                exception
            }
        assertEquals(
            "java.net.SocketTimeoutException: timeout",
            exception.toString()
        )
    }
}
