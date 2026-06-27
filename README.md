# WeatherWindow

Android-first prototype for a live weather wallpaper and home screen widget.

## Concept

WeatherWindow is not a full weather app. The product surface is:

- Android live wallpaper
- Android home screen widget

The app screen is only for setup, preview, permission, and refresh controls.

## Included in this prototype

- Native Android/Kotlin scaffold
- `WallpaperService` live wallpaper renderer
- Home screen widget that renders a static weather-scene snapshot
- Open-Meteo weather fetcher using Seoul as the default location
- Optional last-known device location fetch
- Scene state layer for weather type, rain/snow/cloud intensity, sunrise and sunset
- Canvas renderer for sky gradient, sun/moon arc, clouds, rain, snow, fog, thunder and a minimal window-frame composition

## Build

```bash
# In Codespaces/local machine with Android SDK + Gradle available
gradle wrapper
./gradlew assembleDebug
```

Debug APK path:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## First test flow

1. Install the debug APK on an Android device.
2. Open WeatherWindow.
3. Tap **Fetch Seoul weather** or **Use device location**.
4. Tap **Open live wallpaper picker** and set the wallpaper.
5. Add the WeatherWindow widget from the Android home screen.

## Status

This is a technical prototype, not a polished product build. The first visual layer is mostly Canvas-based so the core wallpaper/widget behavior can be tested before producing PNG assets.
