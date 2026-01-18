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
    iosArm64()
    iosX64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":techdebt-annotations"))
        }
    }

    jvmToolchain(17)
}

dependencies {
    add("kspJvm", project(":techdebt-processor"))
}

techDebtReport {
    outputFile.set(layout.projectDirectory.file("assets/report.html"))
}
