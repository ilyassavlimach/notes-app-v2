plugins {
    id("app.android.library")
    id("app.hilt")
    id("app.room")
}

dependencies {
    implementation(projects.core.model)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.robolectric)
}
