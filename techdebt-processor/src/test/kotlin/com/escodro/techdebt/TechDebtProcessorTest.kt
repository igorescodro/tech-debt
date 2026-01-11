package com.escodro.techdebt

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.symbolProcessorProviders
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TechDebtProcessorTest {

    @Test
    fun `test if the processor generates the report file for classes`() {
        val source =
            SourceFile.kotlin(
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
            )

        val compilation =
            KotlinCompilation().apply {
                sources = listOf(source)
                symbolProcessorProviders = listOf(TechDebtProcessorProvider())
                inheritClassPath = true
                messageOutputStream = System.out
            }

        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val reportFile =
            compilation.workingDir.resolve("ksp/sources/resources/techdebt/report.html")
        assertTrue(reportFile.exists(), "Report file should exist")

        val content = reportFile.readText()
        assertTrue(content.contains("MyClass"))
        assertTrue(content.contains("My description"))
        assertTrue(content.contains("JIRA-123"))
        assertTrue(content.contains("HIGH"))
    }

    @Test
    fun `test if the processor generates the report file for functions`() {
        val source =
            SourceFile.kotlin(
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
            )

        val compilation =
            KotlinCompilation().apply {
                sources = listOf(source)
                symbolProcessorProviders = listOf(TechDebtProcessorProvider())
                inheritClassPath = true
                messageOutputStream = System.out
            }

        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val reportFile =
            compilation.workingDir.resolve("ksp/sources/resources/techdebt/report.html")
        assertTrue(reportFile.exists(), "Report file should exist")

        val content = reportFile.readText()
        assertTrue(content.contains("myFunction"))
        assertTrue(content.contains("Function debt"))
        assertTrue(content.contains("JIRA-456"))
        assertTrue(content.contains("MEDIUM"))
    }

    @Test
    fun `test if the processor generates the report file for properties`() {
        val source =
            SourceFile.kotlin(
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
            )

        val compilation =
            KotlinCompilation().apply {
                sources = listOf(source)
                symbolProcessorProviders = listOf(TechDebtProcessorProvider())
                inheritClassPath = true
                messageOutputStream = System.out
            }

        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val reportFile =
            compilation.workingDir.resolve("ksp/sources/resources/techdebt/report.html")
        assertTrue(reportFile.exists(), "Report file should exist")

        val content = reportFile.readText()
        assertTrue(content.contains("myProperty"))
        assertTrue(content.contains("Property debt"))
        assertTrue(content.contains("JIRA-789"))
        assertTrue(content.contains("LOW"))
    }

    @Test
    fun `test if the processor does not generate a report if no annotations are found`() {
        val source =
            SourceFile.kotlin(
                "MyClass.kt",
                """
            package com.escodro.techdebt
            
            class MyClass
            """
            )

        val compilation =
            KotlinCompilation().apply {
                sources = listOf(source)
                symbolProcessorProviders = listOf(TechDebtProcessorProvider())
                inheritClassPath = true
                messageOutputStream = System.out
            }

        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val reportFile =
            compilation.workingDir.resolve("ksp/sources/resources/techdebt/report.html")
        assertTrue(!reportFile.exists(), "Report file should not exist")
    }

    @Test
    fun `test if the processor correctly counts priorities`() {
        val source =
            SourceFile.kotlin(
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
            )

        val compilation =
            KotlinCompilation().apply {
                sources = listOf(source)
                symbolProcessorProviders = listOf(TechDebtProcessorProvider())
                inheritClassPath = true
                messageOutputStream = System.out
            }

        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val reportFile =
            compilation.workingDir.resolve("ksp/sources/resources/techdebt/report.html")
        assertTrue(reportFile.exists(), "Report file should exist")

        val content = reportFile.readText().replace("\n", "")
        assertTrue(content.contains("<h2>6</h2><span>Total Items</span>"))
        assertTrue(content.contains("<h2>2</h2><span>High Priority</span>"))
        assertTrue(content.contains("<h2>1</h2><span>Medium Priority</span>"))
        assertTrue(content.contains("<h2>1</h2><span>Low Priority</span>"))
        assertTrue(content.contains("<h2>2</h2><span>No Priority</span>"))
    }
}
