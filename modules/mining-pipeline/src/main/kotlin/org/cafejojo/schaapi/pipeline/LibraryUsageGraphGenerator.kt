package org.cafejojo.schaapi.pipeline

import org.cafejojo.schaapi.models.Node
import org.cafejojo.schaapi.models.Project

/**
 * Library usage graph generator.
 */
interface LibraryUsageGraphGenerator<in LP : Project, in UP : Project, out N : Node> {
    /**
     * Generates all usage graphs for the user project with respect to the library project.
     *
     * @param libraryProject library project
     * @param userProject library user project
     * @return list of graphs
     */
    fun generate(libraryProject: LP, userProject: UP): List<N>
}
