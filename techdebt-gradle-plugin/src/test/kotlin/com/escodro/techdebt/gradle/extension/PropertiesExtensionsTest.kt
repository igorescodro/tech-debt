package com.escodro.techdebt.gradle.extension

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class PropertiesExtensionsTest {

    @Test
    fun `test getPluginVersion returns version from properties file`() {
        val version = getPluginVersion()
        assertEquals("0.4.0", version)
    }

    @Test
    fun `test getPluginVersion throws exception when file is missing`() {
        assertThrows(IllegalStateException::class.java) { getPluginVersion("missing.properties") }
    }

    @Test
    fun `test getPluginVersion throws error when version is missing`() {
        assertThrows(IllegalStateException::class.java) {
            getPluginVersion("no_version.properties")
        }
    }

    @Test
    fun `test getPluginVersion throws error when version is blank`() {
        assertThrows(IllegalStateException::class.java) {
            getPluginVersion("blank_version.properties")
        }
    }
}
