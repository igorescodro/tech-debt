package com.escodro.techdebt.gradle.extension

import com.escodro.techdebt.gradle.GenerateTechDebtReportTask
import com.escodro.techdebt.gradle.TechDebtExtension
import com.google.devtools.ksp.gradle.KspExtension
import java.util.Locale.getDefault
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

private const val ANDROID_APPLICATION_PLUGIN = "com.android.application"
private const val ANDROID_LIBRARY_PLUGIN = "com.android.library"
private const val KOTLIN_MULTIPLATFORM_PLUGIN = "org.jetbrains.kotlin.multiplatform"
private const val KOTLIN_JVM_PLUGIN = "org.jetbrains.kotlin.jvm"
private const val TECH_DEBT_PROCESSOR_DEPENDENCY = "io.github.igorescodro:techdebt-processor"
private const val TECH_DEBT_ANNOTATIONS_DEPENDENCY = "io.github.igorescodro:techdebt-annotations"
private const val KSP_PLUGIN = "com.google.devtools.ksp"
private const val KSP_PARAM = "ksp"
private const val IMPLEMENTATION_PARAM = "implementation"
private const val COMMON_MAIN_IMPLEMENTATION = "commonMainImplementation"
private const val KSP_ARG_MODULE_NAME = "moduleName"
private const val KSP_ARG_COLLECT_SUPPRESS = "collectSuppress"
private const val KMP_METADATA_TARGET = "metadata"

/**
 * Configures the Android plugins for the Gradle project to support KSP (Kotlin Symbol Processing)
 * and integrates the tech debt tracking dependencies.
 */
fun Project.configureAndroidPlugins() {
    val androidPlugins = listOf(ANDROID_APPLICATION_PLUGIN, ANDROID_LIBRARY_PLUGIN)
    androidPlugins.forEach { androidPlugin ->
        pluginManager.withPlugin(androidPlugin) {
            pluginManager.apply(KSP_PLUGIN)

            // Using the base 'ksp' configuration applies the processor to all Android variants
            dependencies.add(KSP_PARAM, "$TECH_DEBT_PROCESSOR_DEPENDENCY:${getPluginVersion()}")
            dependencies.add(
                IMPLEMENTATION_PARAM,
                "$TECH_DEBT_ANNOTATIONS_DEPENDENCY:${getPluginVersion()}"
            )
        }
    }
}

/**
 * Configures the Kotlin Multiplatform plugins for the Gradle project to support KSP (Kotlin Symbol
 * Processing) and integrates the tech debt tracking dependencies.
 */
fun Project.configureKmpPlugins() {
    pluginManager.withPlugin(KOTLIN_MULTIPLATFORM_PLUGIN) {
        pluginManager.apply(KSP_PLUGIN)

        extensions.getByType(KotlinMultiplatformExtension::class.java).targets.all { target ->
            if (target.name == KMP_METADATA_TARGET) return@all

            val configurationName =
                "ksp${target.name.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(getDefault()) else it.toString()
                }}"

            // Check if the configuration exists before adding the dependency
            if (configurations.findByName(configurationName) != null) {
                dependencies.add(
                    configurationName,
                    "$TECH_DEBT_PROCESSOR_DEPENDENCY:${getPluginVersion()}"
                )
            } else {
                // Fallback: Use the general 'ksp' configuration if the specific one doesn't
                // exist or just skip it if it's not applicable for this target.
                dependencies.add("ksp", "$TECH_DEBT_PROCESSOR_DEPENDENCY:${getPluginVersion()}")
            }
        }
        dependencies.add(
            COMMON_MAIN_IMPLEMENTATION,
            "$TECH_DEBT_ANNOTATIONS_DEPENDENCY:${getPluginVersion()}"
        )
    }
}

/**
 * Configures the JVM plugins for the Gradle project to support KSP (Kotlin Symbol Processing) and
 * integrates the tech debt tracking dependencies.
 */
fun Project.configureJvmPlugins() {
    pluginManager.withPlugin(KOTLIN_JVM_PLUGIN) {
        pluginManager.apply(KSP_PLUGIN)

        dependencies.add(KSP_PARAM, "$TECH_DEBT_PROCESSOR_DEPENDENCY:${getPluginVersion()}")
        dependencies.add(
            IMPLEMENTATION_PARAM,
            "$TECH_DEBT_ANNOTATIONS_DEPENDENCY:${getPluginVersion()}"
        )
    }
}

/**
 * Configures the KSP modules for the Gradle project, setting up dependencies and report generation.
 *
 * @param reportTask the task to configure the dependencies for
 * @param extension the plugin extension
 */
fun Project.setupKspModules(
    reportTask: TaskProvider<GenerateTechDebtReportTask>,
    extension: TechDebtExtension
) {
    pluginManager.withPlugin(KSP_PLUGIN) {
        val kspExtension = extensions.getByType(KspExtension::class.java)
        kspExtension.arg(k = KSP_ARG_MODULE_NAME, v = path)
        kspExtension.arg(
            k = KSP_ARG_COLLECT_SUPPRESS,
            v = extension.collectSuppress.map { it.toString() }
        )

        reportTask.configure { task ->
            task.dependsOn(tasks.matching { it.name.startsWith(KSP_PARAM) })
        }
    }
}
