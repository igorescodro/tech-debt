plugins {
    alias(libs.plugins.kotlin.jvm)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":techdebt-annotations"))
    implementation(libs.ksp.api)
    implementation(libs.kotlinx.html)
}

kotlin {
    jvmToolchain(17)
}
