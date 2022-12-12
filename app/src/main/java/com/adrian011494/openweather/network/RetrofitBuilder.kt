package com.adrian011494.openweather.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*


object RetrofitBuilder {

    var BASE_URL = "https://api.openweathermap.org/data/2.5/"
    var API_ID = "e584e19712018e69ec3ba0d3ea278295"

    private fun getRetrofit(): Retrofit {

        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC)
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(Interceptor {
                val originalHttpUrl  = it.request().url


               val url = originalHttpUrl.newBuilder()
                    .addQueryParameter("appid", API_ID)
                    .addQueryParameter("lang", Locale.getDefault().language)
                    .addQueryParameter("units", "metric")
                    .build()

                val requestBuilder = it.request().newBuilder()
                    .url(url)

                it.proceed(requestBuilder.build())
            })
            .build()


        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    val apiService: OpenWeatherApi = getRetrofit().create(OpenWeatherApi::class.java)
}