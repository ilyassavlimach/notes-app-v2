import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Coverage gate (TEST-03): ≥ 80% line coverage of logic code. Composables, previews, DI and generated
 * code are excluded so the number measures ViewModels, repositories, mappers and use cases.
 */
class KoverConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlinx.kover")

        extensions.configure<KoverProjectExtension> {
            reports {
                filters {
                    excludes {
                        annotatedBy(
                            "androidx.compose.runtime.Composable",
                            "androidx.compose.ui.tooling.preview.Preview",
                            "*Preview*",
                            "dagger.Module",
                        )
                        classes(
                            "*Hilt_*",
                            "*_Factory",
                            "*_Factory\$*",
                            "*_HiltModules*",
                            "*_MembersInjector",
                            "*ComposableSingletons*",
                            "*.BuildConfig",
                            "*_Impl",
                            "*_Impl\$*",
                            "*.di.*",
                            "*.navigation.*",
                        )
                    }
                }
                verify {
                    rule {
                        minBound(MIN_LINE_COVERAGE)
                    }
                }
            }
        }
    }
}

private const val MIN_LINE_COVERAGE = 80
