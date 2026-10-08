// Repositories: the single source of truth for features (ARCH-04). Offline-first when the spec needs it.
plugins {
    id("app.android.library")
    id("app.hilt")
    id("app.kover")
}

dependencies {
    api(projects.core.common)
    api(projects.core.model)
    implementation(libs.kotlinx.coroutines.core)
    // Add implementation(projects.core.network / database / datastore) only for modules this app has (DEAD-03).

    testImplementation(projects.core.testing)
}
