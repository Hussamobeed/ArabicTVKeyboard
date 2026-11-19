# Project Context — ArabicTVKeyboard

> **Purpose:** Persistent handoff context for future AI sessions. Read this file before changing the project, and update it after every meaningful decision, implementation change, test result, or user clarification.
>
> **Last updated:** 2026-11-19
> **Repository:** `https://github.com/Hussamobeed/ArabicTVKeyboard.git`
> **Local checkout:** `/home/ubuntu/ArabicTVKeyboard`
> **Current branch:** `main`
>
## User's current objective

Extend the existing Arabic TV Keyboard Android app with a new mode called **Gamepad** (user also wrote “Gamepade”). The user has a USB air-mouse/remote control that works with an Android TV box, but many games do not respond to its normal keyboard/remote keys. The desired result is for the TV box/game to recognize the same physical remote as a gamepad, allowing games such as PS1/emulator games, GTA, and other controller-based games to be played without buying a separate gamepad.

The user provided a photo of the remote at `/home/ubuntu/upload/pasted_file_F6Dclj_image.png` during the 2026-11-19 session. The original attachment may not persist across sessions, so preserve observations here rather than relying on the file.

## Remote identification from the provided photo

The photo shows a long black USB wireless air-mouse/keyboard-style Android TV remote. The exact manufacturer/model is not identifiable from the photo alone. The photo is rotated so the printed legends are upside down relative to the viewer; observations below describe visible controls rather than asserting scan codes.

Visible controls:

- Four colored keys near the top: blue, yellow, green, and red. Their exact Android key events are unknown and must be discovered on-device using the existing remote-test screen/scan-code diagnostics.
- A blue key with a mouse/air-pointer-looking icon and a red `TV` key.
- A numeric keypad and additional keys with white/blue symbols.
- Dedicated volume and page/channel-style rocker controls.
- A central directional pad with left/right/up/down arrows and a large blue `OK` button.
- Additional navigation/menu/back-like buttons below the D-pad.
- Media playback controls near the bottom: previous, next, rewind, fast-forward, and related media buttons.
- Red power button at the bottom.
- A small central slider/button area likely related to mouse/air-pointer or mode switching.
- The device is expected to connect through its USB receiver. The actual USB product/vendor ID, device name, HID usages, Linux input device nodes, Android key events, scan codes, and whether the receiver exposes mouse + keyboard only are not yet known.

The user later provided a second photo of the reverse side of the same device at `/home/ubuntu/upload/pasted_file_y5vcau_image.png`. This side contains a compact full QWERTY keyboard with blue secondary legends. Clearly visible keys include `Q W E R T Y U I O P`, `A S D F G H J K L`, and `Z X C V B N M`, plus blue alternate symbols/numbers, `SPACE`, `ALT`, `CAPS`, `ESC`, `ENTER`, `BACKSPACE`, arrow/navigation keys, and a `WWW`-style key. This confirms that the hardware is a combined air-mouse/remote and wireless mini-keyboard, not only a conventional TV remote. The keyboard legends reinforce that button assignments should be discovered from actual Android `KeyEvent`/scan-code data rather than inferred solely from printed labels.

## Existing project baseline

This is a native Kotlin Android project with platform views; it is an Android Input Method Editor (IME), not an Accessibility service.

- Minimum SDK: Android 5.0 / API 21.
- Compile/build setup documented in `README.md`: JDK 17, Android SDK 35, Build Tools 35.0.0, Gradle wrapper 8.11.1.
- Application ID: `com.herannet.arabictvkeyboard`; debug builds use the `.debug` suffix.
- No AndroidX/Compose runtime, Google services, Internet permission, ads, or analytics.
- Existing source package: `app/src/main/java/com/herannet/arabictvkeyboard/`.
- Important files:
  - `ArabicImeService.kt`: active IME service and key handling.
  - `RemoteKeys.kt`, `RemoteLayer.kt`, `RemoteModeShortcuts.kt`, `UsbScanCodes.kt`: remote event handling, modes, scan-code mappings, and shortcuts.
  - `KeyboardLayout.kt`: approved Arabic/QWERTY mappings.
  - `MainActivity.kt`, `RemoteTestActivity.kt`, `Ui.kt`: setup, diagnostics, and UI.
  - `Prefs.kt`: persisted local settings.
- Existing tests: `KeyboardLayoutTest.kt`, `RemoteLayerTest.kt`, `RemoteModeShortcutsTest.kt`, and `UsbScanCodesTest.kt`.
- Existing app behavior includes Arabic/English input, numeric/symbol mode, a secondary ALT layer, on-screen guide, manual remote mapping, and scan-code recovery.

## Non-negotiable project constraints

Follow `AGENTS.md` and the README:

