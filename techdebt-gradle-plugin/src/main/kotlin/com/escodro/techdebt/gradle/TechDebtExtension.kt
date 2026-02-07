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

    /** Whether to collect TODO/FIXME comments. Defaults to `false`. */
    abstract val collectComments: Property<Boolean>

    /** Whether to enable Git metadata (e.g. last modified date). Defaults to `false`. */
    abstract val enableGitMetadata: Property<Boolean>

    /**
     * The base URL for the tickets. If set, the ticket property in the HTML report will be a link.
     */
    abstract val baseTicketUrl: Property<String>
}
