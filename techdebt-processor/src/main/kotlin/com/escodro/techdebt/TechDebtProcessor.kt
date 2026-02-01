package com.escodro.techdebt

import com.escodro.techdebt.processor.SuppressSymbolProcessor
import com.escodro.techdebt.processor.TechDebtSymbolProcessor
import com.escodro.techdebt.report.TechDebtItem
import com.escodro.techdebt.report.json.TechDebtJsonReportGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFile
import java.io.IOException

/**
 * Processes annotations with `@TechDebt` and `@Suppress` and generates a technical debt report in
 * JSON format.
 *
 * @property environment the symbol processor environment
 */
internal class TechDebtProcessor(
    private val environment: SymbolProcessorEnvironment,
) : SymbolProcessor {

    private val jsonReportGenerator = TechDebtJsonReportGenerator()
    private val moduleName = environment.options["moduleName"] ?: "unknown"
    private val sourceSet = environment.options["sourceSet"] ?: "unknown"
    private val collectSuppress = environment.options["collectSuppress"]?.toBoolean() ?: false
    private val allItems = mutableListOf<TechDebtItem>()
    private val allOriginatingFiles = mutableSetOf<KSFile>()

    private val techDebtSymbolProcessor: TechDebtSymbolProcessor = TechDebtSymbolProcessor()
    private val suppressSymbolProcessor: SuppressSymbolProcessor = SuppressSymbolProcessor()

    /**
     * Processes the symbols in the current round. It delegates the processing of `@TechDebt` and
     * `@Suppress` annotations to their respective processors.
     *
     * @param resolver the KSP resolver
     * @return the list of symbols that were unable to be processed in this round
     */
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val unableToProcess = mutableListOf<KSAnnotated>()

        techDebtSymbolProcessor.process(
            resolver = resolver,
            allItems = allItems,
            allOriginatingFiles = allOriginatingFiles,
            moduleName = moduleName,
            sourceSet = sourceSet,
            unableToProcess = unableToProcess
        )
        if (collectSuppress) {
            suppressSymbolProcessor.process(
                resolver = resolver,
                allItems = allItems,
                allOriginatingFiles = allOriginatingFiles,
                moduleName = moduleName,
                sourceSet = sourceSet
            )
        }
        return unableToProcess
    }

    /**
     * Finishes the processing by generating the consolidated JSON report with all the collected
     * tech debt items.
     */
    @Suppress("SpreadOperator", "TooGenericExceptionCaught")
    override fun finish() {
        if (allItems.isEmpty()) return

        try {
            val dependencies = Dependencies(aggregating = true, *allOriginatingFiles.toTypedArray())

            // Generate JSON report
            val jsonFile =
                environment.codeGenerator.createNewFile(dependencies, "techdebt", "report", "json")
            jsonFile.bufferedWriter().use { writer ->
                jsonReportGenerator.generate(writer, allItems)
            }
        } catch (e: IOException) {
            environment.logger.error(
                "Failed to generate tech debt report due to I/O error: ${e.message}"
            )
        } catch (e: Exception) {
            environment.logger.error(
                "An unexpected error occurred while generating the tech debt report: ${e.message}"
            )
        }
    }
}
