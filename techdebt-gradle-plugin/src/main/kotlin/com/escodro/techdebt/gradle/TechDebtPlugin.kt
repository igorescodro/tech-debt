package com.escodro.techdebt.gradle

import com.escodro.techdebt.gradle.extension.TechDebtExtension
import com.escodro.techdebt.gradle.extension.configureAndroidPlugins
import com.escodro.techdebt.gradle.extension.configureJvmPlugins
import com.escodro.techdebt.gradle.extension.configureKmpPlugins
import com.escodro.techdebt.gradle.extension.setupKspModules
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider

/** Gradle plugin that generates a consolidated tech debt report from all modules. */
class TechDebtPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension: TechDebtExtension =
            project.extensions.create("techDebtReport", TechDebtExtension::class.java)
        extension.collectSuppress.convention(false)
        extension.collectComments.convention(false)
        extension.enableGitMetadata.convention(false)

        val reportTask: TaskProvider<GenerateTechDebtReportTask> =
            project.tasks.register(
                "generateTechDebtReport",
                GenerateTechDebtReportTask::class.java
            ) { task ->
                task.collectComments.set(extension.collectComments)
                task.enableGitMetadata.set(extension.enableGitMetadata)
                task.baseTicketUrl.set(extension.baseTicketUrl)
                task.projectPathByDirectory.set(
                    project.allprojects.associate { it.projectDir.absolutePath to it.path }
                )
                task.rootProjectDirectory.set(project.rootProject.layout.projectDirectory)
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

                // Collect source files from all subprojects if collectComments is enabled
                task.sourceFiles.from(
                    extension.collectComments.map { enabled ->
                        if (enabled) {
                            project.allprojects.map { subproject ->
                                subproject.fileTree("src").matching { it.include("**/*.kt") }
                            }
                        } else {
                            emptyList<Any>()
                        }
                    }
                )
            }

        // Set up task dependencies and autoconfigure moduleName for all subprojects with KSP
        project.allprojects.forEach { subproject ->
            with(subproject) {
                configureAndroidPlugins()
                configureKmpPlugins()
                configureJvmPlugins()
                setupKspModules(reportTask = reportTask, extension = extension)
            }
        }
    }
}
