package com.adrian011494.openweather.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.adrian011494.openweather.R
import com.adrian011494.openweather.network.model.DayWeather
import com.adrian011494.openweather.textdrawable.ColorGenerator
import com.airbnb.lottie.LottieAnimationView
import kotlinx.android.synthetic.main.list_item_main.view.*
import java.text.SimpleDateFormat
import java.util.*


class MainAdapter(private val items: List<DayWeather>) : RecyclerView.Adapter<MainAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_main, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = items[position]
        val generator = ColorGenerator.MATERIAL

        // generate random color
        val color = generator.getColor(data.main?.temp)
        holder.cvListWeather.setCardBackgroundColor(color)

        val format = SimpleDateFormat("kk:mm", Locale.getDefault())
        val readableTime = format.format(Date((data.dt ?: 0) * 1000L))


        holder.tvNameDay.text = readableTime
        holder.tvTemp.text = String.format(Locale.getDefault(), "%.0f°C", data.main?.temp)
        holder.tvTempMin.text = String.format(Locale.getDefault(), "%.0f°C", data.main?.tempMin)
        holder.tvTempMax.text = String.format(Locale.getDefault(), "%.0f°C", data.main?.tempMax)

        if (data.weather.firstOrNull()?.icon?.startsWith("04") == true) {
            holder.iconTemp.setAnimation(R.raw.broken_clouds)
        } else if (data.weather.firstOrNull()?.icon?.startsWith("09") == true) {
            holder.iconTemp.setAnimation(R.raw.light_rain)
        } else if (data.weather.firstOrNull()?.icon?.startsWith("04") == true) {
            holder.iconTemp.setAnimation(R.raw.overcast_clouds)
        } else if (data.weather.firstOrNull()?.icon?.startsWith("10") == true) {
            holder.iconTemp.setAnimation(R.raw.moderate_rain)
        } else if (data.weather.firstOrNull()?.icon?.startsWith("02") == true) {
            holder.iconTemp.setAnimation(R.raw.few_clouds)
        } else if (data.weather.firstOrNull()?.icon?.startsWith("11") == true) {
            holder.iconTemp.setAnimation(R.raw.heavy_intentsity)
        } else if (data.weather.firstOrNull()?.icon?.startsWith("01") == true) {
            holder.iconTemp.setAnimation(R.raw.clear_sky)
        } else if (data.weather.firstOrNull()?.icon?.startsWith("03") == true) {
            holder.iconTemp.setAnimation(R.raw.scattered_clouds)
        } else {
            holder.iconTemp.setAnimation(R.raw.unknown)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var cvListWeather: CardView
        var tvNameDay: TextView
        var tvTemp: TextView
        var tvTempMin: TextView
        var tvTempMax: TextView
        var iconTemp: LottieAnimationView

        init {
            cvListWeather = itemView.cvListWeather
            tvNameDay = itemView.tvNameDay
            tvTemp = itemView.tvTemp
            tvTempMin = itemView.tvTempMin
            tvTempMax = itemView.tvTempMax
            iconTemp = itemView.iconTemp
        }
    }
}