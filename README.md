# 💸 TechDebt

[![Build status](https://github.com/igorescodro/tech-debt/actions/workflows/build.yml/badge.svg)](https://github.com/igorescodro/tech-debt/actions/workflows/build.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.igorescodro/techdebt-annotations.svg)](https://central.sonatype.com/search?q=g:io.github.igorescodro+techdebt)

TechDebt is a Kotlin Symbol Processing (KSP) tool designed to help developers track and visualize technical debt
directly within their codebase. By using the `@TechDebt` annotation, you can document technical debt, link it to
tickets, and assign priority levels. The tool then generates a comprehensive HTML report summarizing all marked
technical debt.

## Goal

The primary goal of TechDebt is to make technical debt visible and manageable. Instead of letting TODOs get lost in the
code, TechDebt allows you to:
- Formally document technical debt at the class, function, or property level.
- Categorize debt by priority (Low, Medium, High).
- Link code smells or shortcuts to issue tracker tickets.
- Generate a visual report to share with the team or stakeholders.

## How it Works

TechDebt uses **KSP (Kotlin Symbol Processing)** to scan your source code for the `@TechDebt` annotation during the
compilation process. It collects all annotated symbols and their metadata to produce an HTML report.

## Report example

<img src="assets/screenshot-report-example.png" alt="Screenshot of TechDebt report example">

## Download

TechDebt is available in the Sonatype Snapshot repository. To use it in your project, add the snapshot repository to 
your `repositories` block and the following dependencies:

```kotlin
repositories {
    mavenCentral()
    maven {
        url = uri("https://central.sonatype.com/repository/maven-snapshots/")
    }
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.igorescodro:techdebt-annotations:0.1.0-beta01")
        }
    }
}

dependencies {
    add("kspAndroid", "io.github.igorescodro:techdebt-processor:0.1.0-beta01")
    add("kspIosSimulatorArm64", "io.github.igorescodro:techdebt-processor:0.1.0-beta01")
    add("kspIosX64", "io.github.igorescodro:techdebt-processor:0.1.0-beta01")
    add("kspIosArm64", "io.github.igorescodro:techdebt-processor:0.1.0-beta01")
    // Add any other platform target you use in your project, for example kspDesktop
}
```

For Android or JVM only projects:

```kotlin
dependencies {
    implementation("io.github.igorescodro:techdebt-annotations:0.1.0-beta01")
    ksp("io.github.igorescodro:techdebt-processor:0.1.0-beta01")
}
```

## How to Use

### 1. Annotate your code
Use the `@TechDebt` annotation to mark areas of technical debt:

```kotlin
@TechDebt(
    ticket = "JIRA-123",
    description = "Quick fix to handle edge case, needs proper refactoring.",
    priority = Priority.HIGH
)
fun complexMethod() {
    // ...
}
```

The annotation can be applied to:
- Classes
- Functions
- Properties

### 2. Generate the report
The report is automatically generated during the build process. Simply run your Gradle build:

```bash
./gradlew build
```

The HTML report will be generated in the build directory:
`build/generated/ksp/main/resources/techdebt/report.html`

## Features

- **HTML Report**: A clean, easy-to-read summary of all technical debt in your project.
- **Priority Levels**: Support for `LOW`, `MEDIUM`, and `HIGH` priority levels (and `NONE`).
- **Ticket Linking**: Keep track of related tickets in your issue tracking system.

## License

```
Copyright 2026 Igor Escodro

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
