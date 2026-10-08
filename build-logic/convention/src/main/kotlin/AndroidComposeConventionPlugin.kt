import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.findByType
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

/** Compose for an Android application or library module. Apply after app.android.application/library. */
class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        extensions.findByType<ApplicationExtension>()?.buildFeatures?.compose = true
        extensions.findByType<LibraryExtension>()?.buildFeatures?.compose = true

        extensions.configure(ComposeCompilerGradlePluginExtension::class.java) {
            // ./gradlew assembleRelease -PcomposeReports=true → build/compose_compiler (stability reports)
            if (providers.gradleProperty("composeReports").isPresent) {
                reportsDestination.set(layout.buildDirectory.dir("compose_compiler"))
                metricsDestination.set(layout.buildDirectory.dir("compose_compiler"))
            }
        }

        dependencies {
            val bom = platform(libs.lib("androidx-compose-bom"))
            add("implementation", bom)
            add("androidTestImplementation", bom)
            add("implementation", libs.lib("androidx-compose-ui"))
            add("implementation", libs.lib("androidx-compose-material3"))
            add("implementation", libs.lib("androidx-compose-ui-tooling-preview"))
            add("debugImplementation", libs.lib("androidx-compose-ui-tooling"))
            add("debugImplementation", libs.lib("androidx-compose-ui-test-manifest"))
            add("androidTestImplementation", libs.lib("androidx-compose-ui-test-junit4"))
            add("androidTestImplementation", libs.lib("androidx-test-ext-junit"))
            add("androidTestImplementation", libs.lib("androidx-test-runner"))
            // Compose UI tests run on the JVM with Robolectric (src/test), so the gate executes them (TEST-04).
            add("testImplementation", bom)
            add("testImplementation", libs.lib("androidx-compose-ui-test-junit4"))
            add("testImplementation", libs.lib("androidx-test-ext-junit"))
            add("testImplementation", libs.lib("robolectric"))
            // Pin Espresso: the version Compose UI test pulls in transitively can lag behind new Android SDKs.
            add("testImplementation", libs.lib("androidx-test-espresso-core"))
            add("androidTestImplementation", libs.lib("androidx-test-espresso-core"))
        }
    }
}
