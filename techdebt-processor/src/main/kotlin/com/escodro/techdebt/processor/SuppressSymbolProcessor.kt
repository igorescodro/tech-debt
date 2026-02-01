package com.escodro.techdebt.processor

import com.escodro.techdebt.extension.getSymbolName
import com.escodro.techdebt.report.TechDebtItem
import com.escodro.techdebt.report.TechDebtItemType
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.validate
import kotlin.sequences.forEach

internal class SuppressSymbolProcessor {

    fun process(
        resolver: Resolver,
        allItems: MutableList<TechDebtItem>,
        allOriginatingFiles: MutableSet<KSFile>,
        moduleName: String,
        sourceSet: String
    ) {
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
}
