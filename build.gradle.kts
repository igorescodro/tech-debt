plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.maven.publish) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktfmt)
    id("lifecycle-base")
}

repositories {
    mavenLocal()
    mavenCentral()
    google()
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

tasks.named("build") {
    dependsOn(gradle.includedBuild("techdebt-gradle-plugin").task(":build"))
}

tasks.named("ktfmtCheck") {
    dependsOn(gradle.includedBuild("techdebt-gradle-plugin").task(":ktfmtCheck"))
}

tasks.named("ktfmtFormat") {
    dependsOn(gradle.includedBuild("techdebt-gradle-plugin").task(":ktfmtFormat"))
}

tasks.named("detekt") {
    dependsOn(gradle.includedBuild("techdebt-gradle-plugin").task(":detekt"))
}
