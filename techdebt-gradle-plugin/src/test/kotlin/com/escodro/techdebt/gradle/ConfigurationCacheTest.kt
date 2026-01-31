package com.escodro.techdebt.gradle

import java.io.File
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class ConfigurationCacheTest {

    @TempDir lateinit var tempDir: File

    @BeforeEach
    fun setup() {
        File(tempDir, "settings.gradle.kts")
            .writeText(
                """
            rootProject.name = "test-project"
            include(":lib")
        """
                    .trimIndent()
            )

        File(tempDir, "build.gradle.kts")
            .writeText(
                """
            plugins {
                id("io.github.igorescodro.techdebt")
            }
            
            allprojects {
                repositories {
                    mavenCentral()
                    google()
                    mavenLocal()
                }
            }
            
            techDebtReport {
                collectComments.set(true)
            }
        """
                    .trimIndent()
            )

        val libDir = File(tempDir, "lib")
        libDir.mkdirs()
        File(libDir, "build.gradle.kts")
            .writeText(
                """
            plugins {
                kotlin("jvm")
                id("io.github.igorescodro.techdebt")
            }
        """
                    .trimIndent()
            )

        val srcDir = File(libDir, "src/main/kotlin/com/example")
        srcDir.mkdirs()
        File(srcDir, "MyClass.kt")
            .writeText(
                """
            package com.example
            // TODO: Test configuration cache
            class MyClass
        """
                    .trimIndent()
            )
    }

    @Test
    fun `test generateTechDebtReport works with configuration cache`() {
        // First run to store the configuration cache
        val firstResult =
            GradleRunner.create()
                .withProjectDir(tempDir)
                .withArguments("generateTechDebtReport", "--configuration-cache")
                .withPluginClasspath()
                .build()

        assertEquals(TaskOutcome.SUCCESS, firstResult.task(":generateTechDebtReport")?.outcome)
        assertTrue(firstResult.output.contains("Configuration cache entry stored"))

        // Second run to use the configuration cache
        val secondResult =
            GradleRunner.create()
                .withProjectDir(tempDir)
                .withArguments("generateTechDebtReport", "--configuration-cache")
                .withPluginClasspath()
                .build()

        assertTrue(
            secondResult.task(":generateTechDebtReport")?.outcome in
                listOf(TaskOutcome.SUCCESS, TaskOutcome.UP_TO_DATE)
        )
        assertTrue(secondResult.output.contains("Configuration cache entry reused"))

        val reportFile = File(tempDir, "build/reports/techdebt/consolidated-report.html")
        assertTrue(reportFile.exists())
        assertTrue(reportFile.readText().contains("TODO: Test configuration cache"))
    }
}
