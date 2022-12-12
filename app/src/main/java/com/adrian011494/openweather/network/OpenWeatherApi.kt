package com.adrian011494.openweather.network

import com.adrian011494.openweather.network.model.ForecastData
import com.adrian011494.openweather.network.model.WeatherData
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenWeatherApi {

    @GET("forecast")
    suspend fun forecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("cnt") cnt: Int = 5,
    ): ForecastData

    @GET("weather")
    suspend fun weather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
    ): WeatherData
}