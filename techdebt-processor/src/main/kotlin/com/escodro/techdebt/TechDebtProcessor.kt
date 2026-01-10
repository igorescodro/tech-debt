package com.escodro.techdebt

import com.escodro.techdebt.report.TechDebtItem
import com.escodro.techdebt.report.html.TechDebtReportGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSDeclaration

/**
 * Processes annotations with `@TechDebt` and generates a technical debt report.
 *
 * @property environment the symbol processor environment
 */
internal class TechDebtProcessor(
    private val environment: SymbolProcessorEnvironment,
) : SymbolProcessor {

    private val reportGenerator = TechDebtReportGenerator()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver
            .getSymbolsWithAnnotation(TechDebt::class.qualifiedName!!)
            .filterIsInstance<KSDeclaration>()

        if (!symbols.iterator().hasNext()) return emptyList()

        val items = symbols.map { symbol ->
            val annotation = symbol.annotations.first {
                it.shortName.asString() == "TechDebt"
            }

            val args = annotation.arguments.associate {
                it.name!!.asString() to it.value.toString()
            }

            TechDebtItem(
                name = symbol.qualifiedName?.asString() ?: symbol.simpleName.asString(),
                description = args["description"] ?: "",
                ticket = args["ticket"] ?: "",
                priority = args["priority"]?.substringAfterLast('.') ?: "NONE"
            )
        }.toList()

        val file = environment.codeGenerator.createNewFile(
            Dependencies(false),
            "techdebt",
            "report",
            "html"
        )

        file.bufferedWriter().use { writer ->
            reportGenerator.generate(writer, items)
        }

        return emptyList()
    }
}
