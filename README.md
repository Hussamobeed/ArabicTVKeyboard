# Arabic TV Keyboard

لوحة عربية لأجهزة Android TV Box والريموتات المزودة بلوحة مفاتيح إنجليزية عبر USB.

An Arabic input method for Android TV boxes and USB air-mouse remotes with English keyboards.

[![Android APK](https://github.com/Hussamobeed/ArabicTVKeyboard/actions/workflows/android.yml/badge.svg)](https://github.com/Hussamobeed/ArabicTVKeyboard/actions/workflows/android.yml)

[العربية](#arabic) · [English](#english) · [توزيع الحروف / Key map](#keymap) · [للمطورين / Developers](#developers)

<a id="arabic"></a>
## العربية

### ما وظيفة التطبيق؟

اكتب بالعربية باستخدام لوحة الريموت الإنجليزية، مثل `D = د` و`K = ك`. يظهر دليل شفاف على الشاشة لعرض الحروف ومكانها، وتوجد طبقة إضافية للحروف مثل `أ، إ، آ، ء، ئ، ؤ، ى، ة`.

- يدعم Android 5.0 فأحدث، دون الحاجة إلى خدمات Google أو صلاحيات الروت.
- واجهة تعمل بالريموت والأسهم، مع إمكانية الضغط على الأزرار من الشاشة.
- دليل حروف يمكن تغيير حجمه من 45% إلى 100%، وحجمه الافتراضي 65%، مع ضبط العتامة أو إخفائه.
- تبديل العربية والإنجليزية، ووضع للأرقام والرموز.
- معالجة رموز المسح `scanCode` عندما يرسل الجهاز `KEYCODE_UNKNOWN` بدل الحروف.
- تعيين الأزرار يدويًا للريموتات المختلفة، مع حفظ التعيينات على الجهاز.
- دون إنترنت أو إعلانات أو تحليلات. لا يحفظ النصوص المكتوبة أو يرسلها.

التوزيع مقترح لتسهيل التذكر، وليس توزيع لوحة المفاتيح العربية القياسي. لا يوجد مقابل صوتي إنجليزي مباشر لكل حرف عربي.

### التنزيل والتثبيت

1. افتح [GitHub Actions](https://github.com/Hussamobeed/ArabicTVKeyboard/actions/workflows/android.yml)، ثم أحدث تشغيل ناجح بعلامة خضراء.
2. من **Artifacts** نزّل `ArabicTVKeyboard-signed-…`، وفك الملف المضغوط. يتطلب تنزيل المرفقات تسجيل الدخول إلى GitHub.
3. انقل `ArabicTVKeyboard.apk` إلى جهاز التلفزيون وثبّته.
4. افتح **Arabic TV Keyboard** من قائمة التطبيقات.
5. اضغط **تفعيل لوحة الإدخال** وفعّل التطبيق من إعدادات Android.
6. اضغط **اختيار اللوحة** وحدد **Arabic TV Keyboard**. يجب أن تظهر عبارة «اللوحة محددة الآن».
7. اختبر الحروف في خانة **جرّب الكتابة هنا** قبل الانتقال إلى التطبيقات الأخرى.

للتحديث، ثبّت النسخة الموقّعة فوق النسخة السابقة دون حذفها للحفاظ على الإعدادات والتعيينات. نسخ `debug` مخصصة للتطوير ولها حزمة منفصلة.

### الاستخدام

| الزر | الوظيفة |
| --- | --- |
| حروف الريموت | الحروف العربية الموضحة في [جدول التوزيع](#keymap) |
| `CAPS` أو **العربية / EN** | تبديل اللغة |
| `ALT` أو `F2` أو زر ALT على الشاشة | تفعيل طبقة الحروف الإضافية للضغطة التالية |
| `F3` أو زر **123** | تبديل وضع الأرقام والرموز |
| `OK` | الدخول إلى دليل الشاشة، ثم اختيار الزر المحدد |
| الأسهم داخل الدليل | التنقل بين أزراره |
| `Esc` أو **إخفاء** | إخفاء الدليل مع استمرار الكتابة من الريموت |

تظهر حروف الطبقة الإضافية بالسماوي. عند تفعيلها يتغير لون زر ALT وإطار اللوحة وتظهر حالة التفعيل في الرأس.

### اختصارات جاهزة للريموت المدعوم

هذه الاختصارات تخص الجهاز الذي يظهر باسم `2.4G Composite Devic Wireless Devic Mouse`، وتعمل أثناء وجود المؤشر داخل صندوق كتابة. تستخدم أرقام `scanCode`، وليست أسماء الأزرار المطبوعة.

| إشارة الزر | الوظيفة |
| --- | --- |
| `keyCode=0` و`scanCode=165` | تثبيت ALT حتى تضغط الزر نفسه مرة أخرى. الكتابة والمسافة وتغيير صندوق الكتابة لا تلغي التثبيت. |
| `keyCode=0` و`scanCode=163` | تفعيل وضع الأرقام، ثم العودة للحروف عند الضغط مرة أخرى. |

- تظهر عبارة **ALT مثبت** عند تثبيت الطبقة. لا يلغي زر ALT على الشاشة هذا التثبيت؛ استخدم الزر `165` نفسه.
- عند تشغيل الأرقام مع ALT المثبت، يأخذ وضع الأرقام الأولوية مع الاحتفاظ بتثبيت ALT للعودة إليه لاحقًا.
- في وضع الأرقام، أزرار `Q W E R T Y U I O P` تكتب `1 2 3 4 5 6 7 8 9 0`، و`M` تكتب `:`.
- زر النقطة يكتب `.` حتى لو ظل SHIFT مفعّلًا من الريموت. مثال إدخال: `192.168.1.1:8080`.
- بقية الحروف في وضع الأرقام تطابق الرموز الزرقاء على الريموت. الأرقام الموجودة في الجهة الأخرى تظل أرقامًا.
- الضغط المطوّل لا يكرر تبديل الوضع. حالتا التثبيت والأرقام محفوظتان محليًا.

لا تُطبق هذه الاختصارات على أحداث الوسائط المعروفة في Android أو على الريموتات ذات الأسماء الأخرى.

### إذا كان ALT يظهر باسم SHIFT

بعض ريموتات USB ترسل SHIFT أو رموزًا مثل `#` عند ضغط ALT. خيار **توافق الريموت: اعتبار SHIFT مثل ALT في العربية** مفعّل افتراضيًا لهذا الاستخدام. ألغِه عند استخدام لوحة عادية إذا أردت SHIFT بسلوكه المعتاد.

إذا كان الريموت يرسل الرموز الزرقاء مباشرة، افتح **اختبار الريموت وتعيين الأزرار**، ثم جرّب خيار **تحويل الرموز الزرقاء** عند الحاجة. هذا الخيار قد يحول أرقامًا عادية إلى حروف إضافية داخل النص العربي؛ استخدم وضع الأرقام لإدخالها كأرقام.

إذا لم يرسل الريموت أي حدث عند تغيير وضع ALT داخله، فلن يستطيع التطبيق معرفة التغيير قبل وصول ضغطة أخرى. زر ALT على الشاشة يوفر حالة يمكن التحكم بها داخل التطبيق.

### التعيين اليدوي وحل المشكلات

1. تأكد أن **Arabic TV Keyboard** هي لوحة الإدخال المحددة.
2. افتح **اختبار الريموت وتعيين الأزرار** واضغط الزر المطلوب.
3. اختر **تعيين زر إلى حرف عربي**، وحدد الحرف، ثم اضغط الزر أو التركيبة التي تريدها.
4. انتظر عبارة **حُفظ التعيين**. يقبل التطبيق رموز المسح حتى عند وجود `keyCode=0` و`unicode=0`.

تُحفظ التعيينات حسب جهاز USB ورمز المسح والمفاتيح المعدّلة، ولا تُحذف عند تحديث التطبيق. يمكن مسحها من شاشة الاختبار. في وضع الأرقام لا تُستخدم التعيينات العربية.

إذا غطّى الدليل صندوق الكتابة، صغّر حجمه أو ألغِ خيار **دليل شفاف فوق التطبيق** لعرضه كلوحة مثبتة أسفل الشاشة. بعض تطبيقات التلفزيون تستخدم لوحة خاصة ولا تمرر الإدخال إلى لوحات Android الخارجية؛ لذلك لا يُضمن التوافق مع كل تطبيق أو جهاز.

<a id="english"></a>
## English

### What does it do?

Type Arabic with the English keyboard on a USB remote, using mnemonic positions such as `D = د` and `K = ك`. A translucent on-screen guide shows the layout. A secondary layer provides letters such as `أ، إ، آ، ء، ئ، ؤ، ى، ة`.

- Android 5.0+; no Google services or root required.
- Remote/D-pad navigation and clickable on-screen keys.
- Adjustable guide size, opacity and visibility; default size 65%, adjustable from 45% to 100%.
- Arabic/English switching and a numeric/symbol mode.
- Scan-code recovery for boxes reporting `KEYCODE_UNKNOWN` instead of letters.
- Device-local manual key assignments for other remotes.
- No Internet permission, ads or analytics. Typed text is not stored or transmitted.

This is a proposed mnemonic layout, not the standard Arabic PC layout. Some Arabic sounds have no direct English-letter equivalent.

### Download and install

1. Open [GitHub Actions](https://github.com/Hussamobeed/ArabicTVKeyboard/actions/workflows/android.yml) and select the latest successful run with a green check.
2. Under **Artifacts**, download `ArabicTVKeyboard-signed-…` and extract it. GitHub requires sign-in to download workflow artifacts.
3. Transfer `ArabicTVKeyboard.apk` to the TV box and install it.
4. Open **Arabic TV Keyboard** from the launcher.
5. Choose **تفعيل لوحة الإدخال** (enable keyboard), then enable the input method in Android settings.
6. Choose **اختيار اللوحة** (select keyboard) and select **Arabic TV Keyboard**. The setup screen should report that it is selected.
7. Try the **جرّب الكتابة هنا** text box before testing other apps.

Install signed updates over the existing release to preserve settings and assignments. Debug builds use a separate package and do not replace release installations.

### Controls

| Key | Action |
| --- | --- |
| Remote letter keys | Arabic letters in the [key map](#keymap) |
| `CAPS` or **العربية / EN** | Switch language |
| `ALT`, `F2` or on-screen ALT | Arm the secondary layer for the next key |
| `F3` or **123** | Toggle numeric/symbol mode |
| `OK` | Enter the guide, then activate the focused key |
| D-pad inside the guide | Move between keys |
| `Esc` or **إخفاء** | Hide the guide; physical typing continues |

Secondary legends are cyan. The ALT key, panel border and header show when the secondary layer is active.

### Ready presets for the supported remote

These presets apply to the device reported as `2.4G Composite Devic Wireless Devic Mouse`, only while an editor has focus. The numbers below are scan codes, not printed key names.

| Reported event | Action |
| --- | --- |
| `keyCode=0`, `scanCode=165` | Toggle a persistent ALT lock. Typing, spaces and editor changes do not cancel it. Press the same key again to unlock. |
| `keyCode=0`, `scanCode=163` | Toggle numeric mode; press it again to return to letters. |

- **ALT مثبت** indicates the lock. The on-screen ALT button does not clear this dedicated lock; use the `165` shortcut.
- Numeric mode takes priority while preserving any ALT lock for when numeric mode is turned off.
- In numeric mode, `Q W E R T Y U I O P` produce `1 2 3 4 5 6 7 8 9 0`, and `M` produces `:`.
- The period key produces `.` even if the remote leaves SHIFT set. Example: `192.168.1.1:8080`.
- Other letters follow the remote's blue symbol legends. Actual digit keys continue to produce digits.
- Holding a shortcut does not repeatedly toggle it. Both mode states are saved locally.

Known Android media key events and events from differently named devices are not overridden by these presets.

### ALT reported as SHIFT

Some USB air-mouse keyboards synthesize SHIFT or blue symbols such as `#` instead of sending an ALT modifier. **توافق الريموت: اعتبار SHIFT مثل ALT في العربية** is enabled by default to handle this. Disable it for a conventional keyboard if you want ordinary SHIFT behavior in Arabic mode. English mode retains regular SHIFT behavior.

Use **اختبار الريموت وتعيين الأزرار** (remote test/key assignment) to inspect events. If the firmware sends blue symbols directly, enable **تحويل الرموز الزرقاء** when needed. That option can turn ordinary digits into secondary letters in Arabic text; use numeric mode for literal numbers.

An internal firmware ALT toggle that sends no event cannot be detected before another key arrives. On-screen ALT provides a directly controlled alternative.

### Manual mapping and troubleshooting

1. Confirm that **Arabic TV Keyboard** is the selected input method.
2. Open the remote test screen and press the key.
3. Choose **تعيين زر إلى حرف عربي**, select an output, then press the desired key or combination.
4. Look for **حُفظ التعيين** (assignment saved). Nonzero scan codes are accepted even when `keyCode` and `unicode` are zero.

Assignments are scoped to the USB device, scan code and modifiers, and survive app updates. Reset them from the test screen. Arabic assignments are bypassed in numeric mode.

If the guide covers the editor, reduce its size or disable **دليل شفاف فوق التطبيق** to use a docked IME area. Some TV apps implement their own keyboard and do not route input through third-party Android IMEs. Compatibility with every app or OEM box is not guaranteed.

<a id="keymap"></a>
## Keyboard layout / توزيع الحروف

الجدول يوضح الحروف الأصلية والطبقة الإضافية. الشرطـة تعني عدم وجود حرف إضافي منفصل.

Primary and secondary Arabic letters. A dash means there is no separate secondary letter.

| English key | العربية / Arabic | ALT |
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

<a id="developers"></a>
## Developers / للمطورين

### Build / البناء

Kotlin and native Android platform views. JDK 17, Android SDK 35, Build Tools 35.0.0 and Gradle 8.11.1. No AndroidX/Compose runtime or native ABI-specific libraries.

المشروع بكوتلن وواجهات Android الأصلية. ثبّت JDK 17 وAndroid SDK 35 وBuild Tools 35.0.0، ثم استخدم Gradle Wrapper المرفق.

```sh
git clone https://github.com/Hussamobeed/ArabicTVKeyboard.git
cd ArabicTVKeyboard
./gradlew testDebugUnitTest lintDebug assembleDebug
```

The Gradle distribution is checksum-pinned. CI runs unit tests, lint, a debug build, then a signed release build and certificate verification. Artifacts are retained for 90 days.

يتحقق البناء الآلي من الاختبارات والتوافق، ثم يبني APK ويفحص شهادته. تبقى مرفقات Actions متاحة لمدة 90 يومًا.

### Update identity / توقيع التحديثات

- Release package: `com.herannet.arabictvkeyboard`. Debug package has a `.debug` suffix.
- The maintainer's permanent release key is held in the Actions secret `ANDROID_SIGNING_BUNDLE`, not in the repository. `signing-certificate.sha256` contains only the public certificate fingerprint.
- Missing signing secrets stop the release build. CI never generates a replacement key or falls back to a debug key.
- `github.run_number` supplies an increasing release `versionCode`. Preserve the workflow counter; a migrated workflow must start above the last installed code.
- Pull-request builds do not receive the release signing key. Signing files are removed after the job and are not uploaded as artifacts.
- Forks can build debug APKs. To distribute releases from a fork, configure your own signing identity and update the pinned public certificate fingerprint. A fork's new key cannot update installations signed by this project's key.

معرّف التطبيق ومفتاح التوقيع ثابتان لتمكين التحديث فوق النسخة المثبتة. المفتاح الخاص ليس ضمن المستودع، وملف البصمة المنشور يحتوي بصمة الشهادة العامة فقط. يتوقف بناء الإصدار الموقّع عند غياب السر، ولا يستبدل المفتاح تلقائيًا. عند إنشاء نسخة مستقلة من المشروع، استخدم مفتاحك وبصمته؛ لن تتمكن من تحديث تثبيت موقّع بمفتاح المشروع الأصلي.

For a local release, provide `ANDROID_KEYSTORE_PATH`, `ANDROID_STORE_PASSWORD`, `ANDROID_KEY_PASSWORD`, `ANDROID_KEY_ALIAS`, `APP_VERSION_CODE` and `APP_VERSION_NAME`, then run `./gradlew assembleRelease`. Do not commit their values or signing backups.

لبناء إصدار موقّع محليًا، اضبط المتغيرات السابقة ثم نفّذ أمر الإصدار. احتفظ بنسخة المفتاح الاحتياطية خارج GitHub ولا تضف كلمات المرور أو ملفات التوقيع إلى Git.

### Optional ADB setup / تفعيل اختياري عبر ADB

If the TV hides input-method settings, you can enable/select the installed IME from your own computer. إذا أخفى جهازك إعدادات لوحات الإدخال، يمكن تفعيل التطبيق المثبت واختياره عبر ADB من جهازك:

```sh
adb shell ime enable com.herannet.arabictvkeyboard/.ArabicImeService
adb shell ime set com.herannet.arabictvkeyboard/.ArabicImeService
```

### Implementation notes / ملاحظات التطوير

The service runs only for an active input connection. Scan-code fallback follows AOSP's generic USB layout. The two owner-requested UNKNOWN shortcuts are limited to the named remote; Home, Back, volume and Ctrl/Meta shortcuts remain with Android/the editor. The guide stays inside the IME window and requests neither Accessibility access nor permission to draw over arbitrary apps.

تعمل معالجة الأزرار داخل صندوق كتابة نشط فقط. يعتمد تعويض رموز المسح على توزيع USB في AOSP. الاختصاران الخاصان بالريموت مقيدان باسمه، ولا يستبدلان أزرار النظام المعروفة. الدليل جزء من نافذة لوحة الإدخال، ولا يحتاج خدمة إمكانية وصول أو صلاحية الظهور فوق التطبيقات.

## References / المصادر

- [Android input methods](https://developer.android.com/develop/ui/views/touch-and-input/creating-input-method)
- [AOSP Generic.kl](https://android.googlesource.com/platform/frameworks/base/+/master/data/keyboards/Generic.kl)
- [Android app signing](https://developer.android.com/studio/publish/app-signing)
- [Downloading workflow artifacts](https://docs.github.com/en/actions/managing-workflow-runs-and-deployments/managing-workflow-runs/downloading-workflow-artifacts)
- [GitHub Actions secrets](https://docs.github.com/en/actions/security-for-github-actions/security-guides/using-secrets-in-github-actions)
