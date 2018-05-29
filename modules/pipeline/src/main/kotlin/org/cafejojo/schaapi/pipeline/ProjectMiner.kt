package org.cafejojo.schaapi.pipeline

import org.cafejojo.schaapi.models.Project

/**
 * Project miner.
 */
interface ProjectMiner<P : Project, S : SearchOptions<P>> {
    /**
     * Mines projects.
     */
    fun mine(searchOptions: S): List<P>
}
