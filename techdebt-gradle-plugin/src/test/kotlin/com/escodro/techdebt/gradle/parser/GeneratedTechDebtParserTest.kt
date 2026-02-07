package com.escodro.techdebt.gradle.parser

import java.io.File
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

internal class GeneratedTechDebtParserTest {

    @TempDir lateinit var tempDir: File

    private val parser = GeneratedTechDebtParser()

    @Test
    fun `test parser correctly parses KSP-generated JSON`() {
        val project = ProjectBuilder.builder().withProjectDir(tempDir).build()
        val kspDir = File(tempDir, "build/generated/ksp/main/resources/techdebt")
        kspDir.mkdirs()
        val jsonFile =
            File(kspDir, "report.json").apply {
                writeText(
                    """
                [
                    {
                        "moduleName": ":app",
                        "name": "com.example.MyClass",
                        "description": "Test debt",
                        "ticket": "JIRA-123",
                        "priority": "HIGH",
                        "sourceSet": "main"
                    }
                ]
                """
                        .trimIndent()
                )
            }

        val jsonFiles = project.files(jsonFile)
        val items = parser.parse(jsonFiles)

        assertEquals(1, items.size)
        val item = items.first()
        assertEquals(":app", item.moduleName)
        assertEquals("com.example.MyClass", item.name)
        assertEquals("Test debt", item.description)
        assertEquals("JIRA-123", item.ticket)
        assertEquals("HIGH", item.priority)
        assertEquals("main", item.sourceSet)
    }

    @Test
    fun `test parser resolves sourceSet from path when unknown`() {
        val project = ProjectBuilder.builder().withProjectDir(tempDir).build()
        val kspDir = File(tempDir, "build/generated/ksp/customSourceSet/resources/techdebt")
        kspDir.mkdirs()
        val jsonFile =
            File(kspDir, "report.json").apply {
                writeText(
                    """
                [
                    {
                        "moduleName": ":app",
                        "name": "com.example.MyClass",
                        "description": "Test debt",
                        "ticket": "JIRA-123",
                        "priority": "HIGH",
                        "sourceSet": "unknown"
                    }
                ]
                """
                        .trimIndent()
                )
            }

        val jsonFiles = project.files(jsonFile)
        val items = parser.parse(jsonFiles)

        assertEquals(1, items.size)
        assertEquals("customSourceSet", items.first().sourceSet)
    }

    @Test
    fun `test parser does not resolve sourceSet when it is a bare filename`() {
        val project = ProjectBuilder.builder().withProjectDir(tempDir).build()
        val kspDir = File(tempDir, "build/generated/ksp/main/resources/techdebt")
        kspDir.mkdirs()
        val jsonFile =
            File(kspDir, "report.json").apply {
                writeText(
                    """
                [
                    {
                        "moduleName": ":app",
                        "name": "com.example.MyClass",
                        "description": "Test debt",
                        "ticket": "JIRA-123",
                        "priority": "HIGH",
                        "sourceSet": "MyFile.kt"
                    }
                ]
                """
                        .trimIndent()
                )
            }

        val jsonFiles = project.files(jsonFile)
        val items = parser.parse(jsonFiles)

        assertEquals(1, items.size)
        assertEquals("MyFile.kt", items.first().sourceSet)
    }
}
