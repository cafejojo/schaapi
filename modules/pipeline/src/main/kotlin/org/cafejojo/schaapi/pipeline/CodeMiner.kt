package org.cafejojo.schaapi.pipeline

import org.cafejojo.schaapi.models.Project

/**
 * Code miner.
 */
interface CodeMiner {
    /**
     * Mine projects which (likely) depend on a software library with the given group id, artifact id and version.
     *
     * @param groupId group id of library
     * @param artifactId artifact id of library
     * @param version version of library
     * @return list of projects using library
     */
    fun mine(groupId: String, artifactId: String, version: String): List<Project>
}
