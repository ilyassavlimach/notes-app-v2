// RC-01: remote config with in-app defaults, and the force-update / maintenance gate it drives.
plugins {
    id("app.android.library")
    id("app.android.compose")
    id("app.hilt")
    id("app.kover")
}

dependencies {
    api(projects.core.common)
    implementation(projects.core.designsystem)
    implementation(projects.core.i18n)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.config)
    implementation(libs.kotlinx.coroutines.play.services)

    testImplementation(projects.core.testing)
}
