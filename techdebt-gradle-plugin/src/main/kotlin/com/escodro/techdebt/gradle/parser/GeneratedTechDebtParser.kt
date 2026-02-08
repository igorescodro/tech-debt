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
        return sourceSet == SOURCE_SET_UNKNOWN || sourceSet == SOURCE_SET_MAIN
    }

    private fun resolveSourceSet(path: String): String {
        val parts = path.split("/")
        val kspIndex = parts.indexOf("ksp")
        if (kspIndex != -1 && kspIndex + 1 < parts.size) {
            return parts[kspIndex + 1]
        }
        return SOURCE_SET_UNKNOWN
    }

    private companion object {

        private const val SOURCE_SET_MAIN = "main"
        private const val SOURCE_SET_UNKNOWN = "unknown"
    }
}
