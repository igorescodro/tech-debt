plugins {
    alias(libs.plugins.kotlin.jvm)
    id("io.github.igorescodro.techdebt")
}

repositories {
    mavenLocal()
    mavenCentral()
}

kotlin {
    jvmToolchain(17)
}

techDebtReport {
    outputFile.set(layout.projectDirectory.file("assets/report.html"))
    collectSuppress.set(true)
}
