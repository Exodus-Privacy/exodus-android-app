package org.eu.exodus_privacy.exodusprivacy.utils

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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
    ): NotificationManagerCompat {
        return NotificationManagerCompat.from(context)
    }

    @Singleton
    @Provides
    @RequiresApi(Build.VERSION_CODES.O)
    fun provideUpdateNotificationChannel(
        @ApplicationContext context: Context
    ): NotificationChannelCompat {
        return NotificationChannelCompat
            .Builder(UPDATES, NotificationManagerCompat.IMPORTANCE_LOW)
            .setName(context.getString(R.string.updates))
            .build()
    }

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
