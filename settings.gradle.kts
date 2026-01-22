pluginManagement {
    includeBuild("techdebt-gradle-plugin")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "tech-debt"
include(":techdebt-annotations")
include(":techdebt-processor")
include(":samples:sample-jvm")
include(":samples:sample-android")
include(":samples:sample-kmp")
