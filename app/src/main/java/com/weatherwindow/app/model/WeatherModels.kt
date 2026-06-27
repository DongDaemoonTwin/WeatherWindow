package com.weatherwindow.app.model

import android.content.Context
import org.json.JSONObject
import java.net.URL
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.math.max
import kotlin.math.min

private const val PREFS = "weather_window_scene"

enum class WeatherType { CLEAR, CLOUDY, RAIN, SNOW, FOG, THUNDER }

data class WeatherSceneState(
    val cityName: String,
    val temperature: Double,
    val weatherType: WeatherType,
    val cloudCover: Double,
    val rainIntensity: Double,
    val snowIntensity: Double,
    val windSpeed: Double,
    val sunrise: LocalDateTime,
    val sunset: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    fun summary(): String {
        return "$cityName · ${temperature.toInt()}°C · ${weatherType.name.lowercase()}\n" +
            "Cloud ${cloudCover.toInt()}% · Rain ${"%.1f".format(rainIntensity)}mm · Snow ${"%.1f".format(snowIntensity)}cm"
    }

    companion object {
        fun fallback(): WeatherSceneState {
            val today = LocalDate.now()
            return WeatherSceneState(
                cityName = "Seoul",
                temperature = 22.0,
                weatherType = WeatherType.CLEAR,
                cloudCover = 25.0,
                rainIntensity = 0.0,
                snowIntensity = 0.0,
                windSpeed = 2.0,
                sunrise = today.atTime(6, 0),
                sunset = today.atTime(19, 30),
                updatedAt = LocalDateTime.now()
            )
        }
    }
}

object WeatherCodeMapper {
    fun map(code: Int): WeatherType = when (code) {
        0, 1 -> WeatherType.CLEAR
        2, 3 -> WeatherType.CLOUDY
        45, 48 -> WeatherType.FOG
        51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82 -> WeatherType.RAIN
        71, 73, 75, 77, 85, 86 -> WeatherType.SNOW
        95, 96, 99 -> WeatherType.THUNDER
        else -> WeatherType.CLOUDY
    }
}

object WeatherStore {
    fun save(context: Context, state: WeatherSceneState) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString("cityName", state.cityName)
            .putFloat("temperature", state.temperature.toFloat())
            .putString("weatherType", state.weatherType.name)
            .putFloat("cloudCover", state.cloudCover.toFloat())
            .putFloat("rainIntensity", state.rainIntensity.toFloat())
            .putFloat("snowIntensity", state.snowIntensity.toFloat())
            .putFloat("windSpeed", state.windSpeed.toFloat())
            .putString("sunrise", state.sunrise.toString())
            .putString("sunset", state.sunset.toString())
            .putString("updatedAt", state.updatedAt.toString())
            .apply()
    }

    fun load(context: Context): WeatherSceneState {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val fallback = WeatherSceneState.fallback()
        return WeatherSceneState(
            cityName = prefs.getString("cityName", fallback.cityName) ?: fallback.cityName,
            temperature = prefs.getFloat("temperature", fallback.temperature.toFloat()).toDouble(),
            weatherType = runCatching {
                WeatherType.valueOf(prefs.getString("weatherType", fallback.weatherType.name) ?: fallback.weatherType.name)
            }.getOrDefault(fallback.weatherType),
            cloudCover = prefs.getFloat("cloudCover", fallback.cloudCover.toFloat()).toDouble(),
            rainIntensity = prefs.getFloat("rainIntensity", fallback.rainIntensity.toFloat()).toDouble(),
            snowIntensity = prefs.getFloat("snowIntensity", fallback.snowIntensity.toFloat()).toDouble(),
            windSpeed = prefs.getFloat("windSpeed", fallback.windSpeed.toFloat()).toDouble(),
            sunrise = parseDateTime(prefs.getString("sunrise", null), fallback.sunrise),
            sunset = parseDateTime(prefs.getString("sunset", null), fallback.sunset),
            updatedAt = parseDateTime(prefs.getString("updatedAt", null), fallback.updatedAt)
        )
    }

    private fun parseDateTime(value: String?, fallback: LocalDateTime): LocalDateTime {
        return runCatching { if (value == null) fallback else LocalDateTime.parse(value) }.getOrDefault(fallback)
    }
}

class WeatherRepository {
    fun fetch(latitude: Double, longitude: Double, label: String): WeatherSceneState {
        val url = "https://api.open-meteo.com/v1/forecast" +
            "?latitude=$latitude" +
            "&longitude=$longitude" +
            "&current=temperature_2m,is_day,weather_code,precipitation,rain,snowfall,cloud_cover,wind_speed_10m" +
            "&daily=sunrise,sunset" +
            "&timezone=auto"

        val json = JSONObject(URL(url).readText())
        val current = json.getJSONObject("current")
        val daily = json.getJSONObject("daily")
        val weatherCode = current.optInt("weather_code", 3)

        return WeatherSceneState(
            cityName = label,
            temperature = current.optDouble("temperature_2m", 22.0),
            weatherType = WeatherCodeMapper.map(weatherCode),
            cloudCover = clamp(current.optDouble("cloud_cover", 40.0), 0.0, 100.0),
            rainIntensity = max(0.0, current.optDouble("rain", current.optDouble("precipitation", 0.0))),
            snowIntensity = max(0.0, current.optDouble("snowfall", 0.0)),
            windSpeed = max(0.0, current.optDouble("wind_speed_10m", 0.0)),
            sunrise = LocalDateTime.parse(daily.getJSONArray("sunrise").getString(0)),
            sunset = LocalDateTime.parse(daily.getJSONArray("sunset").getString(0)),
            updatedAt = LocalDateTime.now()
        )
    }

    private fun clamp(value: Double, minValue: Double, maxValue: Double): Double {
        return min(max(value, minValue), maxValue)
    }
}

object SceneMath {
    fun dayProgress(now: LocalDateTime, sunrise: LocalDateTime, sunset: LocalDateTime): Float {
        val total = java.time.Duration.between(sunrise, sunset).seconds.toFloat().coerceAtLeast(1f)
        val passed = java.time.Duration.between(sunrise, now).seconds.toFloat()
        return (passed / total).coerceIn(0f, 1f)
    }

    fun nightProgress(now: LocalDateTime, sunset: LocalDateTime, nextSunrise: LocalDateTime): Float {
        val end = if (nextSunrise.isAfter(sunset)) nextSunrise else nextSunrise.plusDays(1)
        val total = java.time.Duration.between(sunset, end).seconds.toFloat().coerceAtLeast(1f)
        val passed = java.time.Duration.between(sunset, now).seconds.toFloat()
        return (passed / total).coerceIn(0f, 1f)
    }
}
