// PUSH-01..03: channels, the FCM service and the POST_NOTIFICATIONS request. Strings come from :core:i18n.
plugins {
    id("app.android.library")
    id("app.android.compose")
    id("app.hilt")
}

dependencies {
    implementation(projects.core.i18n)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.androidx.startup)
    implementation(libs.androidx.activity.compose)

    testImplementation(projects.core.testing)
}
