package com.escodro.techdebt.utils

import java.io.File
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner

class TestProject(private val projectDir: File) {

    private val buildFile = File(projectDir, "build.gradle.kts")
    private val settingsFile = File(projectDir, "settings.gradle.kts")
    private val srcDir = File(projectDir, "src/main/kotlin")

    init {
        projectDir.mkdirs()
        srcDir.mkdirs()
        setupSettings()
    }

    private fun setupSettings() {
        settingsFile.writeText(
            """
            rootProject.name = "test-project"
            """
                .trimIndent()
        )
    }

    fun setupBuild(
        kotlinVersion: String = "2.3.0",
        kspVersion: String = "2.3.4",
    ) {
        val classpath =
            System.getProperty("java.class.path").split(File.pathSeparator).map { File(it) }

        buildFile.writeText(
            """
            plugins {
                kotlin("jvm") version "$kotlinVersion"
                id("com.google.devtools.ksp") version "$kspVersion"
            }

            repositories {
                mavenCentral()
            }

            dependencies {
                implementation("io.github.igorescodro:techdebt-annotations:0.1.0-beta01")
                ksp(files(${classpath.joinToString { "\"$it\"" }}))
            }

            ksp {
                arg("moduleName", "test-project")
            }
            """
                .trimIndent()
        )
    }

    fun addSource(fileName: String, content: String) {
        val file = File(srcDir, fileName)
        file.parentFile.mkdirs()
        file.writeText(content)
    }

    fun build(): BuildResult {
        return GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("kspKotlin", "--stacktrace")
            .forwardOutput()
            .build()
    }

    fun getJsonReportFile(): File {
        return File(projectDir, "build/generated/ksp/main/resources/techdebt/report.json")
    }
}
