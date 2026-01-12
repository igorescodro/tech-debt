package com.escodro.techdebt

import com.escodro.techdebt.utils.TestProject
import java.io.File
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

internal class TechDebtProcessorIntegrationTest {

    @TempDir lateinit var tempDir: File

    @Test
    fun `test if the processor generates the report file for classes`() {
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

        val reportFile = testProject.getReportFile()
        assertTrue(reportFile.exists(), "Report file should exist")

        val content = reportFile.readText()
        assertTrue(content.contains("MyClass"))
        assertTrue(content.contains("My description"))
        assertTrue(content.contains("JIRA-123"))
        assertTrue(content.contains("HIGH"))
    }

    @Test
    fun `test if the processor generates the report file for functions`() {
        val testProject = TestProject(tempDir)
        testProject.setupBuild()
        testProject.addSource(
            "MyClass.kt",
            """
            package com.escodro.techdebt
            
            import com.escodro.techdebt.TechDebt
            import com.escodro.techdebt.Priority
            
            class MyClass {

                @TechDebt(
                    ticket = "JIRA-456",
                    description = "Function debt",
                    priority = Priority.MEDIUM
                )
                fun myFunction() { }
            }
            """
                .trimIndent()
        )

        testProject.build()

        val reportFile = testProject.getReportFile()
        assertTrue(reportFile.exists(), "Report file should exist")

        val content = reportFile.readText()
        assertTrue(content.contains("myFunction"))
        assertTrue(content.contains("Function debt"))
        assertTrue(content.contains("JIRA-456"))
        assertTrue(content.contains("MEDIUM"))
    }

    @Test
    fun `test if the processor generates the report file for properties`() {
        val testProject = TestProject(tempDir)
        testProject.setupBuild()
        testProject.addSource(
            "MyClass.kt",
            """
            package com.escodro.techdebt
            
            import com.escodro.techdebt.TechDebt
            import com.escodro.techdebt.Priority
            
            class MyClass {

                @TechDebt(
                    ticket = "JIRA-789",
                    description = "Property debt",
                    priority = Priority.LOW
                )
                val myProperty: String = ""
            }
            """
                .trimIndent()
        )

        testProject.build()

        val reportFile = testProject.getReportFile()
        assertTrue(reportFile.exists(), "Report file should exist")

        val content = reportFile.readText()
        assertTrue(content.contains("myProperty"))
        assertTrue(content.contains("Property debt"))
        assertTrue(content.contains("JIRA-789"))
        assertTrue(content.contains("LOW"))
    }

    @Test
    fun `test if the processor does not generate a report if no annotations are found`() {
        val testProject = TestProject(tempDir)
        testProject.setupBuild()
        testProject.addSource(
            "MyClass.kt",
            """
            package com.escodro.techdebt
            
            class MyClass
            """
                .trimIndent()
        )

        testProject.build()

        val reportFile = testProject.getReportFile()
        assertFalse(reportFile.exists(), "Report file should not exist")
    }

    @Test
    fun `test if the processor correctly counts priorities`() {
        val testProject = TestProject(tempDir)
        testProject.setupBuild()
        testProject.addSource(
            "MyClass.kt",
            """
            package com.escodro.techdebt
            
            import com.escodro.techdebt.TechDebt
            import com.escodro.techdebt.Priority
            
            @TechDebt(priority = Priority.HIGH)
            class High1
            
            @TechDebt(priority = Priority.HIGH)
            class High2
            
            @TechDebt(priority = Priority.MEDIUM)
            class Medium1
            
            @TechDebt(priority = Priority.LOW)
            class Low1
            
            @TechDebt(priority = Priority.NONE)
            class None1
            
            @TechDebt
            class None2
            """
                .trimIndent()
        )

        testProject.build()

        val reportFile = testProject.getReportFile()
        assertTrue(reportFile.exists(), "Report file should exist")

        val content = reportFile.readText().replace("\n", "")
        assertTrue(content.contains("<h2>6</h2><span>Total Items</span>"))
        assertTrue(content.contains("<h2>2</h2><span>High Priority</span>"))
        assertTrue(content.contains("<h2>1</h2><span>Medium Priority</span>"))
        assertTrue(content.contains("<h2>1</h2><span>Low Priority</span>"))
        assertTrue(content.contains("<h2>2</h2><span>No Priority</span>"))
    }

    @Test
    fun `test if the processor handles multiple originating files`() {
        val testProject = TestProject(tempDir)
        testProject.setupBuild()
        testProject.addSource(
            "File1.kt",
            """
            package com.escodro.techdebt
            import com.escodro.techdebt.TechDebt
            @TechDebt(description = "Debt 1")
            class File1
            """
                .trimIndent()
        )
        testProject.addSource(
            "File2.kt",
            """
            package com.escodro.techdebt
            import com.escodro.techdebt.TechDebt
            @TechDebt(description = "Debt 2")
            class File2
            """
                .trimIndent()
        )

        testProject.build()

        val reportFile = testProject.getReportFile()
        assertTrue(reportFile.exists(), "Report file should exist")

        val content = reportFile.readText()
        assertTrue(content.contains("Debt 1"))
        assertTrue(content.contains("Debt 2"))
    }
}
