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
        get() =
            when (priority) {
                "HIGH" -> HIGH_PRIORITY
                "MEDIUM" -> MEDIUM_PRIORITY
                "LOW" -> LOW_PRIORITY
                else -> NO_PRIORITY
            }

    companion object {
        private const val HIGH_PRIORITY = 0
        private const val MEDIUM_PRIORITY = 1
        private const val LOW_PRIORITY = 2
        private const val NO_PRIORITY = 3
    }
}
