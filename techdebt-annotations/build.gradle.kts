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

    pom {
        name.set("tech-debt annotations")
        description.set("Annotations for the tech-debt KSP tool.")
        url.set("https://github.com/igorescodro/tech-debt")
        licenses {
            license {
                name.set("The Apache Software License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
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
