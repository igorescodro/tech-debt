plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.maven.publish) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktfmt)
    id("lifecycle-base")
}

repositories {
    mavenCentral()
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
}

tasks.named("check") {
    dependsOn(gradle.includedBuild("techdebt-gradle-plugin").task(":check"))
}
