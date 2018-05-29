package org.cafejojo.schaapi.pipeline.miner.directory

import org.cafejojo.schaapi.models.Project
import org.cafejojo.schaapi.pipeline.SearchOptions
import java.io.File

/**
 * Search options for a [ProjectMiner].
 *
 * @property directory the directory containing the projects to be mined
 */
class SearchOptions<P : Project>(val directory: File) : SearchOptions<P>
