package org.cafejojo.schaapi.miningpipeline.miner.directory

import org.cafejojo.schaapi.miningpipeline.SearchOptions
import java.io.File

/**
 * Search options for a [DirectoryProjectMiner].
 *
 * @property directory the directory containing the projects to be mined
 */
class DirectorySearchOptions(val directory: File) : SearchOptions
