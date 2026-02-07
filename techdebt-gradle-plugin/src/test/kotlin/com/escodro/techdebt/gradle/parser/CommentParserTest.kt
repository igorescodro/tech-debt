package com.escodro.techdebt.gradle.parser

import com.escodro.techdebt.gradle.model.TechDebtItemType
import java.io.File
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

internal class CommentParserTest {

    @TempDir lateinit var tempDir: File

    private lateinit var parser: CommentParser

    @BeforeEach
    fun setup() {
        parser = CommentParser()
    }

    @Test
    fun `test parser collects TODOs from source files`() {
        val project = ProjectBuilder.builder().withProjectDir(tempDir).build()
        val projectDir = File(tempDir, "project")
        projectDir.mkdirs()
        val srcDir = File(projectDir, "src/main/kotlin/com/example")
        srcDir.mkdirs()
        val file =
            File(srcDir, "MyClass.kt").apply {
                writeText(
                    """
                package com.example
                
                // TODO: My task
                class MyClass
                """
                        .trimIndent()
                )
            }

        val sourceFiles = project.files(file)
        val projectPaths = mapOf(projectDir.absolutePath to ":project")

        val items = parser.parse(sourceFiles, projectPaths)

        assertEquals(1, items.size)
        val item = items.first()
        assertEquals(":project", item.moduleName)
        assertEquals("TODO: My task", item.description)
        assertEquals("src/main/kotlin/com/example/MyClass.kt", item.sourceSet)
        assertEquals("src/main/kotlin/com/example/MyClass.kt:3", item.location)
        assertEquals(TechDebtItemType.COMMENT, item.type)
    }

    @Test
    fun `test parser collects FIXMEs from source files`() {
        val project = ProjectBuilder.builder().withProjectDir(tempDir).build()
        val projectDir = File(tempDir, "project")
        projectDir.mkdirs()
        val srcDir = File(projectDir, "src/main/kotlin/com/example")
        srcDir.mkdirs()
        val file =
            File(srcDir, "MyClass.kt").apply {
                writeText(
                    """
                package com.example
                
                // FIXME: Fix this
                class MyClass
                """
                        .trimIndent()
                )
            }

        val sourceFiles = project.files(file)
        val projectPaths = mapOf(projectDir.absolutePath to ":project")

        val items = parser.parse(sourceFiles, projectPaths)

        assertEquals(1, items.size)
        val item = items.first()
        assertEquals("FIXME: Fix this", item.description)
        assertEquals(TechDebtItemType.COMMENT, item.type)
    }

    @Test
    fun `test parser collects block comments from source files`() {
        val project = ProjectBuilder.builder().withProjectDir(tempDir).build()
        val projectDir = File(tempDir, "project")
        projectDir.mkdirs()
        val srcDir = File(projectDir, "src/main/kotlin/com/example")
        srcDir.mkdirs()
        val file =
            File(srcDir, "MyClass.kt").apply {
                writeText(
                    """
                package com.example
                
                /**
                 * TODO: Block comment TODO
                 */
                class MyClass
                """
                        .trimIndent()
                )
            }

        val sourceFiles = project.files(file)
        val projectPaths = mapOf(projectDir.absolutePath to ":project")

        val items = parser.parse(sourceFiles, projectPaths)

        assertEquals(1, items.size)
        val item = items.first()
        assertEquals("TODO: Block comment TODO", item.description)
        assertEquals(TechDebtItemType.COMMENT, item.type)
    }

    @Test
    fun `test parser correctly identifies module name in multi-project setup`() {
        val project = ProjectBuilder.builder().withProjectDir(tempDir).build()
        val rootDir = File(tempDir, "root")
        val appDir = File(rootDir, "app")
        val libDir = File(rootDir, "lib")
        appDir.mkdirs()
        libDir.mkdirs()

        val appFile = File(appDir, "App.kt").apply { writeText("// TODO: App task") }
        val libFile = File(libDir, "Lib.kt").apply { writeText("// TODO: Lib task") }

        val sourceFiles = project.files(appFile, libFile)
        val projectPaths = mapOf(appDir.absolutePath to ":app", libDir.absolutePath to ":lib")

        val items = parser.parse(sourceFiles, projectPaths)

        assertEquals(2, items.size)
        assertTrue(items.any { it.moduleName == ":app" && it.description == "TODO: App task" })
        assertTrue(items.any { it.moduleName == ":lib" && it.description == "TODO: Lib task" })
    }
}
