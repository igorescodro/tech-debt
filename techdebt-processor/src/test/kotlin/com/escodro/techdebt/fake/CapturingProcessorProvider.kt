package com.escodro.techdebt.fake

import com.escodro.techdebt.TechDebtProcessor
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

/** Provides a capturing symbol processor for testing purposes. */
internal class CapturingProcessorProvider : SymbolProcessorProvider {
    var processor: CapturingProcessor? = null

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        val actualProcessor = TechDebtProcessor(environment)
        return CapturingProcessor(actualProcessor).also { processor = it }
    }
}
