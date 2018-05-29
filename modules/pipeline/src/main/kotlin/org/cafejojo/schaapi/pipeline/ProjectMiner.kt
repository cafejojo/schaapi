package org.cafejojo.schaapi.pipeline

import org.cafejojo.schaapi.models.Project

/**
 * Project miner.
 */
interface ProjectMiner<S : SearchOptions, P : Project> {
    /**
     * Mines projects.
     */
    fun mine(searchOptions: S): List<P>
}
