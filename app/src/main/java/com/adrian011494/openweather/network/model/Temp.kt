package com.adrian011494.openweather.network.model

import com.google.gson.annotations.SerializedName


data class Temp (

  @SerializedName("day"   ) var day   : Double? = null,
  @SerializedName("min"   ) var min   : Double? = null,
  @SerializedName("max"   ) var max   : Double? = null,
  @SerializedName("night" ) var night : Double? = null,
  @SerializedName("eve"   ) var eve   : Double? = null,
  @SerializedName("morn"  ) var morn  : Double? = null

)