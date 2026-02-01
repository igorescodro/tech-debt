package com.escodro.techdebt.gradle.report

import com.escodro.techdebt.gradle.model.TechDebtItem
import com.escodro.techdebt.gradle.model.TechDebtItemType
import java.io.StringWriter
import kotlinx.html.body
import kotlinx.html.html
import kotlinx.html.stream.appendHTML
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class CardGeneratorTest {

    private val generator = CardGenerator()

    @Test
    fun `test card contains header information for tech debt`() {
        val item =
            createItem(
                module = ":app",
                name = "MyClass",
                description = "Fix this",
                type = TechDebtItemType.TECH_DEBT
            )

        val html = generateHtml { generator.append(this, item) }

        assertTrue(html.contains(":app"))
        assertTrue(html.contains("MyClass"))
        assertTrue(html.contains("Fix this"))
    }

    @Test
    fun `test card contains header information for comment`() {
        val item =
            createItem(module = ":data", description = "TODO: fix", type = TechDebtItemType.COMMENT)

        val html = generateHtml { generator.append(this, item) }

        assertTrue(html.contains(":data"))
        assertTrue(html.contains("Comment: TODO: fix"))
    }

    @Test
    fun `test card contains header information for suppressed rules`() {
        val item =
            createItem(
                module = ":domain",
                name = "Function",
                description = "ComplexMethod",
                type = TechDebtItemType.SUPPRESS
            )

        val html = generateHtml { generator.append(this, item) }

        assertTrue(html.contains(":domain"))
        assertTrue(html.contains("Function"))
        assertTrue(html.contains("Rule: ComplexMethod"))
    }

    @Test
    fun `test card contains ticket link when base ticket URL is provided`() {
        val item = createItem(ticket = "PROJ-1")
        val baseUrl = "https://jira.com/"

        val html = generateHtml { generator.append(this, item, baseUrl) }

        assertTrue(html.contains("href=\"https://jira.com/PROJ-1\""))
    }

    private fun generateHtml(block: kotlinx.html.BODY.() -> Unit): String {
        val writer = StringWriter()
        writer.appendHTML().html { body { block() } }
        return writer.toString()
    }

    private fun createItem(
        module: String = "module",
        name: String = "name",
        description: String = "description",
        ticket: String = "",
        type: TechDebtItemType = TechDebtItemType.TECH_DEBT
    ) =
        TechDebtItem(
            moduleName = module,
            name = name,
            description = description,
            ticket = ticket,
            priority = "HIGH",
            sourceSet = "main",
            type = type
        )
}
