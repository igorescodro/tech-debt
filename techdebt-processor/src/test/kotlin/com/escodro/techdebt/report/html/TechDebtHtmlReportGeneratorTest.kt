package com.escodro.techdebt.report.html

import com.escodro.techdebt.report.TechDebtItem
import java.io.StringWriter
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TechDebtHtmlReportGeneratorTest {

    private val reportGenerator = TechDebtHtmlReportGenerator()

    @Test
    fun `test if the report is generated with the correct items`() {
        val writer = StringWriter()
        val items =
            listOf(
                TechDebtItem(
                    name = "com.escodro.techdebt.MyClass",
                    description = "My description",
                    ticket = "JIRA-123",
                    priority = "HIGH"
                ),
                TechDebtItem(
                    name = "com.escodro.techdebt.MyFunction",
                    description = "Another description",
                    ticket = "JIRA-456",
                    priority = "LOW"
                )
            )

        reportGenerator.generate(writer, items)

        val report = writer.toString()
        assertTrue(report.contains("Tech Debt Report"))
        assertTrue(report.contains("com.escodro.techdebt.MyClass"))
        assertTrue(report.contains("My description"))
        assertTrue(report.contains("JIRA-123"))
        assertTrue(report.contains("HIGH"))
        assertTrue(report.contains("com.escodro.techdebt.MyFunction"))
        assertTrue(report.contains("Another description"))
        assertTrue(report.contains("JIRA-456"))
        assertTrue(report.contains("LOW"))
    }

    @Test
    fun `test if the summary is correct`() {
        val writer = StringWriter()
        val items =
            listOf(
                TechDebtItem("Item 1", "", "", "HIGH"),
                TechDebtItem("Item 2", "", "", "HIGH"),
                TechDebtItem("Item 3", "", "", "MEDIUM"),
                TechDebtItem("Item 4", "", "", "LOW"),
                TechDebtItem("Item 5", "", "", "NONE")
            )

        reportGenerator.generate(writer, items)

        val report = writer.toString().replace("\n", "")
        assertTrue(report.contains("<h2>5</h2><span>Total Items</span>"))
        assertTrue(report.contains("<h2>2</h2><span>High Priority</span>"))
        assertTrue(report.contains("<h2>1</h2><span>Medium Priority</span>"))
        assertTrue(report.contains("<h2>1</h2><span>Low Priority</span>"))
        assertTrue(report.contains("<h2>1</h2><span>No Priority</span>"))
    }
}
