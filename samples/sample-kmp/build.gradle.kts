plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("io.github.igorescodro.techdebt")
}

kotlin {
    jvm()
    iosArm64()
    iosSimulatorArm64()
    iosX64()

    jvmToolchain(17)
}

repositories {
    mavenLocal()
    mavenCentral()
}
