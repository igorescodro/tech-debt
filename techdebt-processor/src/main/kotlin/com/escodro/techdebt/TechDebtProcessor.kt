package com.escodro.techdebt

import com.escodro.techdebt.report.TechDebtItem
import com.escodro.techdebt.report.TechDebtItemType
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
    private val collectSuppress = environment.options["collectSuppress"]?.toBoolean() ?: false
    private val allItems = mutableListOf<TechDebtItem>()
    private val allOriginatingFiles = mutableSetOf<KSFile>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val unableToProcess = mutableListOf<KSAnnotated>()

        processTechDebt(resolver, unableToProcess)
        if (collectSuppress) {
            processSuppress(resolver)
        }
        return unableToProcess
    }

    private fun processTechDebt(resolver: Resolver, unableToProcess: MutableList<KSAnnotated>) {
        resolver.getSymbolsWithAnnotation(TechDebt::class.qualifiedName!!).forEach { symbol ->
            if (!symbol.validate()) {
                unableToProcess.add(symbol)
                return@forEach
            }

            val ksFile = symbol as? KSFile ?: (symbol as? KSDeclaration)?.containingFile
            ksFile?.let { allOriginatingFiles.add(it) }

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

            val name = getSymbolName(symbol)

            allItems.add(
                TechDebtItem(
                    moduleName = moduleName,
                    name = name,
                    description = args["description"]?.toString().orEmpty(),
                    ticket = args["ticket"]?.toString().orEmpty(),
                    priority = priority,
                    sourceSet = sourceSet
                )
            )
        }
    }

    private fun processSuppress(resolver: Resolver) {
        resolver.getSymbolsWithAnnotation(Suppress::class.qualifiedName!!).forEach { symbol ->
            if (!symbol.validate()) return@forEach

            val ksFile = symbol as? KSFile ?: (symbol as? KSDeclaration)?.containingFile
            ksFile?.let { allOriginatingFiles.add(it) }

            val suppressAnnotations =
                symbol.annotations
                    .filter {
                        it.annotationType.resolve().declaration.qualifiedName?.asString() ==
                            Suppress::class.qualifiedName
                    }
                    .toList()
            if (suppressAnnotations.isEmpty()) return@forEach
            val ruleNames =
                suppressAnnotations.flatMap { annotation ->
                    val namesArg =
                        annotation.arguments.firstOrNull { it.name?.asString() == "names" }?.value
                    when (namesArg) {
                        is List<*> -> namesArg.mapNotNull { it?.toString() }
                        is Array<*> -> namesArg.mapNotNull { it?.toString() }
                        else -> emptyList()
                    }
                }

            val name = getSymbolName(symbol)

            ruleNames.forEach { rule ->
                allItems.add(
                    TechDebtItem(
                        moduleName = moduleName,
                        name = name,
                        description = rule,
                        ticket = "",
                        priority = "",
                        sourceSet = sourceSet,
                        type = TechDebtItemType.SUPPRESS
                    )
                )
            }
        }
    }

    private fun getSymbolName(symbol: KSAnnotated): String =
        when (symbol) {
            is KSDeclaration -> symbol.qualifiedName?.asString() ?: symbol.simpleName.asString()
            is KSFile -> symbol.fileName
            else -> "unknown"
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
