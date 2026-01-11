package com.escodro.techdebt

import com.escodro.techdebt.report.TechDebtItem
import com.escodro.techdebt.report.html.TechDebtHtmlReportGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSType

/**
 * Processes annotations with `@TechDebt` and generates a technical debt report.
 *
 * @property environment the symbol processor environment
 */
internal class TechDebtProcessor(
    private val environment: SymbolProcessorEnvironment,
) : SymbolProcessor {

    private val reportGenerator = TechDebtHtmlReportGenerator()

    @Suppress("SpreadOperator")
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols =
            resolver
                .getSymbolsWithAnnotation(TechDebt::class.qualifiedName!!)
                .filterIsInstance<KSDeclaration>()

        if (!symbols.iterator().hasNext()) return emptyList()

        val items = mutableListOf<TechDebtItem>()
        val originatingFiles = mutableSetOf<KSFile>()

        symbols.forEach { symbol ->
            symbol.containingFile?.let { originatingFiles.add(it) }

            val annotation =
                symbol.annotations.first {
                    it.annotationType.resolve().declaration.qualifiedName?.asString() ==
                        TechDebt::class.qualifiedName
                }

            val args = annotation.arguments.associate { it.name!!.asString() to it.value }

            items.add(
                TechDebtItem(
                    name = symbol.qualifiedName?.asString() ?: symbol.simpleName.asString(),
                    description = args["description"]?.toString().orEmpty(),
                    ticket = args["ticket"]?.toString().orEmpty(),
                    priority =
                        (args["priority"] as? KSType)?.declaration?.simpleName?.asString() ?: "NONE"
                )
            )
        }

        val file =
            environment.codeGenerator.createNewFile(
                Dependencies(aggregating = true, *originatingFiles.toTypedArray()),
                "techdebt",
                "report",
                "html"
            )

        file.bufferedWriter().use { writer -> reportGenerator.generate(writer, items) }

        return emptyList()
    }
}
