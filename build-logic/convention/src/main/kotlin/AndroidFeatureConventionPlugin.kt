import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType

/**
 * A feature module: Compose UI + ViewModel + type-safe navigation, depending only on core modules.
 * Enforces ARCH-01: a feature never depends on another feature.
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("app.android.library")
        pluginManager.apply("app.android.compose")
        pluginManager.apply("app.hilt")
        pluginManager.apply("app.kover")
        pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")

        dependencies {
            add("implementation", project(":core:common"))
            add("implementation", project(":core:designsystem"))
            add("implementation", project(":core:i18n"))
            add("implementation", project(":core:model"))
            add("implementation", project(":core:ui"))
            findProject(":core:data")?.let { add("implementation", it) }
            findProject(":core:domain")?.let { add("implementation", it) }

            add("implementation", libs.lib("androidx-lifecycle-runtime-compose"))
            add("implementation", libs.lib("androidx-lifecycle-viewmodel-compose"))
            add("implementation", libs.lib("androidx-navigation3-runtime"))
            add("implementation", libs.lib("androidx-hilt-lifecycle-viewmodel-compose"))
            add("implementation", libs.lib("kotlinx-collections-immutable"))
            add("implementation", libs.lib("kotlinx-serialization-json"))

            add("testImplementation", project(":core:testing"))
            add("testImplementation", libs.lib("kotlinx-coroutines-test"))
            add("androidTestImplementation", project(":core:testing"))
        }

        afterEvaluate {
            configurations.forEach { configuration ->
                configuration.dependencies.withType<ProjectDependency>().forEach { dependency ->
                    if (dependency.path.startsWith(":feature:") && dependency.path != path) {
                        throw GradleException(
                            "$path depends on ${dependency.path}: features must not depend on other features (ARCH-01). " +
                                "Move the shared code to a core module or wire them through navigation in :app.",
                        )
                    }
                }
            }
        }
    }
}
