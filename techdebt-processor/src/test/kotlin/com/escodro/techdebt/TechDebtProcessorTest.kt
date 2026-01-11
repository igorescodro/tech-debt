package com.escodro.techdebt

import com.escodro.techdebt.fake.CapturingProcessorProvider
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.symbolProcessorProviders
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
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
        assertFalse(reportFile.exists(), "Report file should not exist")
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

    @Test
    fun `test if the processor handles multiple originating files`() {
        val source1 =
            SourceFile.kotlin(
                "File1.kt",
                """
            package com.escodro.techdebt
            import com.escodro.techdebt.TechDebt
            @TechDebt(description = "Debt 1")
            class File1
            """
            )
        val source2 =
            SourceFile.kotlin(
                "File2.kt",
                """
            package com.escodro.techdebt
            import com.escodro.techdebt.TechDebt
            @TechDebt(description = "Debt 2")
            class File2
            """
            )

        val compilation =
            KotlinCompilation().apply {
                sources = listOf(source1, source2)
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
        assertTrue(content.contains("Debt 1"))
        assertTrue(content.contains("Debt 2"))
    }

    @Test
    fun `test if the processor returns unable to process symbols`() {
        val source =
            SourceFile.kotlin(
                "MyClass.kt",
                """
            package com.escodro.techdebt
            import com.escodro.techdebt.TechDebt

            @TechDebt(description = "Invalid")
            val invalidProperty: UnresolvedType = 123
            """
            )

        val capturingProvider = CapturingProcessorProvider()
        val compilation =
            KotlinCompilation().apply {
                sources = listOf(source)
                symbolProcessorProviders = listOf(capturingProvider)
                inheritClassPath = true
            }

        compilation.compile()

        // It should still compile or at least try to process
        val capturedResults = capturingProvider.processor?.capturedResults ?: emptyList()
        assertTrue(capturedResults.isNotEmpty(), "There should be symbols unable to process")
        val symbolNames = capturedResults.map { it.toString() }
        assertTrue(
            symbolNames.contains("invalidProperty"),
            "The invalid property should be captured"
        )
    }

    @Test
    fun `test if the processor sets NONE when priority is invalid`() {
        val source =
            SourceFile.kotlin(
                "MyClass.kt",
                """
            package com.escodro.techdebt
            import com.escodro.techdebt.TechDebt

            @TechDebt(priority = Priority.INVALID)
            class MyClass
            """
            )

        val compilation =
            KotlinCompilation().apply {
                sources = listOf(source)
                symbolProcessorProviders = listOf(TechDebtProcessorProvider())
                inheritClassPath = true
            }

        compilation.compile()
        // It might fail to compile because of Priority.INVALID, but KSP should still run
        // and if it can't resolve the type, it should fallback to NONE

        val reportFile =
            compilation.workingDir.resolve("ksp/sources/resources/techdebt/report.html")
        assertTrue(reportFile.exists(), "Report file should exist")

        val content = reportFile.readText()
        assertTrue(content.contains("NONE"), "The priority should be NONE")
    }
}
