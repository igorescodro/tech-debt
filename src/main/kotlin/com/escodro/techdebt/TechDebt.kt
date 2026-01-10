package com.escodro.techdebt

@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    )
@Retention(AnnotationRetention.SOURCE)
annotation class TechDebt(
    val ticket: String = "",
    val description: String = "",
    val priority: Priority = Priority.NONE
)

enum class Priority {
    NONE,
    LOW,
    MEDIUM,
    HIGH,
}
