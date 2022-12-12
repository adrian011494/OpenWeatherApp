package com.adrian011494.openweather.vm

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adrian011494.openweather.R
import com.adrian011494.openweather.network.ApiHelper
import com.adrian011494.openweather.network.model.ForecastData
import com.adrian011494.openweather.network.model.SearchCity
import com.adrian011494.openweather.network.model.WeatherData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(private val api: ApiHelper) : ViewModel() {


    val forecastData = MutableLiveData<ForecastData>()
    val weatherData = MutableLiveData<WeatherData>()
    val errorsMessage = MutableLiveData<String>()

    var searchCities = ArrayList<SearchCity>()


    /**
     * Send request to get forecast weather data
     */
    fun loadForecastWeather(lat: Double, lng: Double) {
        viewModelScope.launch {
            try {
                val data = api.getForecast(lat, lng)
                forecastData.postValue(data)
            } catch (e: Exception) {
                errorsMessage.postValue("Network Error!!")
            }
        }
    }

    /**
     * Send request to get weather data
     */
    fun loadWeather(lat: Double, lng: Double) {

        viewModelScope.launch {
            try {
                val data = api.getWeather(lat, lng)
                weatherData.postValue(data)
            } catch (e: Exception) {
                errorsMessage.postValue("Network Error!!")
            }
        }

    }


    /**
     * Load cities from file to search in future
     */
    fun loadSearchData(cxt: Context) {
        viewModelScope.launch {
            val raw: InputStream = cxt.resources.openRawResource(R.raw.cities_m)
            val rd: Reader = BufferedReader(InputStreamReader(raw))

            searchCities = Gson().fromJson(rd, object : TypeToken<ArrayList<SearchCity>>() {}.type)
        }
    }


}