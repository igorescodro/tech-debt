package com.escodro.techdebt.gradle

import com.escodro.techdebt.gradle.model.TechDebtItem
import com.escodro.techdebt.gradle.model.TechDebtItemType
import com.escodro.techdebt.gradle.report.ConsolidatedHtmlReportGenerator
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/** Gradle task that generates a consolidated HTML report from all tech debt JSON files. */
abstract class GenerateTechDebtReportTask : DefaultTask() {

    /** The JSON files containing tech debt items from all modules. */
    @get:InputFiles abstract val jsonFiles: ConfigurableFileCollection

    /** Whether to collect TODO/FIXME comments. Defaults to `false`. */
    @get:Input abstract val collectComments: Property<Boolean>

    /** The source files to scan for TODO comments. */
    @get:InputFiles @get:Optional abstract val sourceFiles: ConfigurableFileCollection

    /** The output file for the consolidated HTML report. */
    @get:OutputFile abstract val outputFile: RegularFileProperty

    @TaskAction
    fun generate() {
        val allItems = mutableListOf<TechDebtItem>()
        allItems += parseKspGeneratedFiles()

        if (collectComments.get()) {
            allItems += collectTodoComments()
        }

        val aggregatedItems = aggregateItems(allItems)

        val sortedItems =
            aggregatedItems.sortedWith(compareBy({ it.moduleName }, { it.priorityOrder }))

        writeReport(sortedItems)
    }

    private fun parseKspGeneratedFiles(): List<TechDebtItem> {
        val allItems = mutableListOf<TechDebtItem>()
        jsonFiles.files.forEach { file ->
            if (file.exists()) {
                val items = Json.decodeFromString<List<TechDebtItem>>(file.readText())
                val updatedItems =
                    if (items.any { it.sourceSet == "unknown" }) {
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

    private fun collectTodoComments(): List<TechDebtItem> {
        val items = mutableListOf<TechDebtItem>()
        val todoCommentPattern =
            Regex("""^\s*(?://|/\*\*?|\*)\s*(TODO|FIXME)\b[:\s]?(.*?)(?:\s*\*+/)?\s*$""")

        sourceFiles.files.forEach { file ->
            val subproject =
                project.allprojects.find { file.startsWith(it.projectDir) } ?: return@forEach
            file.readLines(Charsets.UTF_8).forEachIndexed { index, line ->
                val matchResult = todoCommentPattern.find(line)
                if (matchResult != null) {
                    val type = matchResult.groupValues[1]
                    val content = matchResult.groupValues[2].trim()
                    val description = if (content.isEmpty()) type else "$type: $content"

                    val relativePath = file.relativeTo(subproject.projectDir).path
                    items +=
                        TechDebtItem(
                            moduleName = subproject.path,
                            sourceSet = "$relativePath:${index + 1}",
                            name = "",
                            description = description,
                            ticket = "",
                            priority = "",
                            type = TechDebtItemType.COMMENT
                        )
                }
            }
        }

        return items
    }

    private fun aggregateItems(items: List<TechDebtItem>): List<TechDebtItem> =
        items
            .groupBy {
                listOf(it.moduleName, it.name, it.description, it.ticket, it.priority, it.type)
            }
            .map { (_, group) ->
                val first = group.first()
                val sourceSets = group.map { it.sourceSet }.toSet()
                first.copy(sourceSet = sourceSets.sorted().joinToString(", "))
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
