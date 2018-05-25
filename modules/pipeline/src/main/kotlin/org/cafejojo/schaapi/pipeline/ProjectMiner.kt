package org.cafejojo.schaapi.pipeline

import org.cafejojo.schaapi.models.Project

/**
 * Project miner.
 */
interface ProjectMiner {
    /**
     * Mine projects which (likely) depend on the library with the given group id, artifact id and version (number).
     *
     * @param groupId group id of library
     * @param artifactId artifact id of library
     * @param version version (number) of library
     * @return list of projects using library
     */
    fun mine(groupId: String, artifactId: String, version: String): List<Project>
}
