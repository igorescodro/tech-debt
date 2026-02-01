package com.escodro.techdebt

import com.escodro.techdebt.utils.TestProject
import java.io.File
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

internal class TechDebtSymbolProcessorIntegrationTest {

    @TempDir lateinit var tempDir: File

    @Test
    fun `test if the processor generates JSON report file`() {
        val testProject = TestProject(tempDir)
        testProject.setupBuild()
        testProject.addSource(
            "MyClass.kt",
            """
            package com.escodro.techdebt
            
            import com.escodro.techdebt.TechDebt
            import com.escodro.techdebt.Priority
            
            @TechDebt(
                ticket = "JIRA-123",
                description = "My description",
                priority = Priority.HIGH
            )
            class MyClass
            """
                .trimIndent()
        )

        testProject.build()

        val jsonFile = testProject.getJsonReportFile()
        assertTrue(jsonFile.exists(), "JSON report file should exist")

        val content = jsonFile.readText()
        assertTrue(content.contains("\"moduleName\""))
        assertTrue(content.contains("\"name\""))
        assertTrue(content.contains("MyClass"))
        assertTrue(content.contains("My description"))
        assertTrue(content.contains("JIRA-123"))
        assertTrue(content.contains("HIGH"), "JSON should contain priority")
    }

    @Test
    fun `test if the processor includes module name in JSON report`() {
        val testProject = TestProject(tempDir)
        testProject.setupBuild()
        testProject.addSource(
            "MyClass.kt",
            """
            package com.escodro.techdebt
            
            import com.escodro.techdebt.TechDebt
            
            @TechDebt(description = "Test debt")
            class MyClass
            """
                .trimIndent()
        )

        testProject.build()

        val jsonFile = testProject.getJsonReportFile()
        assertTrue(jsonFile.exists(), "JSON report file should exist")

        val content = jsonFile.readText()
        assertTrue(content.contains("test-project"), "JSON should contain module name")
    }

    @Test
    fun `test if the processor handles functions and properties`() {
        val testProject = TestProject(tempDir)
        testProject.setupBuild()
        testProject.addSource(
            "MyClass.kt",
            """
            package com.escodro.techdebt
            
            import com.escodro.techdebt.TechDebt
            
            class MyClass {
                @TechDebt(description = "Property debt")
                val myProp: String = ""

                @TechDebt(description = "Function debt")
                fun myFunc() {}
            }
            """
                .trimIndent()
        )

        testProject.build()

        val jsonFile = testProject.getJsonReportFile()
        val content = jsonFile.readText()
        assertTrue(content.contains("Property debt"))
        assertTrue(content.contains("Function debt"))
    }

    @Test
    fun `test if the processor handles file annotation`() {
        val testProject = TestProject(tempDir)
        testProject.setupBuild()
        testProject.addSource(
            "AnnotatedFile.kt",
            """
            @file:TechDebt(description = "File debt")
            package com.escodro.techdebt
            
            import com.escodro.techdebt.TechDebt
            
            class AnnotatedFile
            """
                .trimIndent()
        )

        testProject.build()

        val jsonFile = testProject.getJsonReportFile()
        val content = jsonFile.readText()
        assertTrue(content.contains("File debt"))
        assertTrue(content.contains("AnnotatedFile.kt"))
    }

    @Test
    fun `test if the processor handles missing module name`() {
        val testProject = TestProject(tempDir)
        // Manual setup without moduleName arg
        testProject.addSource(
            "MyClass.kt",
            """
            package com.escodro.techdebt
            import com.escodro.techdebt.TechDebt
            @TechDebt(description = "No module")
            class MyClass
            """
                .trimIndent()
        )

        // Custom setup without ksp arg
        val classpath =
            System.getProperty("java.class.path").split(File.pathSeparator).map { File(it) }
        File(tempDir, "build.gradle.kts")
            .writeText(
                """
            plugins {
                kotlin("jvm") version "2.3.0"
                id("com.google.devtools.ksp") version "2.3.4"
            }
            repositories { 
                mavenLocal()
                mavenCentral()
            }
            dependencies {
                implementation("io.github.igorescodro:techdebt-annotations:0.1.0-beta01")
                ksp(files(${classpath.joinToString { "\"$it\"" }}))
            }
            // moduleName arg is intentionally omitted
            """
                    .trimIndent()
            )

        testProject.build()

        val jsonFile = testProject.getJsonReportFile()
        val content = jsonFile.readText()
        assertTrue(content.contains("\"moduleName\": \"unknown\""))
    }

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
