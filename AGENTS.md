# Arabic TV Keyboard maintenance

- Native Kotlin and Android platform views. Keep minSdk 21 and avoid dependencies on Google services, AndroidX/Compose and Internet access unless explicitly requested.
- This is an Android IME, not an Accessibility service. Hardware mapping must run only for an active input connection. Do not intercept Home, Back, volume, Ctrl/Meta shortcuts or arbitrary typing outside editors.
- Owner-requested exception: while editing, UNKNOWN scanCode 165 on the named 2.4G Composite Wireless Mouse toggles a persistent ALT lock, and UNKNOWN 163 toggles numbers. Do not generalize these overrides to known media key codes, other devices, or outside editors. Ordinary typing and mode switches must never clear the 165 lock.
- Keep the approved QWERTY mappings in KeyboardLayout.kt. Cover the complete Arabic alphabet, ء ؤ ئ أ إ آ ى ة, and numeric-field bypass with tests.
- Preserve applicationId com.herannet.arabictvkeyboard. Debug builds retain the .debug suffix.
- NEVER regenerate the permanent release key or fall back to a debug key. ANDROID_SIGNING_BUNDLE is an Actions secret. The original signing backup belongs to the owner. signing-certificate.sha256 pins the certificate.
- Keep .github/workflows/android.yml and its increasing run counter. Any versionCode after workflow migration must exceed the last installed release.
- Never commit keystores, signing bundles, passwords or key backups. Never upload them as CI artifacts or print them in logs.
- Run ./gradlew testDebugUnitTest lintDebug assembleDebug. Signed CI must also run assembleRelease and scripts/verify_apk.py.
- Actual OEM box behavior, ALT firmware events and third-party search editors require device testing. Do not claim universal compatibility or physical-device testing from a successful compilation.
