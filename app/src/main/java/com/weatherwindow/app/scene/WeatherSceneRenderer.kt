package com.weatherwindow.app.scene

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import com.weatherwindow.app.model.SceneMath
import com.weatherwindow.app.model.WeatherSceneState
import com.weatherwindow.app.model.WeatherType
import java.time.LocalDateTime
import kotlin.math.PI
import kotlin.math.sin

class WeatherSceneRenderer {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun draw(canvas: Canvas, state: WeatherSceneState, nowMillis: Long, includeText: Boolean) {
        val width = canvas.width.toFloat()
        val height = canvas.height.toFloat()
        val now = LocalDateTime.now()
        val isDay = now.isAfter(state.sunrise) && now.isBefore(state.sunset)

        drawSky(canvas, width, height, state, now, isDay)
        if (isDay) drawSun(canvas, width, height, state, now) else drawNight(canvas, width, height, state, now)
        drawClouds(canvas, width, height, state, nowMillis)
        drawWeatherEffects(canvas, width, height, state, nowMillis)
        drawWindowFrame(canvas, width, height)
        if (includeText) drawLabel(canvas, width, height, state)
    }

    private fun drawSky(canvas: Canvas, width: Float, height: Float, state: WeatherSceneState, now: LocalDateTime, isDay: Boolean) {
        val day = SceneMath.dayProgress(now, state.sunrise, state.sunset)
        val top: Int
        val bottom: Int

        if (!isDay) {
            top = Color.rgb(9, 15, 35)
            bottom = Color.rgb(22, 34, 58)
        } else if (day < 0.18f) {
            top = mix(Color.rgb(38, 44, 92), Color.rgb(120, 174, 220), day / 0.18f)
            bottom = mix(Color.rgb(245, 161, 122), Color.rgb(190, 226, 244), day / 0.18f)
        } else if (day > 0.78f) {
            val t = (day - 0.78f) / 0.22f
            top = mix(Color.rgb(82, 154, 219), Color.rgb(54, 42, 93), t)
            bottom = mix(Color.rgb(194, 231, 247), Color.rgb(247, 139, 91), t)
        } else {
            top = Color.rgb(80, 158, 225)
            bottom = Color.rgb(193, 231, 248)
        }

        val darkness = when (state.weatherType) {
            WeatherType.CLEAR -> 0.0f
            WeatherType.CLOUDY -> 0.16f
            WeatherType.RAIN -> 0.36f
            WeatherType.SNOW -> 0.06f
            WeatherType.FOG -> 0.24f
            WeatherType.THUNDER -> 0.46f
        }

        paint.shader = LinearGradient(
            0f, 0f, 0f, height,
            darken(top, darkness),
            darken(bottom, darkness),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, width, height, paint)
        paint.shader = null
    }

    private fun drawSun(canvas: Canvas, width: Float, height: Float, state: WeatherSceneState, now: LocalDateTime) {
        val progress = SceneMath.dayProgress(now, state.sunrise, state.sunset)
        val x = width * progress
        val horizon = height * 0.74f
        val noon = height * 0.18f
        val arc = sin(progress * PI).toFloat()
        val y = horizon - ((horizon - noon) * arc)

        paint.shader = RadialGradient(x, y, 120f, Color.argb(130, 255, 224, 110), Color.TRANSPARENT, Shader.TileMode.CLAMP)
        canvas.drawCircle(x, y, 120f, paint)
        paint.shader = null
        paint.color = Color.rgb(255, 221, 92)
        canvas.drawCircle(x, y, 34f, paint)
    }

    private fun drawNight(canvas: Canvas, width: Float, height: Float, state: WeatherSceneState, now: LocalDateTime) {
        paint.color = Color.argb(170, 255, 255, 255)
        repeat(36) { i ->
            val x = ((i * 83) % width.toInt()).toFloat()
            val y = 30f + ((i * 47) % (height * 0.42f).toInt()).toFloat()
            canvas.drawCircle(x, y, if (i % 4 == 0) 2.4f else 1.4f, paint)
        }

        val nextSunrise = state.sunrise.plusDays(1)
        val progress = SceneMath.nightProgress(now, state.sunset, nextSunrise)
        val x = width * progress
        val y = height * 0.22f + sin(progress * PI).toFloat() * height * 0.18f
        paint.color = Color.rgb(236, 239, 210)
        canvas.drawCircle(x, y, 24f, paint)
        paint.color = Color.rgb(22, 34, 58)
        canvas.drawCircle(x + 10f, y - 6f, 23f, paint)
    }

    private fun drawClouds(canvas: Canvas, width: Float, height: Float, state: WeatherSceneState, nowMillis: Long) {
        val cover = (state.cloudCover / 100.0).toFloat().coerceIn(0f, 1f)
        val cloudCount = (2 + cover * 6).toInt()
        paint.color = when (state.weatherType) {
            WeatherType.RAIN, WeatherType.THUNDER -> Color.argb(190, 78, 88, 108)
            else -> Color.argb((90 + cover * 90).toInt(), 245, 248, 250)
        }

        repeat(cloudCount) { i ->
            val speed = 0.012f + state.windSpeed.toFloat() * 0.003f
            val baseX = ((i * width / cloudCount) + (nowMillis * speed) % (width + 220f)) - 160f
            val y = height * (0.14f + (i % 4) * 0.08f)
            drawCloud(canvas, baseX, y, 0.82f + i * 0.05f)
        }
    }

