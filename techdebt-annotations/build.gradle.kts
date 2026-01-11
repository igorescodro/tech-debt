plugins {
    alias(libs.plugins.kotlin.jvm)
    id("maven-publish")
}

group = "com.escodro.techdebt"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlin.stdlib)
}

kotlin {
    jvmToolchain(17)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "techdebt-annotations"
            artifact(tasks.jar)
            pom.withXml {
                val dependenciesNode = asNode().appendNode("dependencies")
                configurations.implementation.get().allDependencies.forEach {
                    val dependencyNode = dependenciesNode.appendNode("dependency")
                    dependencyNode.appendNode("groupId", it.group)
                    dependencyNode.appendNode("artifactId", it.name)
                    dependencyNode.appendNode("version", it.version)
                    dependencyNode.appendNode("scope", "runtime")
                }
            }
        }
    }
}
