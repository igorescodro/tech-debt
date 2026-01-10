package com.escodro.techdebt

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSDeclaration

internal class TechDebtProcessor(
    private val environment: SymbolProcessorEnvironment,
): SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val annotations = resolver
            .getSymbolsWithAnnotation(TechDebt::class.qualifiedName!!)
            .filterIsInstance<KSDeclaration>()

        if(!annotations.iterator().hasNext()) return emptyList()

            for (symbol in annotations) {
                val annotation = symbol.annotations.first {
                    it.shortName.asString() == "TechDebt"
                }

                val args = annotation.arguments.associate {
                    it.name!!.asString() to it.value.toString()
                }

                val name = symbol.qualifiedName?.asString() ?: symbol.simpleName.asString()
                val reason = args["reason"]
                val ticket = args["ticket"]

                println("[TECH DEBT] $name")
                println("Reason: $reason")
                println("Ticket: $ticket")
                println("-----------------")
            }

        return emptyList()
    }
}
