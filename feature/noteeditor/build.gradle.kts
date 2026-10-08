// Feature module: Route + stateless Screen + ViewModel + type-safe navigation (see references/reference-feature.md).
// The convention plugin adds core modules, Compose, Hilt, navigation and test dependencies.
plugins {
    id("app.android.feature")
}

dependencies {
    implementation(projects.core.analytics)
    implementation(libs.androidx.activity.compose) // BackHandler for the unsaved-changes dialog
}
