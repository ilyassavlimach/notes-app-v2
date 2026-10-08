plugins {
    id("app.android.library")
    id("app.hilt")
    id("app.room")
}

dependencies {
    implementation(projects.core.model)
}
