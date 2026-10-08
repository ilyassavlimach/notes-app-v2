plugins {
    id("app.android.library")
    id("app.hilt")
    alias(libs.plugins.kotlin.serialization)
}

android {
    buildFeatures {
        // Only DEBUG is read here; API values come from :app per environment flavor via ApiConfig (ENV-01).
        buildConfig = true
    }
}

dependencies {
    api(projects.core.common)
    api(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(platform(libs.okhttp.bom))
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)

    // OBS-02: Chucker records traffic in debug builds only; the no-op artifact keeps release builds clean.
    debugImplementation(libs.chucker)
    releaseImplementation(libs.chucker.noop)

    testImplementation(libs.kotlinx.coroutines.test)
}
