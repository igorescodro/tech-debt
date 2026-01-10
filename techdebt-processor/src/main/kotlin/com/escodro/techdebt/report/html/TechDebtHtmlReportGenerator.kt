package com.escodro.techdebt.report.html

import com.escodro.techdebt.report.TechDebtItem
import kotlinx.html.body
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.span
import kotlinx.html.stream.appendHTML
import kotlinx.html.style
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.tr
import kotlinx.html.unsafe
import java.io.Writer

/**
 * Generates an HTML report with the tech debt items.
 */
internal class TechDebtReportGenerator {

    /**
     * Generates an HTML report with the tech debt items.
     */
    fun generate(writer: Writer, items: List<TechDebtItem>) {
        writer.appendHTML().html {
            head {
                style {
                    unsafe {
                        +TechDebtItemStyle.trimIndent()
                    }
                }
            }
            body {
                unsafe {
                    +"<h1>Tech Debt Report</h1>"
                }
                table {
                    thead {
                        tr {
                            th { +"Symbol" }
                            th { +"Description" }
                            th { +"Ticket" }
                            th { +"Priority" }
                        }
                    }
                    tbody {
                        for (item in items) {
                            tr {
                                td {
                                    unsafe {
                                        +"<strong>${item.name}</strong>"
                                    }
                                }
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
    }
}

private const val TechDebtItemStyle = """
    body {
        font-family: sans-serif;
        background-color: #f3f3f3;
        margin: 0;
        padding: 20px;
    }
    h1 {
        color: #333;
    }
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
