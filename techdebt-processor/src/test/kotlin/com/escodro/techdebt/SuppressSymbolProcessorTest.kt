package com.escodro.techdebt

import com.escodro.techdebt.utils.TestProject
import java.io.File
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

internal class SuppressSymbolProcessorTest {

    @TempDir lateinit var tempDir: File

    @Test
    fun `test if the processor collects suppressed rules when enabled`() {
        val testProject = TestProject(tempDir)
        testProject.setupBuild()
        // Add collectSuppress arg correctly
        File(tempDir, "build.gradle.kts")
            .appendText(
                """
            
            configure<com.google.devtools.ksp.gradle.KspExtension> {
                arg("collectSuppress", "true")
            }
            """
                    .trimIndent()
            )
        testProject.addSource(
            "MyClass.kt",
            """
            package com.escodro.techdebt
            
            @Suppress("UnusedPrivateMember", "MagicNumber")
            class MyClass {
                @Suppress("Deprecation")
                fun myFunc() {}
            }
            """
                .trimIndent()
        )

        testProject.build()

        val jsonFile = testProject.getJsonReportFile()
        val content = jsonFile.readText()
        assertTrue(content.contains("UnusedPrivateMember"))
        assertTrue(content.contains("MagicNumber"))
        assertTrue(content.contains("Deprecation"))
        assertTrue(content.contains("\"type\": \"SUPPRESS\""))
    }

    @Test
    fun `test if the processor collects multiple suppressed rules in a single annotation`() {
        val testProject = TestProject(tempDir)
        testProject.setupBuild()
        File(tempDir, "build.gradle.kts")
            .appendText(
                """
            
            configure<com.google.devtools.ksp.gradle.KspExtension> {
                arg("collectSuppress", "true")
            }
            """
                    .trimIndent()
            )
        testProject.addSource(
            "MyClass.kt",
            """
            package com.escodro.techdebt
            
            @Suppress("MagicNumber", "LongMethod", "MaxLineLength")
            class MyClass
            """
                .trimIndent()
        )

        testProject.build()

        val jsonFile = testProject.getJsonReportFile()
        val content = jsonFile.readText()
        assertTrue(content.contains("MagicNumber"), "Should contain MagicNumber")
        assertTrue(content.contains("LongMethod"), "Should contain LongMethod")
        assertTrue(content.contains("MaxLineLength"), "Should contain MaxLineLength")

        // Check if all three are present as separate items
        val suppressCount = "\"type\": \"SUPPRESS\"".toRegex().findAll(content).count()
        assertTrue(
            suppressCount >= 3,
            "Expected at least 3 suppressed items, but found $suppressCount"
        )
    }

    @Test
    fun `test if the processor collects suppressed rules from multiple annotations`() {
        val testProject = TestProject(tempDir)
        testProject.setupBuild()
        File(tempDir, "build.gradle.kts")
            .appendText(
                """
            
            configure<com.google.devtools.ksp.gradle.KspExtension> {
                arg("collectSuppress", "true")
            }
            """
                    .trimIndent()
            )
        testProject.addSource(
            "MyClass.kt",
            """
            package com.escodro.techdebt
            
            @Suppress("MagicNumber")
            @Suppress("LongMethod")
            class MyClass
            """
                .trimIndent()
        )

        testProject.build()

        val jsonFile = testProject.getJsonReportFile()
        val content = jsonFile.readText()
        assertTrue(content.contains("MagicNumber"), "Should contain MagicNumber")
        assertTrue(content.contains("LongMethod"), "Should contain LongMethod")

        val suppressCount = "\"type\": \"SUPPRESS\"".toRegex().findAll(content).count()
        assertTrue(
            suppressCount >= 2,
            "Expected at least 2 suppressed items, but found $suppressCount"
        )
    }

    @Test
    fun `test if the processor does not collect suppressed rules by default`() {
        val testProject = TestProject(tempDir)
        testProject.setupBuild()
        testProject.addSource(
            "MyClass.kt",
            """
            package com.escodro.techdebt
            
            @Suppress("UnusedPrivateMember")
            class MyClass
            """
                .trimIndent()
        )

        testProject.build()

        val jsonFile = testProject.getJsonReportFile()
        assertTrue(!jsonFile.exists() || !jsonFile.readText().contains("UnusedPrivateMember"))
    }
}
