package com.escodro.techdebt.gradle.parser

import com.escodro.techdebt.gradle.model.TechDebtItem
import java.io.File
import org.eclipse.jgit.api.Git
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

internal class GitParserTest {

    @TempDir lateinit var tempDir: File

    @Test
    fun `test git parser extracts information correctly`() {
        // Initialize a git repository in the temp directory
        val git = Git.init().setDirectory(tempDir).call()
        val file = File(tempDir, "TestFile.kt")
        file.writeText("content")
        git.add().addFilepattern("TestFile.kt").call()
        val commit =
            git.commit().setMessage("Initial commit").setAuthor("Junie", "junie@example.com").call()

        val parser = GitParser(tempDir)
        val item =
            TechDebtItem(
                moduleName = ":app",
                name = "Test",
                description = "Test description",
                ticket = "T-123",
                priority = "HIGH",
                sourceSet = "main", // Realistic sourceSet name
                location = file.absolutePath + ":1"
            )

        val results = parser.parse(listOf(item))

        assertEquals(1, results.size)
        val result = results.first()
        assertEquals("Junie", result.author)
        assertNotNull(result.lastModified)
    }

    @Test
    fun `test git parser returns original items if no repository found`() {
        val parser = GitParser(tempDir)
        val file = File(tempDir, "TestFile.kt")
        file.writeText("content")
        val item =
            TechDebtItem(
                moduleName = ":app",
                name = "Test",
                description = "Test description",
                ticket = "T-123",
                priority = "HIGH",
                sourceSet = "TestFile.kt:1",
                location = file.absolutePath + ":1"
            )

        val results = parser.parse(listOf(item))

        assertEquals(1, results.size)
        assertNull(results.first().author)
        assertNull(results.first().lastModified)
    }

    @Test
    fun `test git parser uses cache for multiple items in same file`() {
        val git = Git.init().setDirectory(tempDir).call()
        val file = File(tempDir, "TestFile.kt")
        file.writeText("line 1\nline 2")
        git.add().addFilepattern("TestFile.kt").call()
        git.commit().setMessage("Initial commit").setAuthor("Junie", "junie@example.com").call()

        val parser = GitParser(tempDir)
        val item1 =
            TechDebtItem(
                moduleName = ":app",
                name = "Test 1",
                description = "Desc 1",
                ticket = "T-1",
                priority = "HIGH",
                sourceSet = "TestFile.kt:1",
                location = file.absolutePath + ":1"
            )
        val item2 =
            TechDebtItem(
                moduleName = ":app",
                name = "Test 2",
                description = "Desc 2",
                ticket = "T-2",
                priority = "LOW",
                sourceSet = "TestFile.kt:2",
                location = file.absolutePath + ":2"
            )

        val results = parser.parse(listOf(item1, item2))

        assertEquals(2, results.size)
        assertEquals("Junie", results[0].author)
        assertEquals("Junie", results[1].author)
    }

    @Test
    fun `test git parser returns null if line number is missing`() {
        val git = Git.init().setDirectory(tempDir).call()
        val file = File(tempDir, "MainFile.kt")
        file.writeText("content")
        git.add().addFilepattern("MainFile.kt").call()
        git.commit().setMessage("Initial commit").setAuthor("Junie", "junie@example.com").call()

        val parser = GitParser(tempDir)
        val item =
            TechDebtItem(
                moduleName = ":app",
                name = "Test",
                description = "Test description",
                ticket = "T-123",
                priority = "HIGH",
                sourceSet = "main",
                location = file.absolutePath // No line number here
            )

        val results = parser.parse(listOf(item))

        assertEquals(1, results.size)
        assertNull(results.first().author)
    }

    @Test
    fun `test git parser handles OS-specific paths`() {
        // Initialize a git repository
        val git = Git.init().setDirectory(tempDir).call()

        // Create a file in a subdirectory to have a relative path
        val subDir = File(tempDir, "subdir").apply { mkdir() }
        val file = File(subDir, "TestFile.kt")
        file.writeText("content")

        // In Git, the path must be forward-slash separated
        git.add().addFilepattern("subdir/TestFile.kt").call()
        git.commit().setMessage("Initial commit").setAuthor("Junie", "junie@example.com").call()

        val parser = GitParser(tempDir)
        val item =
            TechDebtItem(
                moduleName = ":app",
                name = "Test",
                description = "Test description",
                ticket = "T-123",
                priority = "HIGH",
                sourceSet = "TestFile.kt:1",
                location = file.absolutePath + ":1"
            )

        val results = parser.parse(listOf(item))
        assertEquals("Junie", results.first().author)
    }

    @Test
    fun `test git parser does not return info for uncommitted changes`() {
        val git = Git.init().setDirectory(tempDir).call()
        val file = File(tempDir, "TestFile.kt")
        file.writeText("committed content\n")
        git.add().addFilepattern("TestFile.kt").call()
        git.commit().setMessage("Initial commit").setAuthor("Junie", "junie@example.com").call()

        // Add a new line that is NOT committed
        file.appendText("uncommitted content")

        val parser = GitParser(tempDir)
        val item =
            TechDebtItem(
                moduleName = ":app",
                name = "Test",
                description = "Test description",
                ticket = "T-123",
                priority = "HIGH",
                sourceSet = "main",
                location = file.absolutePath + ":2"
            )

        val results = parser.parse(listOf(item))

        assertEquals(1, results.size)
        assertNull(results.first().author)
        assertNull(results.first().lastModified)
    }
}
