// OBS-03/05: the only module that talks to analytics SDKs. Features depend on AnalyticsTracker, never on an SDK.
// Each chosen provider (templates/modules/analytics-<provider>) adds its dependency and keys below — see
// references/integrations.md → Analytics.
plugins {
    id("app.android.library")
    id("app.hilt")
}

android {
    buildFeatures {
        buildConfig = true
    }
    defaultConfig {
        // Keys come from ~/.gradle/gradle.properties or CI; a blank key keeps that provider off.
        fun key(name: String) = "\"${providers.gradleProperty(name).getOrElse("")}\""
        buildConfigField("String", "ADJUST_APP_TOKEN", key("analytics.adjust.appToken"))
        buildConfigField("String", "ADJUST_EVENT_TOKENS", key("analytics.adjust.eventTokens"))
        buildConfigField("String", "APPSFLYER_DEV_KEY", key("analytics.appsflyer.devKey"))
    }
}

dependencies {
    implementation(libs.androidx.startup)
    implementation(libs.timber)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.adjust.android)
    implementation(libs.android.installreferrer)
    implementation(libs.appsflyer)

    testImplementation(projects.core.testing)
}
