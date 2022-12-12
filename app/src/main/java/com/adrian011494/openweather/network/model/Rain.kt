package com.adrian011494.openweather.network.model

import com.google.gson.annotations.SerializedName


data class Rain (

  @SerializedName("1h" ) var last1h : Double? = null

)