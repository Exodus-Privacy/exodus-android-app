package org.eu.exodus_privacy.exodusprivacy.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.eu.exodus_privacy.exodusprivacy.R
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NotificationManagerModule {

    private const val UPDATES = "updates"

    @Singleton
    @Provides
    fun provideNotificationManagerInstance(
        @ApplicationContext context: Context
    ): NotificationManager {
        return context.getSystemService(NotificationManager::class.java)
    }

    @Singleton
    @Provides
    @RequiresApi(Build.VERSION_CODES.O)
    fun provideUpdateNotificationChannel(
        @ApplicationContext context: Context
    ): NotificationChannel {
        return NotificationChannel(
            UPDATES,
            context.getString(R.string.updates),
            NotificationManager.IMPORTANCE_LOW
        )
    }

    @Singleton
    @Provides
    fun provideUpdateNotification(
        @ApplicationContext context: Context
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, UPDATES)
            .setSmallIcon(R.drawable.ic_update)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
    }
}
