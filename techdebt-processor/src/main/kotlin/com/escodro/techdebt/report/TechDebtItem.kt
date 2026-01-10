package com.escodro.techdebt.report

/**
 * Represents a technical debt item with its details.
 *
 * @property name the tech debt name
 * @property description the tech debt description
 * @property ticket the ticket reference
 * @property priority the priority of the tech debt
 */
internal data class TechDebtItem(
    val name: String,
    val description: String,
    val ticket: String,
    val priority: String
)
