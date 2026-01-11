plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.maven.publish)
}

group = "io.github.igorescodro"
version = libs.versions.techdebt.get()

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlin.stdlib)
}

kotlin {
    jvmToolchain(17)
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
}
