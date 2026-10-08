plugins {
    id("app.android.library")
}

dependencies {
    api(projects.core.analytics)
    api(projects.core.data)
    api(libs.junit4)
    api(libs.androidx.test.ext.junit)
    api(libs.kotlinx.coroutines.test)
    api(libs.kotlin.test)
}
