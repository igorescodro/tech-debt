package com.escodro.techdebt

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSDeclaration
import java.io.OutputStreamWriter

internal class TechDebtProcessor(
    private val environment: SymbolProcessorEnvironment,
): SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver
            .getSymbolsWithAnnotation(TechDebt::class.qualifiedName!!)
            .filterIsInstance<KSDeclaration>()

        if (!symbols.iterator().hasNext()) return emptyList()

        val file = environment.codeGenerator.createNewFile(
            Dependencies(false),
            "techdebt",
            "report",
            "html"
        )

        OutputStreamWriter(file).use { writer ->
            writer.appendLine("<html><body>")
            writer.appendLine("<h1>Technical Debt Report</h1>")
            writer.appendLine("<ul>")

            for (symbol in symbols) {
                val annotation = symbol.annotations.first {
                    it.shortName.asString() == "TechDebt"
                }

                val args = annotation.arguments.associate {
                    it.name!!.asString() to it.value.toString()
                }

                val name = symbol.qualifiedName?.asString() ?: symbol.simpleName.asString()
                val reason = args["reason"]
                val ticket = args["ticket"]

                writer.appendLine("<li>")
                writer.appendLine("<b>$name</b><br/>")
                writer.appendLine("Reason: $reason<br/>")
                if (!ticket.isNullOrBlank()) {
                    writer.appendLine("Ticket: $ticket<br/>")
                }
                writer.appendLine("</li>")
            }

            writer.appendLine("</ul>")
            writer.appendLine("</body></html>")
        }

        return emptyList()
    }
}
