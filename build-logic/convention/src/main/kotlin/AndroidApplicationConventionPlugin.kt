import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.application")
        applyKotlinAndroidIfNeeded()
        pluginManager.apply("app.android.compose")
        pluginManager.apply("app.quality")

        extensions.configure<ApplicationExtension> {
            namespace = providers.gradleProperty("app.package").get()
            compileSdk = libs.intVersion("compileSdk")
            defaultConfig {
                minSdk = libs.intVersion("minSdk")
                targetSdk = libs.intVersion("targetSdk")
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
            buildFeatures {
                buildConfig = true
            }
            packaging {
                resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
            }
            configureLint(lint, checkDependencies = true)
        }
        configureKotlinCompiler()
        configureTestJvm()
    }
}
