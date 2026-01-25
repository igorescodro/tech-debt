@file:TechDebt(description = "Too many functions", priority = Priority.HIGH)

package com.escodro.sample

import com.escodro.techdebt.Priority
import com.escodro.techdebt.TechDebt

@TechDebt(ticket = "TD-1", description = "This class needs a better name", priority = Priority.LOW)
class Sample {

    @TechDebt(
        ticket = "TD-2",
        description = "This property should be private",
        priority = Priority.MEDIUM
    )
    val someProperty: String = "Hello"

    @TechDebt(
        ticket = "TD-3",
        description = "This function is too complex",
        priority = Priority.HIGH
    )
    fun someComplexFunction() {
        println("Doing something complex")
    }

    @TechDebt(description = "Legacy code, handle with care")
    fun legacyFunction() {
        println("Legacy logic")
    }

    @Suppress("UNUSED_PARAMETER")
    fun suppressedFunction() {
        println("Suppressed function")
    }
}
