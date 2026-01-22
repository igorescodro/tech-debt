package com.escodro.techdebt.gradle

import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import java.util.Locale.getDefault
import java.util.Properties

/**
 * Gradle plugin that generates a consolidated tech debt report from all modules.
 */
class TechDebtPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension: TechDebtExtension = project.extensions.create(
            "techDebtReport",
            TechDebtExtension::class.java
        )

        val reportTask: TaskProvider<GenerateTechDebtReportTask> = project.tasks.register(
            "generateTechDebtReport",
            GenerateTechDebtReportTask::class.java
        ) { task ->
            task.outputFile.set(
                extension.outputFile.convention(
                    project.layout.buildDirectory.file(
                        "reports/techdebt/consolidated-report.html"
                    )
                )
            )

            // Collect JSON files from all subprojects
            task.jsonFiles.from(
                project.allprojects.map { subproject ->
                    subproject.fileTree("build/generated/ksp").matching {
                        it.include("**/resources/techdebt/report.json")
                    }
                }
            )
        }

        // Set up task dependencies and auto-configure moduleName for all subprojects with KSP
        project.allprojects.forEach { subproject ->
            with(subproject) {
                configureAndroidPlugins()
                configureKmpPlugins()
                configureJvmPlugins()
                setupKspModules(reportTask)
            }
        }
    }

    /**
     * Configures the Android plugins for the Gradle project to support KSP (Kotlin Symbol Processing)
     * and integrates the tech debt tracking dependencies.
     */
    private fun Project.configureAndroidPlugins(){
        val androidPlugins = listOf(ANDROID_APPLICATION_PLUGIN, ANDROID_LIBRARY_PLUGIN)
        androidPlugins.forEach { androidPlugin ->
            pluginManager.withPlugin(androidPlugin) {
                pluginManager.apply(KSP_PLUGIN)

                // Using the base 'ksp' configuration applies the processor to all Android variants
                dependencies.add(KSP_PARAM, "$TECH_DEBT_PROCESSOR_DEPENDENCY:${getPluginVersion()}")
                dependencies.add(IMPLEMENTATION_PARAM, "$TECH_DEBT_ANNOTATIONS_DEPENDENCY:${getPluginVersion()}")
            }
        }
    }

    /**
     * Configures the Kotlin Multiplatform plugins for the Gradle project to support KSP (Kotlin Symbol Processing)
     * and integrates the tech debt tracking dependencies.
     */
    private fun Project.configureKmpPlugins(){
        pluginManager.withPlugin(KOTLIN_MULTIPLATFORM_PLUGIN) {
            pluginManager.apply(KSP_PLUGIN)

            extensions.getByType(KotlinMultiplatformExtension::class.java).targets.all { target ->
                if (target.name == KMP_METADATA_TARGET) return@all

                dependencies.add("ksp${
                    target.name.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(getDefault()) else it.toString()
                    }
                }", "$TECH_DEBT_PROCESSOR_DEPENDENCY:${getPluginVersion()}")
            }
            dependencies.add(COMMON_MAIN_IMPLEMENTATION, "$TECH_DEBT_ANNOTATIONS_DEPENDENCY:${getPluginVersion()}")
        }
    }

    /**
     * Configures the JVM plugins for the Gradle project to support KSP (Kotlin Symbol Processing)
     * and integrates the tech debt tracking dependencies.
     */
    private fun Project.configureJvmPlugins(){
        pluginManager.withPlugin(KOTLIN_JVM_PLUGIN) {
            pluginManager.apply(KSP_PLUGIN)

            dependencies.add(KSP_PARAM, "$TECH_DEBT_PROCESSOR_DEPENDENCY:${getPluginVersion()}")
            dependencies.add(IMPLEMENTATION_PARAM, "$TECH_DEBT_ANNOTATIONS_DEPENDENCY:${getPluginVersion()}")
        }
    }

    /**
     * Configures the KSP modules for the Gradle project, setting up dependencies and report generation.
     *
     * @param reportTask the task to configure the dependencies for
     */
    private fun Project.setupKspModules(reportTask: TaskProvider<GenerateTechDebtReportTask>){
        pluginManager.withPlugin(KSP_PLUGIN) {
            val kspExtension = extensions.getByType(KspExtension::class.java)
            kspExtension.arg(KSP_ARG_MODULE_NAME, path)

            reportTask.configure { task ->
                task.dependsOn(tasks.matching { it.name.startsWith(KSP_PARAM) })
            }
        }
    }

    private fun getPluginVersion(): String {
        val props = Properties().apply {
            val classLoader = TechDebtPlugin::class.java.classLoader
            val resourceStream = classLoader?.getResourceAsStream(TECH_DEBT_PROPERTIES)
                ?: Thread.currentThread().contextClassLoader?.getResourceAsStream(TECH_DEBT_PROPERTIES)
            if (resourceStream == null) {
                // Fallback for cases where the resource might not be in the classpath during tests or specific environments
                return "1.0.0"
            }
            load(resourceStream)
        }
        return props[VERSION_PROPERTY] as String
    }

    private companion object {
        private const val ANDROID_APPLICATION_PLUGIN = "com.android.application"
        private const val ANDROID_LIBRARY_PLUGIN = "com.android.library"
        private const val KOTLIN_MULTIPLATFORM_PLUGIN = "org.jetbrains.kotlin.multiplatform"
        private const val KOTLIN_JVM_PLUGIN = "org.jetbrains.kotlin.jvm"
        private const val TECH_DEBT_PROCESSOR_DEPENDENCY = "io.github.igorescodro:techdebt-processor"
        private const val TECH_DEBT_ANNOTATIONS_DEPENDENCY = "io.github.igorescodro:techdebt-annotations"
        private const val KSP_PLUGIN = "com.google.devtools.ksp"
        private const val TECH_DEBT_PROPERTIES = "techdebt.properties"
        private const val KSP_PARAM = "ksp"
        private const val IMPLEMENTATION_PARAM = "implementation"
        private const val COMMON_MAIN_IMPLEMENTATION = "commonMainImplementation"
        private const val VERSION_PROPERTY = "version"
        private const val KSP_ARG_MODULE_NAME = "moduleName"
        private const val KMP_METADATA_TARGET = "metadata"
    }
}
