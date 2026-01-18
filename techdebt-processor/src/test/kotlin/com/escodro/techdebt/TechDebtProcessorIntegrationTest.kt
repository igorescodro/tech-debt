package com.escodro.techdebt

import com.escodro.techdebt.utils.TestProject
import java.io.File
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

internal class TechDebtProcessorIntegrationTest {

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
        assertTrue(content.contains("HIGH"))
    }

    @Test
    fun `test if the processor includes module name in JSON report`() {
        val testProject = TestProject(tempDir)
        testProject.setupBuild(moduleName = ":my-module")
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
        assertTrue(content.contains(":my-module"), "JSON should contain module name")
    }
}
