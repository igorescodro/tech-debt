package com.escodro.techdebt.report

import kotlinx.serialization.Serializable

/**
 * Represents a technical debt item with its details.
 *
 * @property moduleName the module name where the tech debt is located
 * @property name the tech debt name
 * @property description the tech debt description
 * @property ticket the ticket reference
 * @property priority the priority of the tech debt
 * @property sourceSet the source set where the tech debt is located
 * @property type the type of the tech debt item
 */
@Serializable
data class TechDebtItem(
    val moduleName: String,
    val name: String,
    val description: String,
    val ticket: String,
    val priority: String,
    val sourceSet: String,
    val type: TechDebtItemType = TechDebtItemType.TECH_DEBT,
    val date: String? = null
)

/** Represents the type of technical debt item. */
@Serializable
enum class TechDebtItemType {
    /** Item collected from @TechDebt annotation. */
    TECH_DEBT,

    /** Item collected from @Suppress annotation. */
    SUPPRESS,
}
