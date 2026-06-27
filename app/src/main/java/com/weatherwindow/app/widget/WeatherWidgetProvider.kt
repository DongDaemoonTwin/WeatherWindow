package com.weatherwindow.app.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.widget.RemoteViews
import com.weatherwindow.app.R
import com.weatherwindow.app.model.WeatherStore
import com.weatherwindow.app.scene.WeatherSceneRenderer

class WeatherWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        updateWidgets(context, appWidgetManager, appWidgetIds)
    }

    companion object {
        fun updateWidgets(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
            val state = WeatherStore.load(context)
            val renderer = WeatherSceneRenderer()

            appWidgetIds.forEach { id ->
                val bitmap = Bitmap.createBitmap(720, 360, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                renderer.draw(canvas, state, System.currentTimeMillis(), includeText = true)

                val views = RemoteViews(context.packageName, R.layout.widget_weather)
                views.setImageViewBitmap(R.id.widget_image, bitmap)
                appWidgetManager.updateAppWidget(id, views)
            }
        }
    }
}
