plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
val signingFile = providers.environmentVariable("ANDROID_KEYSTORE_PATH").orNull
android {
    namespace = "com.herannet.arabictvkeyboard"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.herannet.arabictvkeyboard"
        minSdk = 21
        targetSdk = 35
        versionCode = providers.environmentVariable("APP_VERSION_CODE").orElse("1").get().toInt()
        versionName = providers.environmentVariable("APP_VERSION_NAME").orElse("0.1.0").get()
    }
    signingConfigs {
        if (signingFile != null) create("stableRelease") {
            storeFile = file(signingFile)
            storePassword = System.getenv("ANDROID_STORE_PASSWORD")
            keyAlias = System.getenv("ANDROID_KEY_ALIAS")
            keyPassword = System.getenv("ANDROID_KEY_PASSWORD")
            enableV1Signing = true
            enableV2Signing = true
        }
    }
    buildTypes {
        getByName("debug") { applicationIdSuffix = ".debug"; versionNameSuffix = "-debug" }
        getByName("release") {
            isMinifyEnabled = false
            if (signingFile != null) signingConfig = signingConfigs.getByName("stableRelease")
        }
    }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
    lint { abortOnError = true }
}
tasks.register("requireReleaseSigning") {
    doLast { check(signingFile != null) { "Release signing is required. Configure ANDROID_SIGNING_BUNDLE; never fall back to a debug key." } }
}
tasks.matching { it.name == "preReleaseBuild" }.configureEach { dependsOn("requireReleaseSigning") }
dependencies { testImplementation("junit:junit:4.13.2") }
