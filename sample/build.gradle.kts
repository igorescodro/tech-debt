plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.ksp)
    id("io.github.igorescodro.techdebt")
}

repositories {
    mavenCentral()
}

kotlin {
    jvm()
    jvmToolchain(17)
}

techDebtReport {
    outputFile.set(layout.projectDirectory.file("assets/report.html"))
}
