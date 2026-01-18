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
        val allItems = parseJsonFiles()
        val aggregatedItems = aggregateItems(allItems)

        val sortedItems = aggregatedItems.sortedWith(
            compareBy(
                { it.moduleName },
                { it.priorityOrder }
            )
        )

        writeReport(sortedItems)
    }

    private fun parseJsonFiles(): List<TechDebtItem> {
        val allItems = mutableListOf<TechDebtItem>()
        jsonFiles.files.forEach { file ->
            if (file.exists()) {
                val items = Json.decodeFromString<List<TechDebtItem>>(file.readText())
                val updatedItems = if (items.any { it.sourceSet == "unknown" }) {
                    val sourceSet = resolveSourceSet(file.absolutePath)
                    items.map { it.copy(sourceSet = sourceSet) }
                } else {
                    items
                }
                allItems.addAll(updatedItems)
            }
        }
        return allItems
    }

    private fun resolveSourceSet(path: String): String {
        val parts = path.split("/")
        val kspIndex = parts.indexOf("ksp")
        return if (kspIndex != -1 && kspIndex + 1 < parts.size) {
            parts[kspIndex + 1]
        } else {
            "unknown"
        }
    }

    private fun aggregateItems(items: List<TechDebtItem>): List<TechDebtItem> =
        items.groupBy {
            // Group by all fields except sourceSet
            listOf(it.moduleName, it.name, it.description, it.ticket, it.priority)
        }.map { (_, group) ->
            val first = group.first()
            val sourceSets = group.map { it.sourceSet }.toSet()
            val consolidatedSourceSet = sourceSets.sorted().joinToString(", ")
            first.copy(sourceSet = consolidatedSourceSet)
        }

    private fun writeReport(items: List<TechDebtItem>) {
        val outputFile = outputFile.get().asFile
        outputFile.parentFile.mkdirs()

        outputFile.bufferedWriter().use { writer ->
            ConsolidatedHtmlReportGenerator().generate(writer, items)
        }

        logger.lifecycle("Tech Debt Report generated: file://${outputFile.absolutePath}")
    }
}
