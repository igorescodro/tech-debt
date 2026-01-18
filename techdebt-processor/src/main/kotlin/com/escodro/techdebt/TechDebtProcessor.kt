package com.escodro.techdebt

import com.escodro.techdebt.report.TechDebtItem
import com.escodro.techdebt.report.json.TechDebtJsonReportGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.validate
import java.io.IOException

/**
 * Processes annotations with `@TechDebt` and generates a technical debt report.
 *
 * @property environment the symbol processor environment
 */
internal class TechDebtProcessor(
    private val environment: SymbolProcessorEnvironment,
) : SymbolProcessor {

    private val jsonReportGenerator = TechDebtJsonReportGenerator()
    private val moduleName = environment.options["moduleName"] ?: "unknown"
    private val sourceSet = environment.options["sourceSet"] ?: "unknown"
    private val allItems = mutableListOf<TechDebtItem>()
    private val allOriginatingFiles = mutableSetOf<KSFile>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val unableToProcess = mutableListOf<KSAnnotated>()
        resolver
            .getSymbolsWithAnnotation(TechDebt::class.qualifiedName!!)
            .filterIsInstance<KSDeclaration>()
            .forEach { symbol ->
                if (!symbol.validate()) {
                    unableToProcess.add(symbol)
                    return@forEach
                }

                symbol.containingFile?.let { allOriginatingFiles.add(it) }

                val annotation =
                    symbol.annotations.firstOrNull {
                        it.annotationType.resolve().declaration.qualifiedName?.asString() ==
                            TechDebt::class.qualifiedName
                    } ?: return@forEach

                val args = annotation.arguments.associate { it.name!!.asString() to it.value }

                val priority =
                    when (val value = args["priority"]) {
                        is KSType -> value.declaration.simpleName.asString()
                        is KSClassDeclaration -> value.simpleName.asString()
                        else -> "NONE"
                    }

                allItems.add(
                    TechDebtItem(
                        moduleName = moduleName,
                        name = symbol.qualifiedName?.asString() ?: symbol.simpleName.asString(),
                        description = args["description"]?.toString().orEmpty(),
                        ticket = args["ticket"]?.toString().orEmpty(),
                        priority = priority,
                        sourceSet = sourceSet
                    )
                )
            }

        return unableToProcess
    }

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
