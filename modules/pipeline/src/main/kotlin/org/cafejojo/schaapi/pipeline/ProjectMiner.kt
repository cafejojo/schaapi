package org.cafejojo.schaapi.pipeline

import org.cafejojo.schaapi.models.Project

/**
 * Project miner.
 */
interface ProjectMiner<S : SearchOptions> {
    /**
     * Mines projects.
     */
    fun mine(searchOptions: S): List<Project>
}
