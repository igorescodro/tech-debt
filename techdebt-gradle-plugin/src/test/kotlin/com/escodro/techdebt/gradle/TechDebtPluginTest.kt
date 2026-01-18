package com.escodro.techdebt.gradle

import java.io.File
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

internal class TechDebtPluginTest {

    @TempDir
    lateinit var tempDir: File

    @Test
    fun `test plugin applies successfully`() {
        setupProject()

        val result = GradleRunner.create()
            .withProjectDir(tempDir)
            .withArguments("tasks", "--all")
            .withPluginClasspath()
            .build()

        assertTrue(result.output.contains("generateTechDebtReport"))
    }

    @Test
    fun `test generateTechDebtReport task is registered`() {
        setupProject()

        val result = GradleRunner.create()
            .withProjectDir(tempDir)
            .withArguments("tasks", "--all")
            .withPluginClasspath()
            .build()

        assertTrue(result.output.contains("generateTechDebtReport"))
    }

    @Test
    fun `test task generates consolidated report from JSON files`() {
        setupProject()

        // Create a mock JSON file
        val kspDir = File(tempDir, "build/generated/ksp/main/resources/techdebt")
        kspDir.mkdirs()
        File(kspDir, "report.json").writeText(
            """
            [
                {
                    "moduleName": ":app",
                    "name": "com.example.MyClass",
                    "description": "Test debt",
                    "ticket": "JIRA-123",
                    "priority": "HIGH"
                }
            ]
            """.trimIndent()
        )

        val result = GradleRunner.create()
            .withProjectDir(tempDir)
            .withArguments("generateTechDebtReport")
            .withPluginClasspath()
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":generateTechDebtReport")?.outcome)

        val reportFile = File(tempDir, "build/reports/techdebt/consolidated-report.html")
        assertTrue(reportFile.exists(), "Consolidated report should exist")

        val content = reportFile.readText()
        assertTrue(content.contains("Consolidated Tech Debt Report"))
        assertTrue(content.contains(":app"))
        assertTrue(content.contains("com.example.MyClass"))
        assertTrue(content.contains("Test debt"))
        assertTrue(content.contains("JIRA-123"))
        assertTrue(content.contains("HIGH"))
    }

    @Test
    fun `test report contains items from multiple modules`() {
        setupProject()

        // Create JSON files for multiple modules
        val module1Dir = File(tempDir, "module1/build/generated/ksp/main/resources/techdebt")
        module1Dir.mkdirs()
        File(module1Dir, "report.json").writeText(
            """
            [
                {
                    "moduleName": ":module1",
                    "name": "Module1Class",
                    "description": "Module 1 debt",
                    "ticket": "JIRA-001",
                    "priority": "HIGH"
                }
            ]
            """.trimIndent()
        )

        val module2Dir = File(tempDir, "module2/build/generated/ksp/main/resources/techdebt")
        module2Dir.mkdirs()
        File(module2Dir, "report.json").writeText(
            """
            [
                {
                    "moduleName": ":module2",
                    "name": "Module2Class",
                    "description": "Module 2 debt",
                    "ticket": "JIRA-002",
                    "priority": "LOW"
                }
            ]
            """.trimIndent()
        )

        // Update settings to include subprojects
        File(tempDir, "settings.gradle.kts").writeText(
            """
            rootProject.name = "test-project"
            include(":module1")
            include(":module2")
            """.trimIndent()
        )

        // Create minimal build files for subprojects
        File(tempDir, "module1").mkdirs()
        File(tempDir, "module1/build.gradle.kts").writeText("")
        File(tempDir, "module2").mkdirs()
        File(tempDir, "module2/build.gradle.kts").writeText("")

        val result = GradleRunner.create()
            .withProjectDir(tempDir)
            .withArguments("generateTechDebtReport")
            .withPluginClasspath()
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":generateTechDebtReport")?.outcome)

        val reportFile = File(tempDir, "build/reports/techdebt/consolidated-report.html")
        assertTrue(reportFile.exists(), "Consolidated report should exist")

        val content = reportFile.readText()
        assertTrue(content.contains(":module1"))
        assertTrue(content.contains(":module2"))
        assertTrue(content.contains("Module1Class"))
        assertTrue(content.contains("Module2Class"))
    }

    @Test
    fun `test items are ordered by module then priority`() {
        setupProject()

        val kspDir = File(tempDir, "build/generated/ksp/main/resources/techdebt")
        kspDir.mkdirs()
        File(kspDir, "report.json").writeText(
            """
            [
                {
                    "moduleName": ":b-module",
                    "name": "BClass",
                    "description": "B debt",
                    "ticket": "",
                    "priority": "LOW"
                },
                {
                    "moduleName": ":a-module",
                    "name": "AClassLow",
                    "description": "A low debt",
                    "ticket": "",
                    "priority": "LOW"
                },
                {
                    "moduleName": ":a-module",
                    "name": "AClassHigh",
                    "description": "A high debt",
                    "ticket": "",
                    "priority": "HIGH"
                }
            ]
            """.trimIndent()
        )

        val result = GradleRunner.create()
            .withProjectDir(tempDir)
            .withArguments("generateTechDebtReport")
            .withPluginClasspath()
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":generateTechDebtReport")?.outcome)

        val reportFile = File(tempDir, "build/reports/techdebt/consolidated-report.html")
        val content = reportFile.readText()

        // Verify ordering: :a-module items should come before :b-module
        // And within :a-module, HIGH priority should come before LOW
        val aModuleHighIndex = content.indexOf("AClassHigh")
        val aModuleLowIndex = content.indexOf("AClassLow")
        val bModuleIndex = content.indexOf("BClass")

        assertTrue(aModuleHighIndex < aModuleLowIndex, "HIGH priority should come before LOW in same module")
        assertTrue(aModuleLowIndex < bModuleIndex, ":a-module should come before :b-module")
    }

    private fun setupProject() {
        File(tempDir, "settings.gradle.kts").writeText(
            """
            rootProject.name = "test-project"
            """.trimIndent()
        )

        File(tempDir, "build.gradle.kts").writeText(
            """
            plugins {
                id("io.github.igorescodro.techdebt")
            }
            """.trimIndent()
        )
    }
}
