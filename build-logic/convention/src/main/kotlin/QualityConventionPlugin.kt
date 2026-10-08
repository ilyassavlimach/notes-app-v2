import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import io.gitlab.arturbosch.detekt.getSupportedKotlinVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
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
        // detekt otherwise takes the JVM target from the Gradle daemon's JDK (e.g. 25 when the IDE writes
        // gradle-daemon-jvm.properties), which detekt 1.x rejects. 17 matches the Kotlin jvmTarget.
        tasks.withType<Detekt>().configureEach { jvmTarget = DETEKT_JVM_TARGET }
        tasks.withType<DetektCreateBaselineTask>().configureEach { jvmTarget = DETEKT_JVM_TARGET }
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

private const val DETEKT_JVM_TARGET = "17"
