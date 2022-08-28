package org.eu.exodus_privacy.exodusprivacy.manager.network

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.eu.exodus_privacy.exodusprivacy.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ExodusModule {

    @Singleton
    @Provides
    fun provideExodusAPIInstance(okHttpClient: OkHttpClient): ExodusAPIInterface {
        return Retrofit.Builder()
            .baseUrl(ExodusAPIInterface.BASE_URL)
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
