import com.android.build.api.dsl.Lint
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.jvm.toolchain.JvmVendorSpec
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.intVersion(name: String): Int = findVersion(name).get().requiredVersion.toInt()

internal fun VersionCatalog.lib(alias: String) = findLibrary(alias).get()

/** Namespace derived from the module path, e.g. :core:i18n -> com.example.app.core.i18n */
internal fun Project.moduleNamespace(): String {
    val base = providers.gradleProperty("app.package").get()
    return base + path.replace(':', '.').replace('-', '_')
}

/** AGP 9+ compiles Kotlin itself (built-in Kotlin); older AGP needs the kotlin-android plugin. */
internal fun Project.applyKotlinAndroidIfNeeded() {
    val agpMajor = libs.findVersion("agp").get().requiredVersion.substringBefore('.').toInt()
    if (agpMajor < BUILT_IN_KOTLIN_AGP_MAJOR) {
        pluginManager.apply("org.jetbrains.kotlin.android")
    }
}

internal fun Project.configureKotlinCompiler() {
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            allWarningsAsErrors.set(true)
        }
    }
}

/** Unit tests (incl. Robolectric for recent Android SDKs) run on a newer JDK than the build itself. */
internal fun Project.configureTestJvm() {
    val toolchains = extensions.getByType<JavaToolchainService>()
    tasks.withType<Test>().configureEach {
        javaLauncher.set(
            toolchains.launcherFor {
                languageVersion.set(JavaLanguageVersion.of(TEST_JAVA_VERSION))
                // A standard OpenJDK build: Robolectric patches JDK internals that other runtimes (e.g. JBR) differ in.
                vendor.set(JvmVendorSpec.ADOPTIUM)
            },
        )
        // Robolectric reaches into java.io internals on recent JDKs.
        jvmArgs(
            "--add-opens=java.base/java.io=ALL-UNNAMED",
            "--add-opens=java.base/java.lang=ALL-UNNAMED",
            "--add-exports=java.base/jdk.internal.access=ALL-UNNAMED",
        )
        // Hilt generates test-variant classes even in modules without tests.
        failOnNoDiscoveredTests.set(false)
    }
}

internal fun Project.configureLint(lint: Lint, checkDependencies: Boolean) {
    lint.apply {
        abortOnError = true
        warningsAsErrors = true
        this.checkDependencies = checkDependencies
        lintConfig = rootProject.file("config/lint.xml")
        // Upgrade hints are handled by resolve_versions.py; they must not fail the build on release day.
        disable += setOf("NewerVersionAvailable", "GradleDependency", "AndroidGradlePluginVersion", "OldTargetApi")
        // Library modules cannot see usages in other modules; the app module checks this with checkDependencies.
        if (!checkDependencies) disable += "UnusedResources"
    }
}

private const val BUILT_IN_KOTLIN_AGP_MAJOR = 9
private const val TEST_JAVA_VERSION = 21
