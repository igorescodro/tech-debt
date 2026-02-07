package com.escodro.techdebt.gradle.parser

import com.escodro.techdebt.gradle.model.TechDebtItem
import java.io.File
import java.io.IOException
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import org.apache.log4j.Logger
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.blame.BlameResult
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder

/**
 * Metadata processor responsible for enriching [TechDebtItem] with Git information, such as the
 * last modified date.
 *
 * @param rootProjectDirectory the root directory of the project
 */
internal class GitParser(private val rootProjectDirectory: File) {

    private val logger = Logger.getLogger(GitParser::class.java)

    private val sourceFileResolver = SourceFileResolver(rootProjectDirectory)

    private val blameCache = mutableMapOf<String, BlameResult?>()

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
                val gitInfo = getGitInfo(git, item)
                item.copy(lastModified = gitInfo?.lastModified, author = gitInfo?.author)
            }
        }
    }

    private fun getGitInfo(git: Git, item: TechDebtItem): GitInfo? {
        val sourceFile = sourceFileResolver.resolve(item.location, item.moduleName)
        if (sourceFile == null || !sourceFile.exists()) {
            return null
        }

        val relativePath = sourceFile.relativeTo(rootProjectDirectory).path
        val lineNumber = getLineNumber(item.sourceSet)

        return try {
            val blame =
                blameCache.getOrPut(relativePath) {
                    git.blame().setFilePath(relativePath).setFollowFileRenames(true).call()
                }
            extractGitInfo(blame, lineNumber)
        } catch (e: IOException) {
            logger.error("Failed to get Git info for file: $relativePath at line: $lineNumber", e)
            null
        } catch (e: GitAPIException) {
            logger.error("Failed to get Git blame for file: $relativePath at line: $lineNumber", e)
            null
        }
    }

    private fun extractGitInfo(blame: BlameResult?, lineNumber: Int): GitInfo? {
        val commit = blame?.getSourceCommit(lineNumber - 1) ?: return null
        val authorIdent = commit.authorIdent
        val date = authorIdent.whenAsInstant
        return GitInfo(
            lastModified =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    .withZone(ZoneId.systemDefault())
                    .format(date),
            author = authorIdent.name,
        )
    }

    private fun findRepository(directory: File): Repository? =
        try {
            val gitDir = FileRepositoryBuilder().readEnvironment().findGitDir(directory).gitDir
            if (gitDir == null) {
                null
            } else {
                FileRepositoryBuilder().setGitDir(gitDir).build()
            }
        } catch (e: IOException) {
            logger.error("Failed to find Git repository in directory: ${directory.absolutePath}", e)
            null
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid Git repository in directory: ${directory.absolutePath}", e)
            null
        }

    private fun getLineNumber(sourceSet: String): Int =
        sourceSet.substringAfterLast(":").toIntOrNull() ?: 1
}

/**
 * Data class representing Git metadata information.
 *
 * @param lastModified the last modified date of the file
 * @param author the author of the last modification
 */
private data class GitInfo(
    val lastModified: String?,
    val author: String?,
)
