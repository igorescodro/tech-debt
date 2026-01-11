plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":techdebt-annotations"))
    ksp(project(":techdebt-processor"))
}

kotlin {
    jvmToolchain(17)
}
