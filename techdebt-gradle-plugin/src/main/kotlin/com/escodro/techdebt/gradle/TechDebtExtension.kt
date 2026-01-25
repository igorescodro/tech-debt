package com.escodro.techdebt.gradle

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property

/** Extension for configuring the tech debt report generation. */
abstract class TechDebtExtension {

    /**
     * The output file for the consolidated HTML report. Defaults to
     * `build/reports/techdebt/consolidated-report.html`.
     */
    abstract val outputFile: RegularFileProperty

    /** Whether to collect suppressed rules from `@Suppress` annotations. Defaults to `false`. */
    abstract val collectSuppress: Property<Boolean>
}
