@file:TechDebt(description = "Too many functions", priority = Priority.HIGH)

package com.escodro.sample

import com.escodro.techdebt.Priority
import com.escodro.techdebt.TechDebt

@TechDebt(ticket = "20", description = "This class needs a better name", priority = Priority.LOW)
class Sample {

    @TechDebt(
        ticket = "23",
        description = "This property should be private",
        priority = Priority.MEDIUM
    )
    val someProperty: String = "Hello"

    @TechDebt(ticket = "26", description = "This function is too complex", priority = Priority.HIGH)
    fun someComplexFunction() {
        // TODO Refactor function when code is ready
        println("Doing something complex")
    }

    @TechDebt(description = "Legacy code, handle with care")
    fun legacyFunction() {
        println("Legacy logic")
        /* TODO Break legacy function */
    }

    @Suppress("UNUSED_PARAMETER")
    fun suppressedFunction() {
        println("Suppressed function")
    }

    /** TODO Simplify this function */
    fun normalFunction() {
        println("Normal function")
        /*
         * TODO reduce function complexity
         */
    }
}
