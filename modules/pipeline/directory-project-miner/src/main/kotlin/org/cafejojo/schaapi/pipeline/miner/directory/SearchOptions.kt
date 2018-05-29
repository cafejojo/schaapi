package org.cafejojo.schaapi.pipeline.miner.directory

import org.cafejojo.schaapi.pipeline.SearchOptions
import java.io.File

/**
 * Search options for a [ProjectMiner].
 */
class SearchOptions(
    /**
     * The directory containing the projects to be mined.
     */
    val directory: File
) : SearchOptions
