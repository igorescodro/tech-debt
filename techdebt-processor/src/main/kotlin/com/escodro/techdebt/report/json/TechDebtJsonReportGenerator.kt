package com.escodro.techdebt.report.json

import com.escodro.techdebt.report.TechDebtItem
import java.io.Writer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Generates a JSON report containing tech debt items. */
internal class TechDebtJsonReportGenerator {

    private val json = Json { prettyPrint = true }

    /**
     * Generates a JSON report with the given tech debt items.
     *
     * @param writer the writer to output the JSON content
     * @param items the list of tech debt items to include in the report
     */
    fun generate(writer: Writer, items: List<TechDebtItem>) {
        writer.write(json.encodeToString(items))
    }
}
