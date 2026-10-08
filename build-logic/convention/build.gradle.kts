plugins {
    `kotlin-dsl`
}

group = "com.machinarium.notesv2.buildlogic"

kotlin {
    jvmToolchain(17)
}

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.compose.gradle.plugin)
    compileOnly(libs.ksp.gradle.plugin)
    compileOnly(libs.room.gradle.plugin)
    compileOnly(libs.detekt.gradle.plugin)
    compileOnly(libs.ktlint.gradle.plugin)
    compileOnly(libs.kover.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "app.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "app.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = "app.android.compose"
            implementationClass = "AndroidComposeConventionPlugin"
        }
        register("androidFeature") {
            id = "app.android.feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }
        register("hilt") {
            id = "app.hilt"
            implementationClass = "HiltConventionPlugin"
        }
        register("room") {
            id = "app.room"
            implementationClass = "RoomConventionPlugin"
        }
        register("jvmLibrary") {
            id = "app.jvm.library"
            implementationClass = "JvmLibraryConventionPlugin"
        }
        register("kover") {
            id = "app.kover"
            implementationClass = "KoverConventionPlugin"
        }
        register("quality") {
            id = "app.quality"
            implementationClass = "QualityConventionPlugin"
        }
    }
}
