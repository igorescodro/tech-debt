package com.escodro.techdebt.gradle.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class TechDebtItemTest {

    @Test
    fun `test priority order`() {
        val high = createItem(priority = "HIGH")
        val medium = createItem(priority = "MEDIUM")
        val low = createItem(priority = "LOW")
        val none = createItem(priority = "NONE")
        val other = createItem(priority = "OTHER")

        assertEquals(0, high.priorityOrder)
        assertEquals(1, medium.priorityOrder)
        assertEquals(2, low.priorityOrder)
        assertEquals(3, none.priorityOrder)
        assertEquals(3, other.priorityOrder)
    }

    private fun createItem(priority: String) = TechDebtItem(
        moduleName = "module",
        name = "name",
        description = "description",
        ticket = "ticket",
        priority = priority
    )
}
