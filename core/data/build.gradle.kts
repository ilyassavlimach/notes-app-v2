// Repositories: the single source of truth for features (ARCH-04). Offline-first: Room is the source of truth.
plugins {
    id("app.android.library")
    id("app.hilt")
    id("app.kover")
}

dependencies {
    api(projects.core.common)
    api(projects.core.model)
    implementation(projects.core.database)
    implementation(projects.core.network)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(projects.core.testing)
    testImplementation(libs.kotlinx.serialization.json)
}
