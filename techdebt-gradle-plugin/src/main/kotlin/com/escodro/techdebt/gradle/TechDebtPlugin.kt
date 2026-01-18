package com.escodro.techdebt.gradle

import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Gradle plugin that generates a consolidated tech debt report from all modules.
 */
class TechDebtPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create(
            "techDebtReport",
            TechDebtExtension::class.java
        )

        val reportTask = project.tasks.register(
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
            subproject.pluginManager.withPlugin("com.google.devtools.ksp") {
                val kspExtension = subproject.extensions.getByType(KspExtension::class.java)
                kspExtension.arg("moduleName", subproject.path)

                reportTask.configure { task ->
                    task.dependsOn(subproject.tasks.matching { it.name.startsWith("ksp") })
                }
            }
        }
    }
}
