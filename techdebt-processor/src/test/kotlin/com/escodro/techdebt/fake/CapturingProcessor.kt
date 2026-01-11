package com.escodro.techdebt.fake

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated

/** Captures the results of a symbol processor for testing purposes. */
internal class CapturingProcessor(private val delegate: SymbolProcessor) : SymbolProcessor {
    val capturedResults = mutableListOf<KSAnnotated>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val results = delegate.process(resolver)
        capturedResults.addAll(results)
        return results
    }

    override fun finish() {
        delegate.finish()
    }

    override fun onError() {
        delegate.onError()
    }
}
