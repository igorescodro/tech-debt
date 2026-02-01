package com.escodro.techdebt.gradle.report

import com.escodro.techdebt.gradle.model.TechDebtItem
import com.escodro.techdebt.gradle.model.TechDebtItemType
import java.io.Writer
import kotlinx.html.BODY
import kotlinx.html.ButtonType
import kotlinx.html.HTML
import kotlinx.html.body
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.script
import kotlinx.html.stream.appendHTML
import kotlinx.html.unsafe

/** Generates a consolidated HTML report with tech debt items from all modules. */
internal class HtmlReportGenerator(
    private val reportStyle: ReportStyle = ReportStyle(),
    private val summaryGenerator: SummaryGenerator = SummaryGenerator(),
    private val sectionGenerator: SectionGenerator = SectionGenerator(CardGenerator())
) {

    /**
     * Generates a consolidated HTML report with the tech debt items.
     *
     * @param writer the writer to output the HTML content
     * @param items the list of tech debt items to include in the report
     * @param baseTicketUrl the base URL for the tickets
     */
    fun generate(writer: Writer, items: List<TechDebtItem>, baseTicketUrl: String? = null) {
        val (techDebtItems, suppressedItems, commentItems) = filterItems(items)
        val summary = calculateSummary(techDebtItems)

        writer.appendHTML().html {
            appendHead(this)
            appendBody(
                html = this,
                summary = summary,
                techDebtItems = techDebtItems,
                suppressedItems = suppressedItems,
                commentItems = commentItems,
                baseTicketUrl = baseTicketUrl
            )
        }
    }

    private fun filterItems(
        items: List<TechDebtItem>
    ): Triple<List<TechDebtItem>, List<TechDebtItem>, List<TechDebtItem>> {
        val techDebtItems = items.filter { it.type == TechDebtItemType.TECH_DEBT }
        val suppressedItems = items.filter { it.type == TechDebtItemType.SUPPRESS }
        val commentItems = items.filter { it.type == TechDebtItemType.COMMENT }
        return Triple(techDebtItems, suppressedItems, commentItems)
    }

    private fun calculateSummary(techDebtItems: List<TechDebtItem>): SummaryData =
        SummaryData(
            total = techDebtItems.size,
            high = techDebtItems.count { it.priority == "HIGH" },
            medium = techDebtItems.count { it.priority == "MEDIUM" },
            low = techDebtItems.count { it.priority == "LOW" },
            none = techDebtItems.count { it.priority == "NONE" }
        )

    private fun appendHead(html: HTML) {
        html.head {
            reportStyle.append(this)
            script {
                unsafe {
                    +"""
                        function expandAll() {
                            document.querySelectorAll('details').forEach(d => d.open = true);
                        }
                        function collapseAll() {
                            document.querySelectorAll('details').forEach(d => d.open = false);
                        }
                        """
                        .trimIndent()
                }
            }
        }
    }

    @Suppress("LongParameterList")
    private fun appendBody(
        html: HTML,
        summary: SummaryData,
        techDebtItems: List<TechDebtItem>,
        suppressedItems: List<TechDebtItem>,
        commentItems: List<TechDebtItem>,
        baseTicketUrl: String?
    ) {
        html.body {
            h1 { +"Tech Debt Report" }

            summaryGenerator.append(
                body = this,
                totalItems = summary.total,
                highItems = summary.high,
                mediumItems = summary.medium,
                lowItems = summary.low,
                noneItems = summary.none
            )

            appendActionButtons(this)

            sectionGenerator.append(
                body = this,
                title = "Annotated Tech Debt",
                items = techDebtItems,
                baseTicketUrl = baseTicketUrl
            )

            sectionGenerator.append(
                body = this,
                title = "Comments",
                items = commentItems,
                baseTicketUrl = baseTicketUrl
            )

            sectionGenerator.append(
                body = this,
                title = "Suppressed Rules",
                items = suppressedItems,
                baseTicketUrl = baseTicketUrl
            )
        }
    }

    private fun appendActionButtons(body: BODY) {
        body.div(classes = "action-container") {
            button(classes = "action-button", type = ButtonType.button) {
                attributes["onclick"] = "expandAll()"
                +"Expand All"
            }
            button(classes = "action-button", type = ButtonType.button) {
                attributes["onclick"] = "collapseAll()"
                +"Collapse All"
            }
        }
    }

    private data class SummaryData(
        val total: Int,
        val high: Int,
        val medium: Int,
        val low: Int,
        val none: Int
    )
}
