# Arabic TV Keyboard

لوحة إدخال عربية لأجهزة Android TV Box والريموتات المزودة بلوحة إنجليزية.

Native Kotlin, Android 5.0+ (API 21), no Google services, no network permission, no ads or analytics. Supports ARM/ARM64/x86 without native libraries. No Accessibility service or draw-over-other-apps permission.

## Installation / التثبيت

1. Download `ArabicTVKeyboard-signed-…` from the latest successful **Actions → Android APK** run. Extract and install `ArabicTVKeyboard.apk` on the TV box. Future updates install over this same package.
2. Open **Arabic TV Keyboard** from the TV launcher.
3. Select **تفعيل لوحة الإدخال** and enable this keyboard in Android's input-method settings.
4. Select **اختيار اللوحة** and choose Arabic TV Keyboard.
5. Open a search/text box. The translucent keyboard guide appears in the IME area. Use the remote's English letter keys. Press **OK** to enter the guide, navigate its keys with the D-pad, then press **OK** to type the focused key. **Esc** hides the guide and returns arrows to the editor.
6. If your box hides keyboard settings, use ADB from your own computer as an optional fallback:

```sh
adb shell ime enable com.herannet.arabictvkeyboard/.ArabicImeService
adb shell ime set com.herannet.arabictvkeyboard/.ArabicImeService
```

The service maps letters only while a text editor has focus. Back, arrows, volume, Home, Tab and Ctrl/Meta shortcuts remain available. Some TV apps use a private search UI and do not accept third-party Android IMEs. OEM behavior must be verified on the actual box.

## Layout / التوزيع

The nearest sounds keep their English positions. Assignments for remaining letters are mnemonic choices, not standard transliteration.

| Key | Arabic | ALT / F2 |
| --- | --- | --- |
| Q | ق | ء |
| W | و | ؤ |
| E | ع | إ |
| R | ر | — |
| T | ت | ط |
| Y | ي | ى |
| U | ث | — |
| I | ذ | ئ |
| O | ش | آ |
| P | ح | — |
| A | ا | أ |
| S | س | ْ |
| D | د | ُ |
| F | ف | َ |
| G | غ | — |
| H | ه | — |
| J | ج | ّ |
| K | ك | ِ |
| L | ل | ٍ |
| Z | ز | ظ |
| X | خ | ـ |
| C | ص | — |
| V | ض | — |
| B | ب | ة |
| N | ن | ً |
| M | م | ٌ |

- **CAPS** or the on-screen **العربية / EN** switches language.
- **ALT** or **F2** arms the secondary layer for the next key. The on-screen ALT button provides a fallback on remotes without an independent modifier event.
- **F3 / 123** toggles number/symbol mode. Number, phone and date input fields always bypass Arabic translation.
- **Esc / إخفاء** hides the guide for the current editor session; physical typing continues. The guide returns in the next input session.
- The guide opacity and visibility can be changed in the setup screen. The default translucent guide overlays the active editor without reserving screen space. Turn off the overlay option to use a docked IME area if it covers a text box on your device. This stays inside the IME window and never floats above unrelated apps.

### Blue symbols on Chinese mini remotes

Some firmware consumes ALT internally and sends a blue symbol instead (for example `ALT+D` becomes `#`, `ALT+Q` becomes `1`). Android cannot distinguish such a `1` from an ordinary `1` if the firmware sends identical events.

Use **اختبار الريموت** first. If Android receives ALT as a modifier, leave **تحويل الرموز الزرقاء** off. If it only receives the printed blue symbols, enable that option. The preset follows the supplied QWERTY remote photo. In that mode ordinary digits may produce secondary Arabic letters in text fields; use **F3 / 123**, English mode, or numeric input fields for numbers. No automatic guessing hides this ambiguity.

For a different remote, select **تعيين زر إلى حرف عربي**, choose the output letter, then press the remote key/combination. These overrides stay locally on the device. They do not affect English or number mode. Reset them from the same screen. Key diagnostics are displayed only on this explicit test screen and are not logged or uploaded.

## Stable signing / توقيع التحديثات

- Permanent application ID: `com.herannet.arabictvkeyboard`.
- Release builds require the **same permanent RSA signing key**. Missing signing secrets fail the build; CI never generates a replacement and never substitutes a debug key.
- One repository Actions secret named **ANDROID_SIGNING_BUNDLE** contains JSON with `keystore` (base64 PKCS12), `password`, and `alias`. It is never committed.
- `signing-certificate.sha256` pins the public certificate fingerprint. CI verifies both APK signing schemes and this fingerprint before uploading the APK.
- Every workflow run uses an increasing `github.run_number` as `versionCode`. Re-running a workflow retains its version code. Keep this workflow file and counter; if migrating/resetting it, start above the last installed version code.
- Debug builds use a separate `.debug` application ID so they cannot replace the release installation.
- Keep the provided signing backup outside GitHub. If secrets are lost, restore the original bundle rather than generating a new key. A new key prevents normal updates to existing installations.

The private key is restored only on pushes/manual runs in this repository, not on pull requests. Workflow token permissions are read-only. The keystore is removed at job completion; neither it nor the bundle is uploaded as a CI artifact.

## Build / البناء

JDK 17, Android SDK 35, build-tools 35.0.0. Gradle 8.11.1 is pinned with its distribution SHA-256. No AndroidX or Compose runtime.

```sh
./gradlew testDebugUnitTest lintDebug assembleDebug
```

GitHub Actions performs tests, lint, a debug build, then a signed release build and certificate verification. APKs are in the signed artifact for each successful run (90-day retention). GitHub may require a plan/minutes allowance for private-repository runners.

For a local signed build, restore the original bundle using `scripts/restore_signing.py` in a protected environment, or provide `ANDROID_KEYSTORE_PATH`, `ANDROID_STORE_PASSWORD`, `ANDROID_KEY_PASSWORD`, `ANDROID_KEY_ALIAS`, `APP_VERSION_CODE` and `APP_VERSION_NAME`, then run `./gradlew assembleRelease`. Never commit those values.

## References

- [Android custom input methods](https://developer.android.com/develop/ui/views/touch-and-input/creating-input-method)
- [InputMethodService](https://developer.android.com/reference/android/inputmethodservice/InputMethodService)
- [App signing and update identity](https://developer.android.com/studio/publish/app-signing)
- [GitHub Actions secrets](https://docs.github.com/en/actions/security-for-github-actions/security-guides/using-secrets-in-github-actions)
