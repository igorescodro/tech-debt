package com.escodro.techdebt.gradle.parser

import java.io.File

/**
 * Resolver responsible for mapping a source set string (path:line) to a physical [File] on disk.
 */
internal class SourceFileResolver(private val rootProjectDirectory: File) {

    /**
     * Resolves the source file for the given source set and module name.
     *
     * @param sourceSet the source set string (e.g., "src/main/kotlin/MyFile.kt:10" or "main")
     * @param moduleName the name of the module (e.g., ":app")
     * @return the resolved [File], or `null` if it couldn't be found
     */
    fun resolve(sourceSet: String?, moduleName: String): File? {
        val path = sourceSet?.substringBeforeLast(":")

        if (sourceSet == "unknown" || path == null) return null

        val file = File(path)
        val resolvedFile =
            when {
                file.isAbsolute && file.exists() -> file
                File(rootProjectDirectory, path).exists() -> File(rootProjectDirectory, path)
                else -> resolveInModule(moduleName, path) ?: fallbackSearch(path)
            }
        return resolvedFile
    }

    private fun resolveInModule(moduleName: String, path: String): File? {
        val moduleRelativePath = moduleName.removePrefix(":").replace(":", "/")
        val moduleDir = File(rootProjectDirectory, moduleRelativePath)

        return if (moduleDir.exists() && moduleDir.isDirectory) {
            val fileInModule = File(moduleDir, path)
            if (fileInModule.exists()) fileInModule else null
        } else {
            null
        }
    }

    private fun fallbackSearch(path: String): File? =
        rootProjectDirectory.walkTopDown().firstOrNull { it.isFile && it.path.endsWith(path) }
}
