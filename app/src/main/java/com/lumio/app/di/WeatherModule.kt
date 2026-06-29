package com.lumio.app.di
import android.content.Context
import com.lumio.app.weather.WeatherRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
@Module
@InstallIn(SingletonComponent::class)
object WeatherModule {
    @Provides @Singleton
    fun provideWeatherRepository(@ApplicationContext context: Context): WeatherRepository = WeatherRepository(context)
}
