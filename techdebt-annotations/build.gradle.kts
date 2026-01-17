plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.maven.publish)
}

group = "io.github.igorescodro"
version = libs.versions.techdebt.get()

repositories {
    mavenCentral()
}

kotlin {
    jvm()
    iosArm64()
    iosX64()
    iosSimulatorArm64()
    macosArm64()
    macosX64()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlin.stdlib)
        }
    }

    jvmToolchain(17)
}

mavenPublishing {
    publishToMavenCentral()
    if (!project.hasProperty("skip.signing")) {
        signAllPublications()
    }

    pom {
        name.set("tech-debt annotations")
        description.set("A KSP annotation to mark code as tech debt")
        url.set("https://github.com/igorescodro/tech-debt")
        licenses {
            license {
                name.set("The Apache Software License, Version 2.0")
                url.set("https://raw.githubusercontent.com/igorescodro/tech-debt/refs/heads/main/LICENSE")
            }
        }
        developers {
            developer {
                id.set("igorescodro")
                name.set("Igor Escodro")
                email.set("escodro@outlook.com")
            }
        }
        scm {
            connection.set("scm:git:github.com/igorescodro/tech-debt.git")
            developerConnection.set("scm:git:ssh://github.com/igorescodro/tech-debt.git")
            url.set("https://github.com/igorescodro/tech-debt")
        }
    }
}
