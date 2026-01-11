plugins {
    alias(libs.plugins.kotlin.jvm)
    id("maven-publish")
}

group = "com.escodro.techdebt"
version = libs.versions.techdebt.get()

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":techdebt-annotations"))
    implementation(libs.ksp.api)
    implementation(libs.kotlinx.html)

    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.kotlin.compile.testing.ksp)
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "techdebt-processor"
            artifact(tasks.jar)
            pom.withXml {
                val dependenciesNode = asNode().appendNode("dependencies")
                configurations.implementation.get().allDependencies.forEach {
                    val dependencyNode = dependenciesNode.appendNode("dependency")
                    if (it is ProjectDependency) {
                        dependencyNode.appendNode("groupId", "com.escodro.techdebt")
                        dependencyNode.appendNode("artifactId", it.name)
                        dependencyNode.appendNode("version", libs.versions.techdebt.get())
                    } else {
                        dependencyNode.appendNode("groupId", it.group)
                        dependencyNode.appendNode("artifactId", it.name)
                        dependencyNode.appendNode("version", it.version)
                    }
                    dependencyNode.appendNode("scope", "runtime")
                }
            }
        }
    }
}
