package com.escodro.techdebt.gradle.model

import kotlinx.serialization.Serializable

/**
 * Represents a technical debt item with its details.
 *
 * @property moduleName the module name where the tech debt is located
 * @property name the tech debt name
 * @property description the tech debt description
 * @property ticket the ticket reference
 * @property priority the priority of the tech debt
 */
@Serializable
data class TechDebtItem(
    val moduleName: String,
    val name: String,
    val description: String,
    val ticket: String,
    val priority: String,
    val sourceSet: String
) {
    /**
     * Returns the priority order for the tech debt item.
     *
     * @return the priority order
     */
    val priorityOrder: Int
        get() = when (priority) {
            "HIGH" -> 0
            "MEDIUM" -> 1
            "LOW" -> 2
            else -> 3
        }
}
