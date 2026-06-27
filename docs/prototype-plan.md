# WeatherWindow Prototype Plan

## Product direction

> Your phone wallpaper quietly follows the real sky.

The first version should focus on a small, beautiful, battery-conscious Android live wallpaper and widget.

## MVP scope

### Live wallpaper

- Time-based sky gradient
- Sun position from sunrise/sunset
- Moon/stars at night
- Cloud cover visualization
- Rain particles
- Snow particles
- Fog overlay
- Minimal thunder flash
- Minimal window-frame composition

### Widget

- Static scene snapshot rendered to a bitmap
- City, temperature and weather label
- 30-minute refresh cadence

### Weather data

- Current weather from Open-Meteo
- Default city: Seoul
- Optional device last-known location
- Store latest scene in SharedPreferences

## Asset strategy

This prototype deliberately avoids heavy PNG assets. Later, replace the drawn frame/clouds with a real asset pack.

```text
assets/
  foreground/window_frame.png
  foreground/glass_reflection.png
  clouds/cloud_soft_01.png
  clouds/cloud_soft_02.png
  clouds/cloud_dark_01.png
  night/moon.png
  night/stars.png
  effects/fog_overlay.png
  effects/glass_raindrops.png
```

## Next technical steps

1. Generate/check Gradle wrapper.
2. Build with `./gradlew assembleDebug`.
3. Install APK on a real Android phone.
4. Confirm live wallpaper picker works.
5. Confirm the home widget renders a scene image.
6. Add WorkManager for periodic background weather refresh.
7. Replace Canvas window frame with PNG foreground assets.
8. Add saved cities and theme packs.
9. Add battery mode: 15fps / 24fps / 30fps.
