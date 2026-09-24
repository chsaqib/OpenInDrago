# Open in Drago — V2

A tiny **unofficial** Android helper for Diskwala share links.

## V2 workflow

After a one-time Android setting is enabled:

**Telegram → tap Diskwala link → Open in Drago → DragoXStream loads the link and presses Watch automatically.**

The V1 Share-sheet workflow is still kept:

**Telegram → Share → Open in Drago**

## First-time setup on Android 12+

1. Open **Open in Drago** once.
2. Tap **Open link settings**.
3. Turn on **Open supported links**.
4. Enable/select `diskwala.com` and `www.diskwala.com` if Android shows them.
5. Return to Telegram and tap a Diskwala `/app/...` link normally.

Android 12+ requires either verified website ownership or explicit user approval before an unverified app can open normal HTTPS links. This helper opens the Android approval screen for you.

## Build

Push the project to GitHub. `.github/workflows/build-apk.yml` builds a debug APK automatically on `main`.

The artifact is named `OpenInDrago-v2-debug-apk`.

## Notes

- Version: 2.0 (`versionCode 2`).
- Minimum Android version: Android 6 (API 23).
- Only the `INTERNET` permission is requested.
- No VPN, DNS blocking, Accessibility Service, or Diskwala modification is used.
- DragoXStream and Diskwala are third-party services. This project is not affiliated with either service.
- Only open content you are authorized to access.
