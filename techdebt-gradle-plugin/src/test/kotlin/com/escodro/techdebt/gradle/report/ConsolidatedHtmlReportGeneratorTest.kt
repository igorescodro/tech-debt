package com.escodro.techdebt.gradle.report

import com.escodro.techdebt.gradle.model.TechDebtItem
import java.io.StringWriter
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class ConsolidatedHtmlReportGeneratorTest {

    private val generator = ConsolidatedHtmlReportGenerator()

    @Test
    fun `test generator creates HTML with correct summary counts`() {
        val items = listOf(
            createItem(module = "A", priority = "HIGH"),
            createItem(module = "A", priority = "MEDIUM"),
            createItem(module = "B", priority = "HIGH"),
            createItem(module = "C", priority = "LOW"),
            createItem(module = "C", priority = "NONE")
        )

        val writer = StringWriter()
        generator.generate(writer, items)
        val html = writer.toString()

        // Summary checks
        assertTrue(html.contains("Total Items"), "Should contain Total Items")
        assertTrue(html.contains("High Priority"), "Should contain High Priority")
        assertTrue(html.contains("5"), "Should contain total count 5")
        assertTrue(html.contains("2"), "Should contain high priority count 2")
    }

    @Test
    fun `test generator includes item details`() {
        val items = listOf(
            TechDebtItem(
                moduleName = ":my-module",
                name = "MyClass",
                description = "Fix this later",
                ticket = "PROJ-456",
                priority = "HIGH"
            )
        )

        val writer = StringWriter()
        generator.generate(writer, items)
        val html = writer.toString()

        assertTrue(html.contains(":my-module"))
        assertTrue(html.contains("MyClass"))
        assertTrue(html.contains("Fix this later"))
        assertTrue(html.contains("PROJ-456"))
        assertTrue(html.contains("HIGH"))
    }

    private fun createItem(module: String, priority: String) = TechDebtItem(
        moduleName = module,
        name = "name",
        description = "description",
        ticket = "ticket",
        priority = priority
    )
}
