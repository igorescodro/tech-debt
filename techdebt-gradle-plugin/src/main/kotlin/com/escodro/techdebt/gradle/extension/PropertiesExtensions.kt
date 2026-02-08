package com.escodro.techdebt.gradle.extension

import com.escodro.techdebt.gradle.TechDebtPlugin
import java.util.*

private const val TECH_DEBT_PROPERTIES = "techdebt.properties"
private const val VERSION_PROPERTY = "version"

/**
 * Returns the current version of the plugin. The version is loaded from the `techdebt.properties`
 * file.
 *
 * @return the plugin version
 */
fun getPluginVersion(
    resourceName: String = TECH_DEBT_PROPERTIES,
): String {
    val props =
        Properties().apply {
            val classLoader = TechDebtPlugin::class.java.classLoader
            val resourceStream =
                classLoader?.getResourceAsStream(resourceName)
                    ?: Thread.currentThread().contextClassLoader?.getResourceAsStream(resourceName)
            if (resourceStream == null) {
                error("Could not find $resourceName in the classpath")
            }
            resourceStream.use { stream -> load(stream) }
        }

    val version: String =
        props[VERSION_PROPERTY] as? String ?: error("Version not found in $resourceName")
    if (version.isBlank()) {
        error("Version cannot be blank in $resourceName")
    }
    return version
}
