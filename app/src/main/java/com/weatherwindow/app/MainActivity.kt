package com.weatherwindow.app

import android.Manifest
import android.app.Activity
import android.app.WallpaperManager
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.weatherwindow.app.model.WeatherRepository
import com.weatherwindow.app.model.WeatherStore
import com.weatherwindow.app.wallpaper.WeatherWallpaperService
import com.weatherwindow.app.widget.WeatherWidgetProvider

class MainActivity : Activity() {
    private lateinit var statusView: TextView
    private val repository = WeatherRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        statusView = TextView(this).apply {
            text = WeatherStore.load(this@MainActivity).summary()
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(28, 48, 28, 28)
        }

        layout.addView(TextView(this).apply {
            text = "WeatherWindow"
            textSize = 26f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 20)
        })
        layout.addView(statusView)
        layout.addView(actionButton("Fetch Seoul weather") { fetchWeather(37.5665, 126.9780, "Seoul") })
        layout.addView(actionButton("Use device location") { fetchDeviceLocationWeather() })
        layout.addView(actionButton("Open live wallpaper picker") { openWallpaperPicker() })
        layout.addView(actionButton("Refresh widgets") { refreshWidgets() })

        setContentView(layout)
    }

    private fun actionButton(label: String, action: () -> Unit): Button = Button(this).apply {
        text = label
        setOnClickListener { action() }
    }

    private fun fetchWeather(lat: Double, lon: Double, label: String) {
        statusView.text = "Fetching weather for $label..."
        Thread {
            runCatching {
                repository.fetch(lat, lon, label)
            }.onSuccess { state ->
                WeatherStore.save(this, state)
                runOnUiThread {
                    statusView.text = state.summary()
                    refreshWidgets()
                }
            }.onFailure { error ->
                runOnUiThread { statusView.text = "Fetch failed: ${error.message}" }
            }
        }.start()
    }

    private fun fetchDeviceLocationWeather() {
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
            return
        }

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)
            .mapNotNull { provider -> runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull() }
            .firstOrNull()

        if (location == null) {
            statusView.text = "No last-known location yet. Using Seoul as fallback."
            fetchWeather(37.5665, 126.9780, "Seoul")
        } else {
            fetchWeather(location.latitude, location.longitude, "Current location")
        }
    }

    private fun openWallpaperPicker() {
        val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
            putExtra(
                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                ComponentName(this@MainActivity, WeatherWallpaperService::class.java)
            )
        }
        runCatching { startActivity(intent) }.onFailure {
            startActivity(Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER))
        }
    }

    private fun refreshWidgets() {
        val manager = AppWidgetManager.getInstance(this)
        val ids = manager.getAppWidgetIds(ComponentName(this, WeatherWidgetProvider::class.java))
        WeatherWidgetProvider.updateWidgets(this, manager, ids)
    }
}
