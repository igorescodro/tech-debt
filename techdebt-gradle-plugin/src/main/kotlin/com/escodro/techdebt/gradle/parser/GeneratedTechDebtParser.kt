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
                    if (items.any { it.sourceSet == "unknown" }) {
                        val sourceSet = resolveSourceSet(file.absolutePath)
                        items.map { it.copy(sourceSet = sourceSet) }
                    } else {
                        items
                    }
                allItems.addAll(updatedItems)
            }
        }
        return allItems
    }

    private fun resolveSourceSet(path: String): String {
        val parts = path.split("/")
        val kspIndex = parts.indexOf("ksp")
        return if (kspIndex != -1 && kspIndex + 1 < parts.size) {
            parts[kspIndex + 1]
        } else {
            "unknown"
        }
    }
}
