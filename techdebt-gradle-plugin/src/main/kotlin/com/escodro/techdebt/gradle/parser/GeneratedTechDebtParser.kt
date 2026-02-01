package com.escodro.techdebt.gradle.parser

import com.escodro.techdebt.gradle.model.TechDebtItem
import kotlinx.serialization.json.Json
import org.gradle.api.file.ConfigurableFileCollection

/** Parser for KSP-generated tech debt JSON files. */
internal class GeneratedTechDebtParser {

    /**
     * Parses the KSP-generated tech debt JSON files.
     *
     * @param jsonFiles the JSON files to parse
     * @return the list of parsed tech debt items
     */
    fun parse(jsonFiles: ConfigurableFileCollection): List<TechDebtItem> {
        val allItems = mutableListOf<TechDebtItem>()
        jsonFiles.files.forEach { file ->
            if (file.exists()) {
                val items = Json.decodeFromString<List<TechDebtItem>>(file.readText())
                val updatedItems =
                    items.map { item ->
                        if (shouldResolveSourceSet(item.sourceSet)) {
                            val sourceSet = resolveSourceSet(file.absolutePath)
                            item.copy(sourceSet = sourceSet)
                        } else {
                            item
                        }
                    }
                allItems.addAll(updatedItems)
            }
        }
        return allItems
    }

    private fun shouldResolveSourceSet(sourceSet: String): Boolean {
        if (sourceSet == "unknown" || sourceSet == "main") return true
        // If it's already an absolute path or has a line number colon, don't resolve
        return !sourceSet.contains("/") && !sourceSet.contains("\\") && !sourceSet.contains(":")
    }

    private fun resolveSourceSet(path: String): String {
        // Example: .../build/generated/ksp/main/resources/techdebt/report.json
        // We want to find the source set name which is after 'ksp'
        val parts = path.split("/")
        val kspIndex = parts.indexOf("ksp")
        if (kspIndex != -1 && kspIndex + 1 < parts.size) {
            val sourceSetName = parts[kspIndex + 1]
            // If it's KSP generated, we don't have exact line numbers easily here,
            // but we can at least return 'main' or similar.
            // Actually, if it's 'unknown', we don't have the file path.
            // Let's try to find if we can provide a better path.
            return sourceSetName
        }
        return "unknown"
    }
}
