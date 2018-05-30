package org.cafejojo.schaapi.miningpipeline.miner.directory

import org.cafejojo.schaapi.models.Project
import org.cafejojo.schaapi.miningpipeline.ProjectMiner
import java.io.File

/**
 * Mines the projects in the given directory.
 *
 * @property projectPacker the packer that transforms [File]s into projects. It may be invoked on both directories and
 * files.
 */
class ProjectMiner<P : Project>(private val projectPacker: (File) -> P) : ProjectMiner<DirectorySearchOptions, P> {
    override fun mine(searchOptions: DirectorySearchOptions) =
        searchOptions.directory.listFiles()?.map { projectPacker(it) } ?: emptyList()
}