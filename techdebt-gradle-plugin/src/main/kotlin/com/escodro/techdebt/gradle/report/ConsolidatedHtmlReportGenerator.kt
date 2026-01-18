package com.escodro.techdebt.gradle.report

import com.escodro.techdebt.gradle.model.TechDebtItem
import java.io.Writer
import kotlinx.html.BODY
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.span
import kotlinx.html.stream.appendHTML
import kotlinx.html.strong
import kotlinx.html.style
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.tr
import kotlinx.html.unsafe

/**
 * Generates a consolidated HTML report with tech debt items from all modules.
 */
internal class ConsolidatedHtmlReportGenerator {

    /**
     * Generates a consolidated HTML report with the tech debt items.
     *
     * @param writer the writer to output the HTML content
     * @param items the list of tech debt items to include in the report
     */
    fun generate(writer: Writer, items: List<TechDebtItem>) {
        val totalItems = items.size
        val highItems = items.count { it.priority == "HIGH" }
        val mediumItems = items.count { it.priority == "MEDIUM" }
        val lowItems = items.count { it.priority == "LOW" }
        val noneItems = items.count { it.priority == "NONE" }

        writer.appendHTML().html {
            head { style { unsafe { +CONSOLIDATED_REPORT_STYLE.trimIndent() } } }
            body {
                h1 { +"Tech Debt Report" }

                header(
                    totalItems = totalItems,
                    highItems = highItems,
                    mediumItems = mediumItems,
                    lowItems = lowItems,
                    noneItems = noneItems
                )

                table(items = items)
            }
        }
    }

    private fun BODY.header(
        totalItems: Int,
        highItems: Int,
        mediumItems: Int,
        lowItems: Int,
        noneItems: Int
    ) {
        div(classes = "summary-container") {
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
        }
    }

    private fun BODY.table(items: List<TechDebtItem>) {
        table {
            thead {
                tr {
                    th { +"Module" }
                    th { +"Symbol" }
                    th { +"Description" }
                    th { +"Ticket" }
                    th { +"Priority" }
                }
            }
            tbody {
                for (item in items) {
                    tr {
                        td { +item.moduleName }
                        td { strong { +item.name } }
                        td { +item.description }
                        td {
                            if (item.ticket.isNotEmpty()) {
                                span(classes = "ticket") { +item.ticket }
                            }
                        }
                        td { +item.priority }
                    }
                }
            }
        }
    }
}

private const val CONSOLIDATED_REPORT_STYLE =
    """
    body {
        font-family: sans-serif;
        background-color: #f3f3f3;
        margin: 0;
        padding: 20px;
    }
    h1 {
        color: #333;
    }
    .summary-container {
        display: flex;
        gap: 20px;
        margin-bottom: 30px;
        flex-wrap: wrap;
    }
    .summary-box {
        flex: 1;
        min-width: 120px;
        padding: 20px;
        border-radius: 8px;
        color: white;
        text-align: center;
        box-shadow: 0 2px 5px rgba(0,0,0,0.1);
    }
    .summary-box h2 {
        margin: 0;
        font-size: 32px;
    }
    .summary-box span {
        font-size: 14px;
        text-transform: uppercase;
        letter-spacing: 1px;
    }
    .total { background-color: #4A90E2; }
    .high { background-color: #E35D5D; }
    .medium { background-color: #F5A623; }
    .low { background-color: #4CAF50; }
    .none { background-color: #9E9E9E; }
    table {
        width: 100%;
        border-collapse: collapse;
        background-color: #fff;
        box-shadow: 0 2px 5px rgba(0,0,0,0.1);
        border-radius: 8px;
        overflow: hidden;
    }
    th, td {
        padding: 12px 15px;
        text-align: left;
        border-bottom: 1px solid #ddd;
    }
    th {
        background-color: #4CAF50;
        color: white;
        text-transform: uppercase;
        letter-spacing: 0.1em;
        font-size: 14px;
    }
    tr:hover {
        background-color: #f5f5f5;
    }
    .ticket {
        font-family: monospace;
        background-color: #eee;
        padding: 2px 4px;
        border-radius: 4px;
    }
"""
