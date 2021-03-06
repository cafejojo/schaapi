package org.cafejojo.schaapi.miningpipeline.miner.directory

import mu.KLogging
import org.cafejojo.schaapi.miningpipeline.ProjectMiner
import org.cafejojo.schaapi.models.Project
import java.io.File
import kotlin.streams.toList

/**
 * Mines the projects in the given directory.
 *
 * @property projectPacker the packer that transforms [File]s into projects. It may be invoked on both directories and
 * files.
 */
class DirectoryProjectMiner<P : Project>(private val projectPacker: (File) -> P) :
    ProjectMiner<DirectorySearchOptions, P> {
    companion object : KLogging()

    override fun mine(searchOptions: DirectorySearchOptions) =
        searchOptions.directory.listFiles()
            ?.filterNot { it.isHidden }
            ?.parallelStream()
            ?.map {
                logger.debug { "Packing user project ${it.absolutePath}." }
                projectPacker(it)
            }
            ?.toList()
            ?: emptyList()
}
