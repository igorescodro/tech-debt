import org.gradle.kotlin.dsl.provideDelegate

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.maven.publish)
    `java-gradle-plugin`
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktfmt)
}

group = "io.github.igorescodro"
version = libs.versions.techdebt.get()

repositories {
    mavenLocal()
    mavenCentral()
    google()
}

dependencies {
    implementation(libs.kotlinx.html)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.ksp.gradle.plugin)
    implementation(libs.android.gradle.plugin)

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

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom(files("${project.layout.projectDirectory.dir("..")}/config/detekt/detekt.yml"))
}

ktfmt {
    kotlinLangStyle()
}

kotlin {
    jvmToolchain(17)
}

val generateVersion by tasks.registering(WriteProperties::class) {
    destinationFile.set(layout.buildDirectory.file("generated/techdebt/techdebt.properties"))
    property("version", project.version.toString())
}

sourceSets.main {
    resources.srcDir(generateVersion.map { it.destinationFile.get().asFile.parentFile })
}

tasks.named<ProcessResources>("processResources") {
    dependsOn(generateVersion)
}

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
    if (!project.hasProperty("skip.signing")) {
        signAllPublications()
    }

    pom {
        name.set("tech-debt gradle plugin")
        description.set("A Gradle plugin to generate tech debt reports")
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
