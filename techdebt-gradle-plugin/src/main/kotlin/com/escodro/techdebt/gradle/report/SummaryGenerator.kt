package com.escodro.techdebt.gradle.report

import kotlinx.html.BODY
import kotlinx.html.div
import kotlinx.html.h2
import kotlinx.html.span

/** Generates the summary header for the HTML report. */
internal class SummaryGenerator {

    /**
     * Appends the summary header to the BODY element.
     *
     * @param body the BODY element to append the header
     * @param totalItems the total number of items
     * @param highItems the number of high priority items
     * @param mediumItems the number of medium priority items
     * @param lowItems the number of low priority items
     * @param noneItems the number of items without priority
     * @param commentItems the number of comment items
     * @param suppressedItems the number of suppressed items
     */
    @Suppress("LongParameterList")
    fun append(
        body: BODY,
        totalItems: Int,
        highItems: Int,
        mediumItems: Int,
        lowItems: Int,
        noneItems: Int,
        commentItems: Int,
        suppressedItems: Int
    ) {
        body.div(classes = "summary-container") {
            div(classes = "summary-box total") {
                h2 { +totalItems.toString() }
                span { +"Total Items" }
            }
            div(classes = "summary-box high") {
                h2 { +highItems.toString() }
                span { +"High Priority" }
            }
            div(classes = "summary-box medium") {
                h2 { +mediumItems.toString() }
                span { +"Medium Priority" }
            }
            div(classes = "summary-box low") {
                h2 { +lowItems.toString() }
                span { +"Low Priority" }
            }
            div(classes = "summary-box none") {
                h2 { +noneItems.toString() }
                span { +"No Priority" }
            }
            div(classes = "summary-box comments") {
                h2 { +commentItems.toString() }
                span { +"Comments" }
            }
            div(classes = "summary-box suppressed") {
                h2 { +suppressedItems.toString() }
                span { +"Suppressed" }
            }
        }
    }
}
