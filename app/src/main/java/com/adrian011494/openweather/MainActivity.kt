package com.adrian011494.openweather

import android.Manifest
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.database.MatrixCursor
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.BaseColumns
import android.text.format.DateFormat
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CursorAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.cursoradapter.widget.SimpleCursorAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.ui.AppBarConfiguration
import androidx.recyclerview.widget.LinearLayoutManager
import com.adrian011494.openweather.adapter.MainAdapter
import com.adrian011494.openweather.databinding.ActivityMainBinding
import com.adrian011494.openweather.network.model.DayWeather
import com.adrian011494.openweather.network.model.SearchCity
import com.adrian011494.openweather.network.model.WeatherData
import com.adrian011494.openweather.vm.MainViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.math.min

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), LocationListener {

    private lateinit var mainAdapter: MainAdapter
    private lateinit var viewModel: MainViewModel
    private lateinit var binding: ActivityMainBinding

    private val modelMain: MutableList<DayWeather> = ArrayList()

    private var lat: Double? = null
    private var lng: Double? = null

    var permissionArrays = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(findViewById(R.id.toolbarLayout))

        //set Permission
        if (!checkIfAlreadyHavePermission()) {
            requestPermissions(permissionArrays, 101)
        }

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        // show error msg
        viewModel.errorsMessage.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }

        mainAdapter = MainAdapter(modelMain)
        rvListWeather.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvListWeather.setHasFixedSize(true)
        rvListWeather.adapter = mainAdapter


        viewModel.forecastData.observe(this) {
            binding.shimmerLayout.stopShimmer()
            binding.shimmerLayout.visibility = View.GONE
            modelMain.clear()
            modelMain.addAll(it.list)
            mainAdapter?.notifyDataSetChanged()

        }

        viewModel.weatherData.observe(this) {
            displayCurrentWeather(it)
        }

        // load cities from file to search
        viewModel.loadSearchData(this)


        getToday()
        getLatlong()
    }

    /**
     * Display the current weather data.
     */
    private fun displayCurrentWeather(it: WeatherData?) {
        it?.let { data ->
            tvWeather.text = data.weather.firstOrNull()?.description?.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.getDefault()
                ) else it.toString()
            }
                ?: ""

            title = "${data.name}, ${data.sys?.country}"
            tvTemp.text = String.format(Locale.getDefault(), "%.0f°C", data.main?.temp ?: 0)

            tvSpeed.text = "Wind: ${data.wind?.speed} m/s"
            tvHumidity.text = "Humidity: ${data.main?.humidity}%"

            if (data.weather.firstOrNull()?.icon?.startsWith("04") == true) {
                iconTemp.setAnimation(R.raw.broken_clouds)
            } else if (data.weather.firstOrNull()?.icon?.startsWith("09") == true) {
                iconTemp.setAnimation(R.raw.light_rain)
            } else if (data.weather.firstOrNull()?.icon?.startsWith("04") == true) {
                iconTemp.setAnimation(R.raw.overcast_clouds)
            } else if (data.weather.firstOrNull()?.icon?.startsWith("10") == true) {
                iconTemp.setAnimation(R.raw.moderate_rain)
            } else if (data.weather.firstOrNull()?.icon?.startsWith("02") == true) {
                iconTemp.setAnimation(R.raw.few_clouds)
            } else if (data.weather.firstOrNull()?.icon?.startsWith("11") == true) {
                iconTemp.setAnimation(R.raw.heavy_intentsity)
            } else if (data.weather.firstOrNull()?.icon?.startsWith("01") == true) {
                iconTemp.setAnimation(R.raw.clear_sky)
            } else if (data.weather.firstOrNull()?.icon?.startsWith("03") == true) {
                iconTemp.setAnimation(R.raw.scattered_clouds)
            } else {
                iconTemp.setAnimation(R.raw.unknown)
            }

            iconTemp.playAnimation()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)

        // Associate searchable configuration with the SearchView
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager

        (menu.findItem(R.id.search).actionView as SearchView).apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))

            // Suggestion logic

            val from =
                arrayOf(SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_2)
            val to = intArrayOf(R.id.item_label, R.id.item_subLabel)
            val cursorAdapter = SimpleCursorAdapter(
                context,
                R.layout.search_suggest,
                null,
                from,
                to,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
            )

            suggestionsAdapter = cursorAdapter

            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {

                    return false
                }

                override fun onQueryTextChange(query: String?): Boolean {
                    val cursor = MatrixCursor(
                        arrayOf(
                            BaseColumns._ID,
                            SearchManager.SUGGEST_COLUMN_TEXT_1,
                            SearchManager.SUGGEST_COLUMN_TEXT_2,
                            SearchCity::class.simpleName
                        )
                    )
                    query?.takeIf { it.isNotBlank() }?.let {
                        viewModel.searchCities.filter { city ->
                            city.name.lowercase().contains(it.lowercase())
                        }
                            .let { list ->
                                list.subList(0, min(20, list.size))
                                    .forEachIndexed { index, searchCity ->
                                        cursor.addRow(
                                            arrayOf(
                                                index,
                                                searchCity.name,
                                                searchCity.country,
                                                Gson().toJson(searchCity)
                                            )
                                        )
                                    }
                            }
                    }

                    cursorAdapter.changeCursor(cursor)
                    return true
                }
            })

            setOnSuggestionListener(object : SearchView.OnSuggestionListener {
                override fun onSuggestionSelect(position: Int): Boolean {
                    return false
                }

                override fun onSuggestionClick(position: Int): Boolean {

                    val cursor = suggestionsAdapter.getItem(position) as Cursor

                    val columnIndex = cursor.getColumnIndex(SearchCity::class.simpleName)
                    val selection =
                        Gson().fromJson(cursor.getString(columnIndex), SearchCity::class.java)

                    setQuery("${selection.name}, ${selection.country}", false)

                    onChangeSearch(selection)

                    // Do something with selection
                    return true
                }
            })
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {

            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun checkIfAlreadyHavePermission(): Boolean {
        val result =
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        return result == PackageManager.PERMISSION_GRANTED
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        for (grantResult in grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                val intent = intent
                finish()
                startActivity(intent)
            } else {
                getLatlong()
            }
        }
    }

    /**
     * Display today date.
     */
    private fun getToday() {
        val date = Calendar.getInstance().time
        val today = DateFormat.format("d MMM yyyy", date) as String
        val formatDate = today.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(
                Locale.getDefault()
            ) else it.toString()
        }
        tvDate.text = formatDate
    }

    /**
     * Find current location and subscribe a location change listener.
     */
    private fun getLatlong() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                115
            )
            return
        }
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        val criteria = Criteria()
        val provider = locationManager.getBestProvider(criteria, true)
        val location = locationManager.getLastKnownLocation(provider ?: "")
        if (location != null) {
            onLocationChanged(location)
        } else {
            locationManager.requestLocationUpdates(provider ?: "", 20000, 0f, this)
        }
    }

    /**
     * Called when the location has changed.
     *
     * Uses view model to retrieve the weather data.
     */
    override fun onLocationChanged(location: Location) {
        lng = location.longitude
        lat = location.latitude

        if (lat != null && lng != null) {
            viewModel.loadForecastWeather(lat!!, lng!!)
            viewModel.loadWeather(lat!!, lng!!)
        }
    }

    /**
     * Called when the search has change.
     *
     * Uses view model to retrieve the weather data.
     */
    fun onChangeSearch(searchCity: SearchCity) {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        locationManager.removeUpdates(this)

        viewModel.loadForecastWeather(searchCity.lat.toDouble(), searchCity.lng.toDouble())
        viewModel.loadWeather(searchCity.lat.toDouble(), searchCity.lng.toDouble())
    }
}