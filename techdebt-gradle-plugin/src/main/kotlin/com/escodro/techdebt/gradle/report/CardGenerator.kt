package com.escodro.techdebt.gradle.report

import com.escodro.techdebt.gradle.model.TechDebtItem
import com.escodro.techdebt.gradle.model.TechDebtItemType
import kotlinx.html.BODY
import kotlinx.html.FlowContent
import kotlinx.html.a
import kotlinx.html.details
import kotlinx.html.div
import kotlinx.html.span
import kotlinx.html.summary

/** Generates the card for an individual tech debt item. */
internal class CardGenerator {

    /**
     * Appends a tech debt item card to the BODY element.
     *
     * @param body the BODY element to append the card
     * @param item the tech debt item
     * @param baseTicketUrl the base URL for the tickets
     */
    fun append(body: BODY, item: TechDebtItem, baseTicketUrl: String? = null) {
        body.div(classes = "card") {
            details {
                summary {
                    div(classes = "card-header") {
                        div(classes = "card-header-main") {
                            div(classes = "header-column column-small") {
                                span(classes = "module-badge") { +item.moduleName }
                            }
                            if (item.type != TechDebtItemType.COMMENT) {
                                div(classes = "header-column column-medium") {
                                    span(classes = "symbol-name") { +item.name }
                                }
                            }
                            div(classes = "header-column") {
                                span {
                                    when (item.type) {
                                        TechDebtItemType.COMMENT -> +"Comment: ${item.description}"
                                        TechDebtItemType.SUPPRESS -> +"Rule: ${item.description}"
                                        else -> +item.description
                                    }
                                }
                            }
                        }
                    }
                    span(classes = "expand-icon")
                }
                div(classes = "card-content") {
                    if (item.ticket.isNotEmpty()) {
                        infoGroup("Ticket") {
                            ticket(ticket = item.ticket, baseTicketUrl = baseTicketUrl)
                        }
                    }
                    if (item.type == TechDebtItemType.TECH_DEBT) {
                        infoGroup("Priority") { +item.priority }
                    }
                    infoGroup(
                        if (item.type == TechDebtItemType.COMMENT) "Location" else "Source Set"
                    ) {
                        +item.sourceSet
                    }
                    if (item.type == TechDebtItemType.SUPPRESS) {
                        infoGroup("Symbol") { +item.name }
                    }
                    if (item.lastModified != null) {
                        infoGroup("Last Modified") { +item.lastModified }
                    }
                    if (item.author != null) {
                        infoGroup("Author") { +item.author }
                    }
                }
            }
        }
    }

    private fun FlowContent.infoGroup(label: String, block: FlowContent.() -> Unit) {
        div(classes = "info-group") {
            span(classes = "info-label") { +label }
            div(classes = "info-value") {
                block()
            }
        }
    }

    private fun FlowContent.ticket(ticket: String, baseTicketUrl: String?) {
        if (baseTicketUrl != null) {
            val url =
                if (baseTicketUrl.endsWith("/")) {
                    "$baseTicketUrl$ticket"
                } else {
                    "$baseTicketUrl/$ticket"
                }
            a(href = url, target = "_blank") {
                attributes["rel"] = "noopener noreferrer"
                +ticket
            }
        } else {
            span(classes = "ticket") { +ticket }
        }
    }
}
