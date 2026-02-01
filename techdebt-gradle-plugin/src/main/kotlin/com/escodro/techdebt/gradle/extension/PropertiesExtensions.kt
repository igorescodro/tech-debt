package com.escodro.techdebt.gradle.extension

import com.escodro.techdebt.gradle.TechDebtPlugin
import java.util.*
import org.apache.log4j.Logger

private const val TECH_DEBT_PROPERTIES = "techdebt.properties"
private const val VERSION_PROPERTY = "version"

fun getPluginVersion(): String {
    val props =
        Properties().apply {
            val classLoader = TechDebtPlugin::class.java.classLoader
            val resourceStream =
                classLoader?.getResourceAsStream(TECH_DEBT_PROPERTIES)
                    ?: Thread.currentThread()
                        .contextClassLoader
                        ?.getResourceAsStream(TECH_DEBT_PROPERTIES)
            if (resourceStream == null) {
                // Fallback for cases where the resource might not be in the classpath during
                // tests or specific environments.
                Logger.getLogger(TechDebtPlugin::class.java)
                    .warn("techdebt.properties not found, " + "defaulting version to 1.0.0")
                return "1.0.0"
            }
            resourceStream.use { stream -> load(stream) }
        }

    val version: String =
        props[VERSION_PROPERTY] as? String ?: error("Version not found in techdebt.properties")
    if (version.isBlank()) {
        error("Version cannot be blank in techdebt.properties")
    }
    return version
}
