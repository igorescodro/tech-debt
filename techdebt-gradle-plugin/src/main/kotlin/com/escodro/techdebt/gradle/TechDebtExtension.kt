package com.escodro.techdebt.gradle

import org.gradle.api.file.RegularFileProperty

/** Extension for configuring the tech debt report generation. */
abstract class TechDebtExtension {

    /**
     * The output file for the consolidated HTML report. Defaults to
     * `build/reports/techdebt/consolidated-report.html`.
     */
    abstract val outputFile: RegularFileProperty
}
