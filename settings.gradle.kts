pluginManagement {
    includeBuild("techdebt-gradle-plugin")
    repositories {
        google()
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenLocal()
        mavenCentral()
    }
}

rootProject.name = "tech-debt"
include(":techdebt-annotations")
include(":techdebt-processor")
include(":samples:sample-jvm")
include(":samples:sample-android")
include(":samples:sample-kmp")
