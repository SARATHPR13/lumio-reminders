package com.lumio.app.di

import android.content.Context
import com.lumio.app.location.GeofenceManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocationModule {

    @Provides
    @Singleton
    fun provideGeofenceManager(
        @ApplicationContext context: Context
    ): GeofenceManager = GeofenceManager(context)
}
