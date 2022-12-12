package com.adrian011494.openweather.network.model

import com.google.gson.annotations.SerializedName


data class ForecastData(

  @SerializedName("city"    ) var city    : City?           = City(),
  @SerializedName("cod"     ) var cod     : String?         = null,
  @SerializedName("message" ) var message : Double?         = null,
  @SerializedName("cnt"     ) var cnt     : Int?            = null,
  @SerializedName("list"    ) var list    : ArrayList<DayWeather> = arrayListOf()

)