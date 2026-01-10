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
            writer.appendLine(
                """
                <!DOCTYPE html>
                <html>
                <head>
                <style>
                    body {
                        font-family: sans-serif;
                        background-color: #f3f3f3;
                        margin: 0;
                        padding: 20px;
                    }
                    h1 {
                        color: #333;
                    }
                    table {
                        width: 100%;
                        border-collapse: collapse;
                        background-color: #fff;
                        box-shadow: 0 2px 5px rgba(0,0,0,0.1);
                        border-radius: 8px;
                        overflow: hidden;
                    }
                    th, td {
                        padding: 12px 15px;
                        text-align: left;
                        border-bottom: 1px solid #ddd;
                    }
                    th {
                        background-color: #4CAF50;
                        color: white;
                        text-transform: uppercase;
                        letter-spacing: 0.1em;
                        font-size: 14px;
                    }
                    tr:hover {
                        background-color: #f5f5f5;
                    }
                    .ticket {
                        font-family: monospace;
                        background-color: #eee;
                        padding: 2px 4px;
                        border-radius: 4px;
                    }
                </style>
                </head>
                <body>
                <h1>Technical Debt Report</h1>
                <table>
                    <thead>
                        <tr>
                            <th>Symbol</th>
                            <th>Description</th>
                            <th>Ticket</th>
                            <th>Priority</th>
                        </tr>
                    </thead>
                    <tbody>
                """.trimIndent()
            )

            for (symbol in symbols) {
                val annotation = symbol.annotations.first {
                    it.shortName.asString() == "TechDebt"
                }

                val args = annotation.arguments.associate {
                    it.name!!.asString() to it.value.toString()
                }

                val name = symbol.qualifiedName?.asString() ?: symbol.simpleName.asString()
                val description = args["description"] ?: ""
                val ticket = args["ticket"] ?: ""
                val priority = args["priority"]?.substringAfterLast('.') ?: "NONE"

                writer.appendLine(
                    """
                    <tr>
                        <td><strong>$name</strong></td>
                        <td>$description</td>
                        <td>${if (ticket.isNotEmpty()) "<span class='ticket'>$ticket</span>" else ""}</td>
                        <td>$priority</td>
                    </tr>
                    """.trimIndent()
                )
            }

            writer.appendLine(
                """
                    </tbody>
                </table>
                </body>
                </html>
                """.trimIndent()
            )
        }

        return emptyList()
    }
}
