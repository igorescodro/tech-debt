package com.escodro.techdebt.gradle.report

import com.escodro.techdebt.gradle.model.TechDebtItem
import kotlinx.html.BODY
import kotlinx.html.h2

/** Generates a section of tech debt items in the HTML report. */
internal class SectionGenerator(private val cardGenerator: CardGenerator) {

    /**
     * Appends a section with a title and a list of tech debt items to the BODY element.
     *
     * @param body the BODY element to append the section
     * @param title the title of the section
     * @param items the list of tech debt items
     * @param baseTicketUrl the base URL for the tickets
     */
    fun append(
        body: BODY,
        title: String,
        items: List<TechDebtItem>,
        baseTicketUrl: String? = null
    ) {
        if (items.isEmpty()) return

        body.h2 { +title }
        for (item in items) {
            cardGenerator.append(body = body, item = item, baseTicketUrl = baseTicketUrl)
        }
    }
}
