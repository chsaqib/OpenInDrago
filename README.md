# Open in Drago

A tiny **unofficial** Android helper for public Diskwala share links.

## What it does

1. In Telegram, share a message/link containing `https://...diskwala.com/...`.
2. Choose **Open in Drago** from Android's Share sheet.
3. The app opens `https://dragoplayer.online/` in a WebView.
4. It fills the Diskwala URL into the page and presses **Watch** automatically.

The helper does not modify Diskwala, block its anti-adblock checks, resolve media URLs itself, or store your links.

## Build on an Android phone with AndroidIDE

AndroidIDE can open existing Gradle Android projects and build/install a debug APK on-device.

1. Install AndroidIDE from one of its trusted sources (official site, GitHub Releases, or F-Droid).
2. In AndroidIDE's terminal, install its build tools if this is your first use (`idesetup -c`).
3. Extract this ZIP to a normal folder on your phone.
4. AndroidIDE → **Open existing project** → select the `OpenInDrago` folder.
5. Let Gradle sync.
6. Tap **Run / Quick Run** to build and install the debug APK.
7. In Telegram, use **Share** on a message containing a Diskwala link and choose **Open in Drago**.

## Notes

- Minimum Android version: Android 6 (API 23).
- The app needs only the `INTERNET` permission.
- It depends on the current HTML structure of DragoXStream. If Drago changes its input/button markup, the auto-fill selector may need updating.
- DragoXStream is a third-party service. This helper is not affiliated with DragoXStream or Diskwala.
- Only open files/links you are authorized to access.
