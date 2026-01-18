package com.escodro.techdebt.gradle

import com.escodro.techdebt.gradle.model.TechDebtItem
import com.escodro.techdebt.gradle.report.ConsolidatedHtmlReportGenerator
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * Gradle task that generates a consolidated HTML report from all tech debt JSON files.
 */
abstract class GenerateTechDebtReportTask : DefaultTask() {

    /**
     * The JSON files containing tech debt items from all modules.
     */
    @get:InputFiles
    abstract val jsonFiles: ConfigurableFileCollection

    /**
     * The output file for the consolidated HTML report.
     */
    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun generate() {
        val allItems = mutableListOf<TechDebtItem>()

        jsonFiles.files.forEach { file ->
            if (file.exists()) {
                val items = Json.decodeFromString<List<TechDebtItem>>(file.readText())
                allItems.addAll(items)
            }
        }

        // Sort by module, then by priority
        val sortedItems = allItems.sortedWith(
            compareBy(
                { it.moduleName },
                { it.priorityOrder }
            )
        )

        val outputFile = outputFile.get().asFile
        outputFile.parentFile.mkdirs()

        outputFile.bufferedWriter().use { writer ->
            ConsolidatedHtmlReportGenerator().generate(writer, sortedItems)
        }

        logger.lifecycle("Tech Debt Report generated: file://${outputFile.absolutePath}")
    }
}
