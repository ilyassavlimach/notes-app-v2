import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import io.gitlab.arturbosch.detekt.getSupportedKotlinVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jlleitschuh.gradle.ktlint.KtlintExtension

/** detekt (+ Compose rules) and ktlint for every module. Lint is configured per module type. */
class QualityConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("io.gitlab.arturbosch.detekt")
        pluginManager.apply("org.jlleitschuh.gradle.ktlint")

        extensions.configure<DetektExtension> {
            config.setFrom(rootProject.file("config/detekt/detekt.yml"))
            buildUponDefaultConfig = true
            parallel = true
            basePath = rootDir.absolutePath
        }
        dependencies {
            add("detektPlugins", libs.lib("compose-rules-detekt"))
        }
        // detekt 1.x runs on the Kotlin version it was built with; keep it isolated from the project's Kotlin.
        configurations.matching { it.name == "detekt" }.configureEach {
            resolutionStrategy.eachDependency {
                if (requested.group == "org.jetbrains.kotlin") {
                    useVersion(getSupportedKotlinVersion())
                }
            }
        }

        extensions.configure<KtlintExtension> {
            android.set(true)
            ignoreFailures.set(false)
            filter {
                exclude { it.file.path.contains("/build/") }
            }
        }
    }
}