- Keep minSdk 21 and native Android/platform views unless the user explicitly changes this requirement.
- Preserve the app identity and release signing behavior.
- Do not add Accessibility-service behavior or intercept Home, Back, volume, Ctrl/Meta, or arbitrary typing outside an active editor as a shortcut for the Gamepad feature.
- Existing hardware mapping is intentionally limited to an active input connection. Do not break the current Arabic IME behavior or the owner-specific scan-code shortcuts.
- Do not claim universal compatibility based only on compilation. Android TV OEM behavior, game/emulator behavior, and remote firmware need physical-device testing.
- Never commit keystores, signing bundles, passwords, or key backups.
- Required validation after implementation: `./gradlew testDebugUnitTest lintDebug assembleDebug`; signed CI also validates release signing separately.
- Update this `CONTEXT.md` after meaningful work.

## Key technical question to resolve before implementation

An Android IME normally injects text/key events only through the active editor/input connection. Games often require a real Android `InputDevice` with gamepad sources and axes/buttons, not IME text events. Therefore, first determine what is technically possible on an unrooted Android TV box:

1. Can this app expose a virtual gamepad/input device using public Android APIs at the target SDK/minSdk without root, privileged permissions, Accessibility, or a companion privileged service?
2. If not, what is the closest practical architecture? Candidates include:
   - A Gamepad mode that maps remote keys to keyboard keys accepted by a specific emulator/game.
   - A mode that emits directional/navigation events only where Android accepts them.
   - A companion PC/ADB/root/privileged bridge (only if clearly disclosed and user-approved).
   - A true USB/Bluetooth HID gadget approach requiring different hardware/firmware, not just this Android app.
3. Can the Android TV box/game receive `KEYCODE_BUTTON_*`, `SOURCE_GAMEPAD`, joystick axes, or only keyboard/mouse events from this remote receiver?
4. Which remote buttons can be safely remapped without breaking system controls or the existing IME?

Do not implement a fake claim that a normal third-party IME can become a system-wide hardware gamepad if Android's public input stack prevents it. Explain any platform limitation and offer the most usable alternative.

## Proposed Gamepad mode direction (separate switchable mode)

The user confirmed that Gamepad should be a separate switchable mode: enable it when playing, then switch back to the normal writing mode when typing. The first implementation follows a diagnostic-first design:

1. Add a Gamepad mode entry to the app, but keep it disabled or clearly marked experimental until the input architecture is confirmed.
2. Reuse the remote test screen to record each button's Android `KeyEvent` data, scan code, device name, vendor/product information where available, and event source/classification.
3. Let the user assign remote buttons to a logical layout: D-pad, A/B/X/Y, Start, Select, L1/R1, L2/R2, and optionally analog-stick directions. The photo's D-pad and colored buttons are natural candidates, but assignments must be configurable because actual events vary by receiver firmware.
4. Separate a “true Android gamepad” implementation from a “keyboard mapping/emulator compatibility” fallback. Label the fallback honestly.
5. Preserve system safety: do not hijack power, Home, Back, volume, or media keys globally; require an explicit active Gamepad session and provide an obvious exit path.
6. Add unit tests for mapping/state transitions and document the physical-device test procedure.

The initial code slice now adds `GamepadMapping.kt` with logical controls (`D-pad`, `A/B/X/Y`, `Start/Select`, and shoulder/trigger buttons), stable persisted bindings keyed by device descriptor/key code/scan code, and a protected list for Home, Back, Power, and Volume keys. `Prefs.kt` stores the Gamepad mode flag and bindings. `RemoteTestActivity.kt` exposes a separate experimental Gamepad switch, assignment flow, profile clearing, and event display. `MainActivity.kt` links to the experimental Gamepad setup. This profile currently records and identifies remote buttons; it intentionally does not claim to create a system-wide Android `InputDevice` or inject controls into games.

The next code slice adds three requested features. `ReleaseUpdateChecker.kt` checks the public GitHub `GET /repos/Hussamobeed/ArabicTVKeyboard/releases/latest` endpoint and opens the latest APK asset (or release page) in the browser; it does not silently install an APK. `INTERNET` is now declared because the user explicitly requested this feature. Gamepad defaults now exist for D-pad, `A/B/X/Y`, `Start/Select`, and `L1/R1/L2/R2`; they use a standard keyboard fallback (`Z/X/A/S`, Enter/Space, and `Q/W/E/R`) and can be overridden per remote button in the diagnostic screen. `ArabicImeService` translates assigned buttons to those fallback key events only when the separate Gamepad mode is active and an input connection exists. `GamepadAutoDetector.kt` optionally checks the foreground package through `UsageStatsManager`; the user must enable Usage Access and enter package names, because Android requires that special access. Auto-detection is best-effort and cannot make a normal IME receive events from games that do not expose an input connection.

## Open questions for the user/device test

