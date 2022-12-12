package com.adrian011494.openweather.network

import javax.inject.Inject

class ApiHelper @Inject constructor(val api: OpenWeatherApi) {

    suspend fun getWeather(
        lat: Double,
        lon: Double,
    ) = api.weather(lat, lon)


    suspend fun getForecast(
        lat: Double,
        lon: Double,
    ) = api.forecast(lat, lon)
}