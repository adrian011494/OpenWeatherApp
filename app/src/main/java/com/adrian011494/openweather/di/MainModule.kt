package com.adrian011494.openweather.di

import com.adrian011494.openweather.network.OpenWeatherApi
import com.adrian011494.openweather.network.RetrofitBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent


@Module
@InstallIn(ViewModelComponent::class)
class MainModule {

    @Provides
    fun apiProvide(): OpenWeatherApi = RetrofitBuilder.apiService

}