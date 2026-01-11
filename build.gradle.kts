plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktfmt)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":techdebt-annotations"))
    ksp(project(":techdebt-processor"))
    implementation(libs.kotlin.stdlib)
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "com.ncorti.ktfmt.gradle")

    detekt {
        buildUponDefaultConfig = true
        allRules = false
        config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    }

    ktfmt {
        kotlinLangStyle()
    }

    afterEvaluate {
        tasks.findByName("check")?.dependsOn("detekt")
        tasks.findByName("check")?.dependsOn("ktfmtCheck")
    }
}
