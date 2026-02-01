package com.escodro.techdebt.gradle.parser

import com.escodro.techdebt.gradle.model.TechDebtItem
import java.io.File
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder

/**
 * Metadata processor responsible for enriching [TechDebtItem] with Git information, such as the last
 * modified date.
 */
internal class GitParser(private val rootProjectDirectory: File) {

    /**
     * Enriches the given [TechDebtItem]s with Git information.
     *
     * @param items the list of tech debt items to be enriched
     * @return the list of enriched tech debt items
     */
    fun parse(items: List<TechDebtItem>): List<TechDebtItem> {
        val repository = findRepository(rootProjectDirectory) ?: return items

        return repository.use { repo ->
            val git = Git(repo)
            items.map { item ->
                val date = getGitLastModified(repo, git, item)
                item.copy(date = date)
            }
        }
    }

    private fun getGitLastModified(repository: Repository, git: Git, item: TechDebtItem): String? {
        val sourceFile = resolveSourceFile(item) ?: return null
        if (!sourceFile.exists()) return null

        val relativePath = sourceFile.relativeTo(rootProjectDirectory).path
        val lineNumber = getLineNumber(item.sourceSet)

        return try {
            val blame = git.blame()
                .setFilePath(relativePath)
                .setFollowFileRenames(true)
                .call()

            val commit = blame?.getSourceCommit(lineNumber - 1) ?: return null
            val authorIdent = commit.authorIdent
            val date = authorIdent.`when`
            date.toString()
        } catch (e: Exception) {
            null
        }
    }

    private fun findRepository(directory: File): Repository? =
        try {
            FileRepositoryBuilder()
                .readEnvironment()
                .findGitDir(directory)
                .build()
        } catch (e: Exception) {
            null
        }

    private fun resolveSourceFile(item: TechDebtItem): File? {
        val sourceSet = item.sourceSet
        if (sourceSet == "unknown" || sourceSet == "main") return null

        val path = sourceSet.substringBeforeLast(":")
        val file = File(path)
        if (file.isAbsolute && file.exists()) {
            return file
        }

        // Try relative to root project
        val relativeFile = File(rootProjectDirectory, path)
        if (relativeFile.exists()) {
            return relativeFile
        }

        // Try to find the module directory by converting :module:path to module/path
        val moduleRelativePath = item.moduleName.removePrefix(":").replace(":", "/")
        val moduleDir = File(rootProjectDirectory, moduleRelativePath)

        if (moduleDir.exists() && moduleDir.isDirectory) {
            val fileInModule = File(moduleDir, path)
            if (fileInModule.exists()) return fileInModule
        }

        // Fallback: search for the file in the whole project (can be slow)
        return rootProjectDirectory.walkTopDown()
            .filter { it.isFile && it.endsWith(path) }
            .firstOrNull()
    }

    private fun getLineNumber(sourceSet: String): Int =
        sourceSet.substringAfterLast(":").toIntOrNull() ?: 1
}
