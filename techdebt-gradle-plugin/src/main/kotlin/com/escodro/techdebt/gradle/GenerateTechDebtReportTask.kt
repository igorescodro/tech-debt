package com.escodro.techdebt.gradle

import com.escodro.techdebt.gradle.model.TechDebtItem
import com.escodro.techdebt.gradle.parser.CommentParser
import com.escodro.techdebt.gradle.parser.GeneratedTechDebtParser
import com.escodro.techdebt.gradle.report.ConsolidatedHtmlReportGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
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

    /**
     * Map of project directory to project path. Used to resolve the module name for TODO comments
     * without accessing the Project object at execution time.
     */
    @get:Input abstract val projectPathByDirectory: MapProperty<String, String>

    /** The source files to scan for TODO comments. */
    @get:InputFiles @get:Optional abstract val sourceFiles: ConfigurableFileCollection

    /**
     * The base URL for the tickets. If set, the ticket property in the HTML report will be a link.
     */
    @get:Input @get:Optional abstract val baseTicketUrl: Property<String>

    /** The output file for the consolidated HTML report. */
    @get:OutputFile abstract val outputFile: RegularFileProperty

    private val jsonParser: GeneratedTechDebtParser = GeneratedTechDebtParser()

    private val commentParser: CommentParser = CommentParser()

    @TaskAction
    fun generate() {
        val allItems = mutableListOf<TechDebtItem>()
        allItems += jsonParser.parse(jsonFiles)

        if (collectComments.get()) {
            allItems +=
                commentParser.parse(
                    sourceFiles = sourceFiles,
                    projectPaths = projectPathByDirectory.get()
                )
        }

        val aggregatedItems = aggregateItems(allItems)

        val sortedItems =
            aggregatedItems.sortedWith(compareBy({ it.moduleName }, { it.priorityOrder }))

        writeReport(sortedItems)
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
            ConsolidatedHtmlReportGenerator()
                .generate(writer = writer, items = items, baseTicketUrl = baseTicketUrl.orNull)
        }

        logger.lifecycle("Tech Debt Report generated: file://${outputFile.absolutePath}")
    }
}
