package org.cafejojo.schaapi.pipeline

import org.cafejojo.schaapi.models.Project

/**
 * Project miner.
 */
interface ProjectMiner {
    /**
     * Mines projects.
     */
    fun mine(): List<Project>
}
