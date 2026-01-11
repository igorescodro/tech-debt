package com.escodro.techdebt

/**
 * Annotation used to mark technical debt in the codebase.
 *
 * @property description a brief description of the technical debt
 * @property ticket a link or reference to the ticket tracking this technical debt
 * @property priority the priority of the technical debt
 */
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
)
@Retention(AnnotationRetention.SOURCE)
annotation class TechDebt(
    val description: String = "",
    val ticket: String = "",
    val priority: Priority = Priority.NONE
)

/** Represents the priority of the technical debt. */
enum class Priority {
    /** No priority assigned. */
    NONE,

    /** Low-priority technical debt. */
    LOW,

    /** Medium priority technical debt. */
    MEDIUM,

    /** High-priority technical debt. */
    HIGH,
}
