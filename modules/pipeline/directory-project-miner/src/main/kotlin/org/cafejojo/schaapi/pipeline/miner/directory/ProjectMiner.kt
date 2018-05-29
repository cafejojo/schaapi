package org.cafejojo.schaapi.pipeline.miner.directory

import org.cafejojo.schaapi.models.Project
import org.cafejojo.schaapi.pipeline.ProjectMiner
import java.io.File

/**
 * Mines the projects in the given directory.
 *
 * @property projectPacker the packer that transforms [File]s into projects. It may be invoked on both directories and
 * files.
 */
class ProjectMiner<P : Project>(private val projectPacker: (File) -> P) : ProjectMiner<P, SearchOptions<P>> {
    override fun mine(searchOptions: SearchOptions<P>) =
        searchOptions.directory.listFiles()?.map { projectPacker(it) } ?: emptyList()
}