    private fun drawCloud(canvas: Canvas, x: Float, y: Float, scale: Float) {
        canvas.drawOval(RectF(x, y + 22f * scale, x + 140f * scale, y + 70f * scale), paint)
        canvas.drawCircle(x + 38f * scale, y + 24f * scale, 32f * scale, paint)
        canvas.drawCircle(x + 82f * scale, y + 16f * scale, 42f * scale, paint)
        canvas.drawCircle(x + 122f * scale, y + 32f * scale, 26f * scale, paint)
    }

    private fun drawWeatherEffects(canvas: Canvas, width: Float, height: Float, state: WeatherSceneState, nowMillis: Long) {
        when (state.weatherType) {
            WeatherType.RAIN, WeatherType.THUNDER -> drawRain(canvas, width, height, state, nowMillis)
            WeatherType.SNOW -> drawSnow(canvas, width, height, state, nowMillis)
            WeatherType.FOG -> drawFog(canvas, width, height)
            else -> Unit
        }

        if (state.weatherType == WeatherType.THUNDER && (nowMillis / 2500L) % 7L == 0L) {
            paint.color = Color.argb(92, 255, 255, 255)
            canvas.drawRect(0f, 0f, width, height, paint)
        }
    }

    private fun drawRain(canvas: Canvas, width: Float, height: Float, state: WeatherSceneState, nowMillis: Long) {
        val count = (90 + state.rainIntensity * 90).toInt().coerceIn(90, 360)
        paint.color = Color.argb(150, 200, 224, 245)
        paint.strokeWidth = 2.3f
        repeat(count) { i ->
            val x = ((i * 61) % width.toInt()).toFloat()
            val y = ((nowMillis / 7 + i * 43) % height.toInt()).toFloat()
            canvas.drawLine(x, y, x - 13f, y + 38f, paint)
        }
    }

    private fun drawSnow(canvas: Canvas, width: Float, height: Float, state: WeatherSceneState, nowMillis: Long) {
        val count = (70 + state.snowIntensity * 120).toInt().coerceIn(70, 260)
        paint.color = Color.argb(210, 255, 255, 255)
        repeat(count) { i ->
            val drift = sin((nowMillis / 700f) + i).toFloat() * 18f
            val x = ((i * 71) % width.toInt()).toFloat() + drift
            val y = ((nowMillis / 22 + i * 37) % height.toInt()).toFloat()
            canvas.drawCircle(x, y, 2f + (i % 4), paint)
        }
    }

    private fun drawFog(canvas: Canvas, width: Float, height: Float) {
        paint.shader = LinearGradient(0f, 0f, 0f, height, Color.argb(170, 235, 238, 240), Color.argb(70, 235, 238, 240), Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, width, height, paint)
        paint.shader = null
    }

    private fun drawWindowFrame(canvas: Canvas, width: Float, height: Float) {
        val margin = width * 0.08f
        val top = height * 0.10f
        val bottom = height * 0.90f
        val frame = RectF(margin, top, width - margin, bottom)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = width * 0.028f
        paint.color = Color.argb(230, 34, 31, 38)
        canvas.drawRoundRect(frame, 36f, 36f, paint)

        paint.strokeWidth = width * 0.012f
        canvas.drawLine(width / 2f, top, width / 2f, bottom, paint)
        canvas.drawLine(margin, (top + bottom) / 2f, width - margin, (top + bottom) / 2f, paint)

        paint.style = Paint.Style.FILL
        paint.color = Color.argb(35, 255, 255, 255)
        canvas.drawRoundRect(frame, 36f, 36f, paint)
        paint.style = Paint.Style.FILL
    }

    private fun drawLabel(canvas: Canvas, width: Float, height: Float, state: WeatherSceneState) {
        paint.color = Color.argb(210, 18, 22, 30)
        canvas.drawRoundRect(RectF(width * 0.13f, height * 0.74f, width * 0.87f, height * 0.86f), 28f, 28f, paint)
        paint.color = Color.WHITE
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = width * 0.055f
        canvas.drawText("${state.cityName} · ${state.temperature.toInt()}°C · ${state.weatherType.name.lowercase()}", width / 2f, height * 0.815f, paint)
        paint.textAlign = Paint.Align.LEFT
    }

    private fun mix(a: Int, b: Int, t: Float): Int {
        val clamped = t.coerceIn(0f, 1f)
        return Color.rgb(
            (Color.red(a) + (Color.red(b) - Color.red(a)) * clamped).toInt(),
            (Color.green(a) + (Color.green(b) - Color.green(a)) * clamped).toInt(),
            (Color.blue(a) + (Color.blue(b) - Color.blue(a)) * clamped).toInt()
        )
    }

    private fun darken(color: Int, amount: Float): Int {
        val factor = (1f - amount).coerceIn(0f, 1f)
        return Color.rgb((Color.red(color) * factor).toInt(), (Color.green(color) * factor).toInt(), (Color.blue(color) * factor).toInt())
    }
}