- What Android TV box brand/model and Android version are being used?
- Is the remote receiver plugged into USB, and does the box identify it as a keyboard, mouse, composite device, or game controller?
- Which exact game/emulator is the first target?
- Does the existing app's “remote test/key assignment” screen receive events from the D-pad, colored buttons, and number keys? What `keyCode`, `scanCode`, device name, source, vendor ID, and product ID are reported?
- Is root access available? Is installing a separate companion app or using ADB acceptable?
- Is the goal strictly system-wide gamepad recognition, or is per-emulator keyboard mapping acceptable if true gamepad injection is impossible?

## Work log

### 2026-11-19 — Initial context setup

- Cloned `https://github.com/Hussamobeed/ArabicTVKeyboard.git` into `/home/ubuntu/ArabicTVKeyboard`.
- Verified checkout is on clean `main` tracking `origin/main`.
- Read `README.md`, `AGENTS.md`, and `settings.gradle.kts`.
- Inspected the attached remote photo in four overlapping original-resolution crops. The device is a black USB wireless air-mouse/keyboard remote with D-pad/OK, numeric keypad, colored buttons, media buttons, volume/page rockers, and power key.
- Inspected the second attached photo showing the reverse side. It confirms a compact QWERTY keyboard with blue secondary legends and dedicated `ALT`, `CAPS`, `ESC`, `SPACE`, `ENTER`, `BACKSPACE`, and arrow/navigation keys.
- No Gamepad implementation has been made yet.
- Temporary crop files were used only for inspection and should not be treated as source assets.

### 2026-11-19 — Initial Gamepad code slice

- Added the separate experimental Gamepad profile and persistent button-binding model.
- Added assignment UI in the existing remote-test screen and a main-screen entry point.
- Added JVM unit coverage for binding round-trip persistence and protected system keys.
- The requested validation command (`./gradlew testDebugUnitTest lintDebug assembleDebug`) was attempted, but the sandbox has no Android SDK configured: `ANDROID_HOME` and `ANDROID_SDK_ROOT` are empty and no common SDK path was found. The build stopped before compilation with `SDK location not found`; no test/lint/build result is available yet.
- The next implementation step is to validate on an Android-SDK-equipped environment, then use the user's TV box to capture real remote event metadata and decide whether a keyboard/emulator fallback or a privileged/companion architecture is required for true gamepad behavior.

### 2026-11-19 — Updates and automatic Gamepad activation

- Added GitHub Releases update checking and browser handoff for the latest APK asset.
- Added default editable Gamepad bindings and a visible mapping summary.
- Added optional Usage Access based foreground-game detection with a comma-separated package allowlist.
- Added separate-mode fallback key translation inside an active input connection; normal writing remains unchanged when Gamepad mode is off.
- Added `INTERNET` and `PACKAGE_USAGE_STATS` manifest declarations. The latter is a special access that the user must grant in Android Settings.
- `git diff --check` passes. Full Android compilation still requires the Android SDK and should be verified by the repository's GitHub Actions workflow.

### 2026-11-19 — GitHub Actions build-failure fix

- GitHub Actions run `44495736664` for commit `5c699d5` failed in the `Tests, lint and debug APK` step.
- Local reproduction with Android SDK 35 and JDK 17 found the initial Kotlin errors: unavailable `BuildConfig` references and assigning a `String` directly to `EditText.text`.
- After fixing those, lint reported `PACKAGE_USAGE_STATS` as a protected permission. The permission is intentionally needed for the documented Usage Access settings flow, so the manifest now has a narrowly scoped `tools:ignore="ProtectedPermissions"` on that permission.
- Final local validation passed: `./gradlew testDebugUnitTest lintDebug assembleDebug` with Android SDK 35 and JDK 17. Lint still reports existing/non-fatal warnings, but no errors.

### 2026-11-19 — Automatic game selection and real GitHub Releases

- Replaced manual game package typing in `MainActivity` with an installed-app multi-select picker. It discovers launchable apps, prioritizes Android apps categorized as games, and also recognizes common game/emulator names (`game`, `emulator`, `retro`, `playstation`, `ps1`, `psx`, `gta`, `arcade`). The selected package list is still stored internally for foreground detection, but the user never needs to type package names.
- Updated `.github/workflows/android.yml`: repository contents permission is now write-enabled, and pushes of tags matching `v*` publish `dist/ArabicTVKeyboard.apk` plus checksum/build files as a GitHub Release using `gh release create`.
- The in-app updater targets GitHub's latest published Release endpoint. To publish an update, push a version tag such as `v0.1.12`; a normal `main` push continues to produce validation and downloadable workflow artifacts but does not create a Release.
- The tag-release workflow is idempotent: if a Release was created in the GitHub browser first, the workflow uploads/replaces its APK assets; otherwise it creates the Release itself. This supports browser-only release creation.
- Local validation after this change passed: `./gradlew testDebugUnitTest lintDebug assembleDebug` with Android SDK 35 and JDK 17.
