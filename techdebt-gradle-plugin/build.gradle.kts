import org.gradle.kotlin.dsl.compileOnly

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.maven.publish)
    `java-gradle-plugin`
    alias(libs.plugins.kotlin.serialization)
}

group = "io.github.igorescodro"
version = libs.versions.techdebt.get()

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlinx.html)
    implementation(libs.kotlinx.serialization.json)
    compileOnly(libs.ksp.gradle.plugin)

    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(gradleTestKit())
}

gradlePlugin {
    plugins {
        register("techDebtPlugin") {
            id = "io.github.igorescodro.techdebt"
            implementationClass = "com.escodro.techdebt.gradle.TechDebtPlugin"
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
    if (!project.hasProperty("skip.signing")) {
        signAllPublications()
    }

    pom {
        name.set("tech-debt gradle plugin")
        description.set("A Gradle plugin to generate consolidated tech debt reports")
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
