plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlin.stdlib)
    // KSP dependencies will be added here or by the user later
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}
