package com.escodro.techdebt.gradle.report

import com.escodro.techdebt.gradle.model.TechDebtItem
import com.escodro.techdebt.gradle.model.TechDebtItemType
import java.io.StringWriter
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class HtmlReportGeneratorTest {

    private val generator = HtmlReportGenerator()

    @Test
    fun `test generator creates HTML with correct summary counts`() {
        val items =
            listOf(
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
        val items =
            listOf(
                TechDebtItem(
                    moduleName = ":my-module",
                    name = "MyClass",
                    description = "Fix this later",
                    ticket = "PROJ-456",
                    priority = "HIGH",
                    sourceSet = "main"
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

    @Test
    fun `test generator creates HTML with suppressed rules section`() {
        val items =
            listOf(
                createItem(module = "A", priority = "HIGH"),
                createItem(
                    module = "B",
                    priority = "NONE",
                    type = TechDebtItemType.SUPPRESS,
                    description = "Rule1"
                )
            )

        val writer = StringWriter()
        generator.generate(writer, items)
        val html = writer.toString()

        assertTrue(html.contains("Annotated Tech Debt"))
        assertTrue(html.contains("Suppressed Rules"))
        assertTrue(html.contains("Rule1"))
        // Suppressed item should be counted in the summary total now
        assertTrue(html.contains("<h2>2</h2>"), "Should contain total 2 in summary total")
        assertTrue(html.contains("Suppressed"), "Should contain Suppressed in summary")
    }

    @Test
    fun `test generator creates link when base ticket URL is provided`() {
        val items =
            listOf(
                TechDebtItem(
                    moduleName = ":my-module",
                    name = "MyClass",
                    description = "Fix this later",
                    ticket = "PROJ-456",
                    priority = "HIGH",
                    sourceSet = "main"
                )
            )

        val writer = StringWriter()
        generator.generate(writer, items, baseTicketUrl = "https://jira.myproject.com/tickets/")
        val html = writer.toString()

        assertTrue(
            html.contains(
                "<a href=\"https://jira.myproject.com/tickets/PROJ-456\" " +
                    "target=\"_blank\" rel=\"noopener noreferrer\">PROJ-456</a>"
            ),
            "Should contain the ticket as a link with rel=\"noopener noreferrer\""
        )
    }

    @Test
    fun `test generator creates link when base ticket URL is provided without trailing slash`() {
        val items =
            listOf(
                TechDebtItem(
                    moduleName = ":my-module",
                    name = "MyClass",
                    description = "Fix this later",
                    ticket = "PROJ-456",
                    priority = "HIGH",
                    sourceSet = "main"
                )
            )

        val writer = StringWriter()
        generator.generate(writer, items, baseTicketUrl = "https://jira.myproject.com/tickets")
        val html = writer.toString()

        assertTrue(
            html.contains(
                "<a href=\"https://jira.myproject.com/tickets/PROJ-456\" " +
                    "target=\"_blank\" rel=\"noopener noreferrer\">PROJ-456</a>"
            ),
            "Should contain the ticket as a link even without trailing slash in base URL and " +
                "with rel=\"noopener noreferrer\""
        )
    }

    @Test
    fun `test generator includes expand all and collapse all buttons`() {
        val writer = StringWriter()
        generator.generate(writer, emptyList())
        val html = writer.toString()

        assertTrue(html.contains("Expand All"))
        assertTrue(html.contains("Collapse All"))
    }

    @Test
    fun `test generator counts comments and suppressed items in summary`() {
        val items =
            listOf(
                createItem(module = "A", priority = "HIGH", type = TechDebtItemType.TECH_DEBT),
                createItem(module = "A", priority = "NONE", type = TechDebtItemType.COMMENT),
                createItem(module = "B", priority = "NONE", type = TechDebtItemType.COMMENT),
                createItem(module = "C", priority = "NONE", type = TechDebtItemType.SUPPRESS)
            )

        val writer = StringWriter()
        generator.generate(writer, items)
        val html = writer.toString()

        assertTrue(html.contains("<h2>4</h2>"), "Total items should be 4")
        assertTrue(html.contains("<h2>1</h2>"), "High priority should be 1")
        assertTrue(html.contains("<h2>2</h2>"), "Comments should be 2")
        assertTrue(html.contains("<h2>1</h2>"), "Suppressed should be 1")
        assertTrue(html.contains("Comments"), "Label 'Comments' should be present")
        assertTrue(html.contains("Suppressed"), "Label 'Suppressed' should be present")
    }

    private fun createItem(
        module: String,
        priority: String,
        type: TechDebtItemType = TechDebtItemType.TECH_DEBT,
        description: String = "description"
    ) =
        TechDebtItem(
            moduleName = module,
            name = "name",
            description = description,
            ticket = "ticket",
            priority = priority,
            sourceSet = "main",
            type = type
        )
}
