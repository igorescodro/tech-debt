package com.escodro.techdebt.processor

import com.escodro.techdebt.TechDebt
import com.escodro.techdebt.extension.getAnnotationLocation
import com.escodro.techdebt.extension.getSymbolName
import com.escodro.techdebt.report.TechDebtItem
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.validate
import kotlin.sequences.forEach

/** Symbol processor responsible for processing symbols annotated with `@TechDebt`. */
internal class TechDebtSymbolProcessor {

    /**
     * Processes the symbols annotated with `@TechDebt`.
     *
     * @param resolver the KSP resolver
     * @param allItems the list of tech debt items to be populated
     * @param allOriginatingFiles the set of originating files to be populated
     * @param moduleName the name of the module being processed
     * @param sourceSet the name of the source set being processed
     * @param unableToProcess the list of symbols that were unable to be processed
     */
    @Suppress("LongParameterList")
    fun process(
        resolver: Resolver,
        allItems: MutableList<TechDebtItem>,
        allOriginatingFiles: MutableSet<KSFile>,
        moduleName: String,
        sourceSet: String,
        unableToProcess: MutableList<KSAnnotated>
    ) {
        resolver.getSymbolsWithAnnotation(TechDebt::class.qualifiedName!!).forEach { symbol ->
            if (!symbol.validate()) {
                unableToProcess.add(symbol)
                return@forEach
            }

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
            val sourceLocation =
                getAnnotationLocation(annotation = annotation, sourceSet = sourceSet)
            val ksFile = symbol as? KSFile ?: (symbol as? KSDeclaration)?.containingFile
            ksFile?.let { allOriginatingFiles.add(it) }

            allItems.add(
                TechDebtItem(
                    moduleName = moduleName,
                    name = name,
                    description = args["description"]?.toString().orEmpty(),
                    ticket = args["ticket"]?.toString().orEmpty(),
                    priority = priority,
                    sourceSet = sourceSet,
                    location = sourceLocation,
                )
            )
        }
    }
}
