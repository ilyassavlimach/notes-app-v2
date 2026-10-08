plugins {
    id("app.android.application")
    id("app.hilt")
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

/** Reads `api.<flavor>.<key>` from gradle.properties / ~/.gradle / -P (ENV-01, SEC-01). */
fun apiProperty(
    flavor: String,
    key: String,
): String = providers.gradleProperty("api.$flavor.$key").getOrElse("")

/** LINK-01: the App Links host per environment, for the manifest intent filter and the deep-link parser. */
fun com.android.build.api.dsl.ApplicationProductFlavor.deepLinkHost(host: String) {
    manifestPlaceholders["deepLinkHost"] = host
    buildConfigField("String", "DEEP_LINK_HOST", "\"$host\"")
}

android {
    defaultConfig {
        applicationId = "com.machinarium.notesv2"
        versionCode = providers.gradleProperty("app.versionCode").getOrElse("1").toInt()
        versionName = providers.gradleProperty("app.versionName").getOrElse("1.0.0")
    }

    buildFeatures {
        resValues = true // the stage flavor renames the app with resValue
    }

    // ENV-01: one flavor per environment. Everything that differs per environment lives here, never in if-checks.
    // Single-environment app (intake answer): delete this block and keep only the prod values in defaultConfig.
    flavorDimensions += "environment"
    productFlavors {
        create("stage") {
            dimension = "environment"
            applicationIdSuffix = ".stage"
            versionNameSuffix = "-stage"
            resValue("string", "common_app_name", "Notes v2 Stage")
            deepLinkHost("stage.notes.example.com")
            buildConfigField("String", "API_BASE_URL", "\"${apiProperty("stage", "baseUrl")}\"")
            buildConfigField("String", "API_CERT_PINS", "\"${apiProperty("stage", "certPins")}\"")
        }
        create("prod") {
            dimension = "environment"
            deepLinkHost("notes.example.com")
            buildConfigField("String", "API_BASE_URL", "\"${apiProperty("prod", "baseUrl")}\"")
            buildConfigField("String", "API_CERT_PINS", "\"${apiProperty("prod", "certPins")}\"")
        }
    }

    // HARD-07: signing secrets only from the environment (CI secrets / local shell), never from files in git.
    signingConfigs {
        create("release") {
            val keystorePath = providers.environmentVariable("KEYSTORE_PATH").orNull
            if (keystorePath != null) {
                storeFile = file(keystorePath)
                storePassword = providers.environmentVariable("KEYSTORE_PASSWORD").orNull
                keyAlias = providers.environmentVariable("KEY_ALIAS").orNull
                keyPassword = providers.environmentVariable("KEY_PASSWORD").orNull
            }
        }
    }

    buildTypes {
        release {
            // HARD-03: shrink + obfuscate with project keep rules.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release").takeIf { it.storeFile != null }
            // R8 mapping upload needs the real Firebase project; turn it on with -Pcrashlytics.uploadMapping=true.
            configure<com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension> {
                mappingFileUploadEnabled =
                    providers.gradleProperty("crashlytics.uploadMapping").map(String::toBoolean).getOrElse(false)
            }
        }
    }
}

dependencies {
    // Only what :app itself uses; features bring their own dependencies (DEAD-03).
    implementation(projects.core.common)
    implementation(projects.core.designsystem)
    implementation(projects.core.i18n)
    implementation(projects.core.config)
    implementation(projects.core.notifications) // merges the FCM service and channel initializer into the manifest
    implementation(projects.feature.notedetail)
    implementation(projects.feature.noteeditor)
    implementation(projects.feature.noteslist)

    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.timber)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)

    // OBS-04: leak detection in debug builds only.
    debugImplementation(libs.leakcanary)

    testImplementation(libs.junit4)
    testImplementation(projects.core.testing)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.robolectric)
}
