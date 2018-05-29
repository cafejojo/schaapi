package org.cafejojo.schaapi.pipeline.miner.directory

import org.cafejojo.schaapi.models.Project
import org.cafejojo.schaapi.pipeline.ProjectMiner
import java.io.File

/**
 * Mines the projects in the given directory.
 *
 * @property projectPacker the packer that transforms [File]s into projects. It be invoked on both directories and
 * files.
 */
class ProjectMiner(private val projectPacker: (File) -> Project) : ProjectMiner<SearchOptions> {
    override fun mine(searchOptions: SearchOptions) =
        searchOptions.directory.listFiles()?.map { projectPacker(it) } ?: emptyList()
}
