package com.escodro.techdebt.gradle.parser

import com.escodro.techdebt.gradle.model.TechDebtItem
import com.escodro.techdebt.gradle.model.TechDebtItemType
import java.io.File
import org.gradle.api.file.ConfigurableFileCollection

/** Parses comments from source files to extract tech debt items. */
internal class CommentParser {

    /**
     * Parses comments from source files to extract tech debt items.
     *
     * @param sourceFiles the source files to parse
     * @param projectPaths the project paths to associate with the tech debt items
     * @return the list of parsed tech debt items
     */
    fun parse(
        sourceFiles: ConfigurableFileCollection,
        projectPaths: Map<String, String?>
    ): List<TechDebtItem> {
        val items = mutableListOf<TechDebtItem>()
        val todoCommentPattern =
            Regex("""^\s*(?://|/\*\*?|\*)\s*(TODO|FIXME)\b[:\s]?(.*?)(?:\s*\*+/)?\s*$""")

        sourceFiles.files.forEach { file ->
            val projectDir =
                projectPaths.keys
                    .filter { File(file.absolutePath).startsWith(File(it)) }
                    .maxByOrNull { it.length } ?: return@forEach
            val projectPath = projectPaths[projectDir] ?: return@forEach

            file.readLines(Charsets.UTF_8).forEachIndexed { index, line ->
                val matchResult = todoCommentPattern.find(line)
                if (matchResult != null) {
                    val type = matchResult.groupValues[1]
                    val content = matchResult.groupValues[2].trim()
                    val description = if (content.isEmpty()) type else "$type: $content"

                    val relativePath = file.relativeTo(File(projectDir)).invariantSeparatorsPath
                    items +=
                        TechDebtItem(
                            moduleName = projectPath,
                            sourceSet = "$relativePath:${index + 1}",
                            name = "",
                            description = description,
                            ticket = "",
                            priority = "",
                            type = TechDebtItemType.COMMENT
                        )
                }
            }
        }

        return items
    }
}
