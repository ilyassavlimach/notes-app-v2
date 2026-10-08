// Shared app components that need app knowledge (strings, AppError, models) — DS-04.
plugins {
    id("app.android.library")
    id("app.android.compose")
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.designsystem)
    implementation(projects.core.i18n)
    implementation(projects.core.model)
}
