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
        val androidPlugins = listOf("com.android.application", "com.android.library")
        androidPlugins.forEach { androidPlugin ->
            pluginManager.withPlugin(androidPlugin) {
                pluginManager.apply("com.google.devtools.ksp")

                // Using the base 'ksp' configuration applies the processor to all Android variants
                dependencies.add("ksp", "io.github.igorescodro:techdebt-processor:${getPluginVersion()}")
                dependencies.add("implementation", "io.github.igorescodro:techdebt-annotations:${getPluginVersion()}")
            }
        }
    }

    /**
     * Configures the Kotlin Multiplatform plugins for the Gradle project to support KSP (Kotlin Symbol Processing)
     * and integrates the tech debt tracking dependencies.
     */
    private fun Project.configureKmpPlugins(){
        pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
            pluginManager.apply("com.google.devtools.ksp")

            extensions.getByType(KotlinMultiplatformExtension::class.java).targets.all { target ->
                if (target.name == "metadata") return@all

                dependencies.add("ksp${
                    target.name.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(getDefault()) else it.toString()
                    }
                }", "io.github.igorescodro:techdebt-processor:${getPluginVersion()}")
            }
            dependencies.add("commonMainImplementation", "io.github.igorescodro:techdebt-annotations:${getPluginVersion()}")
        }
    }

    /**
     * Configures the JVM plugins for the Gradle project to support KSP (Kotlin Symbol Processing)
     * and integrates the tech debt tracking dependencies.
     */
    private fun Project.configureJvmPlugins(){
        pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
            pluginManager.apply("com.google.devtools.ksp")

            dependencies.add("ksp", "io.github.igorescodro:techdebt-processor:${getPluginVersion()}")
            dependencies.add("implementation", "io.github.igorescodro:techdebt-annotations:${getPluginVersion()}")
        }
    }

    /**
     * Configures the KSP modules for the Gradle project, setting up dependencies and report generation.
     *
     * @param reportTask the task to configure the dependencies for
     */
    private fun Project.setupKspModules(reportTask: TaskProvider<GenerateTechDebtReportTask>){
        pluginManager.withPlugin("com.google.devtools.ksp") {
            val kspExtension = extensions.getByType(KspExtension::class.java)
            kspExtension.arg("moduleName", path)

            reportTask.configure { task ->
                task?.dependsOn(tasks.matching { it.name.startsWith("ksp") })
            }
        }
    }

    private fun getPluginVersion(): String {
        val props = Properties().apply {
            val classLoader = TechDebtPlugin::class.java.classLoader
            val resourceStream = classLoader?.getResourceAsStream("techdebt.properties")
                ?: Thread.currentThread().contextClassLoader?.getResourceAsStream("techdebt.properties")
            if (resourceStream == null) {
                // Fallback for cases where the resource might not be in the classpath during tests or specific environments
                return "1.0.0"
            }
            load(resourceStream)
        }
        return props["version"] as String
    }
}
