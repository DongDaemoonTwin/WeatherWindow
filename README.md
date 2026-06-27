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

## Browser preview in Codespaces

Codespaces cannot realistically run the Android live wallpaper or home screen widget like a real phone. Use the browser preview to inspect the visual direction instead:

```bash
python3 -m http.server 5173 -d preview
```

Then open the forwarded port `5173` from the Codespaces **Ports** tab.

This preview is not the APK. It is a lightweight browser version of the wallpaper scene for visual checking.

## Codespaces setup

If you see this error:

```text
SDK location not found. Define a valid SDK location with an ANDROID_HOME environment variable or by setting the sdk.dir path in your project's local properties file.
```

run:

```bash
bash scripts/setup-android-sdk.sh
gradle wrapper --gradle-version 9.3.1
./gradlew clean assembleDebug
```

The setup script installs Android command line tools, platform tools, Android 35, build-tools 35.0.0, accepts SDK licenses, and writes `local.properties` locally.

New Codespaces should also run this automatically through `.devcontainer/devcontainer.json`. If you already had a Codespace open before this file was added, rebuild the container or run the script manually.

## Build

```bash
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
