package com.escodro.techdebt.gradle.parser

import java.io.File
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

internal class SourceFileResolverTest {

    @TempDir lateinit var tempDir: File

    @Test
    fun `test resolver finds file by absolute path`() {
        val file = File(tempDir, "MyFile.kt").apply { writeText("content") }
        val resolver = SourceFileResolver(tempDir)

        val resolved = resolver.resolve(file.absolutePath + ":10", ":app")

        assertNotNull(resolved)
        assertEquals(file.absolutePath, resolved?.absolutePath)
    }

    @Test
    fun `test resolver finds file by relative path from root`() {
        val subDir = File(tempDir, "src/main/kotlin").apply { mkdirs() }
        val file = File(subDir, "MyFile.kt").apply { writeText("content") }
        val resolver = SourceFileResolver(tempDir)

        val resolved = resolver.resolve("src/main/kotlin/MyFile.kt:10", ":app")

        assertNotNull(resolved)
        assertEquals(file.absolutePath, resolved?.absolutePath)
    }

    @Test
    fun `test resolver finds file in module directory`() {
        val moduleDir = File(tempDir, "app/src/main/kotlin").apply { mkdirs() }
        val file = File(moduleDir, "MyFile.kt").apply { writeText("content") }
        val resolver = SourceFileResolver(tempDir)

        // The path in sourceSet might be relative to the module
        val resolved = resolver.resolve("src/main/kotlin/MyFile.kt:10", ":app")

        assertNotNull(resolved)
        assertEquals(file.absolutePath, resolved?.absolutePath)
    }

    @Test
    fun `test resolver falls back to walkTopDown`() {
        val deepDir = File(tempDir, "some/very/deep/path").apply { mkdirs() }
        val file = File(deepDir, "DeepFile.kt").apply { writeText("content") }
        val resolver = SourceFileResolver(tempDir)

        val resolved = resolver.resolve("DeepFile.kt:10", ":any")

        assertNotNull(resolved)
        assertEquals(file.absolutePath, resolved?.absolutePath)
    }

    @Test
    fun `test resolver returns null for unknown sourceSet`() {
        val resolver = SourceFileResolver(tempDir)
        val resolved = resolver.resolve("unknown", ":app")
        assertNull(resolved)
    }

    @Test
    fun `test resolver handles main sourceSet by falling back to search`() {
        val subDir = File(tempDir, "src/main/kotlin").apply { mkdirs() }
        val file = File(subDir, "MainFile.kt").apply { writeText("content") }
        val resolver = SourceFileResolver(tempDir)

        // When KSP says "main", we hope to find the file by searching for the module name or
        // something
        // In this case, "main" as sourceSet won't match any file name unless we have a file named
        // "main"
        // But if we pass a real file name that KSP sometimes fails to give full path for:
        val resolved = resolver.resolve("MainFile.kt", ":app")

        assertNotNull(resolved)
        assertEquals(file.absolutePath, resolved?.absolutePath)
    }
}
