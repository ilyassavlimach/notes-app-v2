pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

plugins {
    // Auto-provisions the JDK used to run unit tests (Robolectric needs a newer Java than the build).
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "notes-app-v2"

// Modules — keep sorted; android-app-builder adds a line per module it creates.
include(":app")
include(":core:analytics")
include(":core:common")
include(":core:config")
include(":core:data")
include(":core:database")
include(":core:designsystem")
include(":core:i18n")
include(":core:model")
include(":core:network")
include(":core:notifications")
include(":core:testing")
include(":core:ui")
include(":feature:notedetail")
include(":feature:noteeditor")
include(":feature:noteslist")
