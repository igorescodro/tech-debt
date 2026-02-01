package com.escodro.techdebt.extension

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFile

/**
 * Returns the name of the given symbol, which can be a declaration or a file.
 *
 * @param symbol the symbol to get the name
 * @return the symbol name
 */
fun getSymbolName(symbol: KSAnnotated): String =
    when (symbol) {
        is KSDeclaration -> symbol.qualifiedName?.asString() ?: symbol.simpleName.asString()
        is KSFile -> symbol.fileName
        else -> "unknown"
    }
