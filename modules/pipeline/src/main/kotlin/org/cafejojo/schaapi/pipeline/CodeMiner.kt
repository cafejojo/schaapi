package org.cafejojo.schaapi.pipeline

import org.cafejojo.schaapi.models.Project

/**
 * Mine code.
 */
interface CodeMiner {
    /**
     * Mine.
     */
    fun mine(groupId: String, artifactId: String): List<Project>
}
