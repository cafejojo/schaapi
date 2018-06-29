package org.cafejojo.schaapi.miningpipeline

import org.cafejojo.schaapi.models.Project

/**
 * Project miner.
 */
interface ProjectMiner<in S : SearchOptions, out P : Project> {
    /**
     * Mines projects.
     */
    fun mine(searchOptions: S): Iterable<P>
}
