package org.cafejojo.schaapi.common

/**
 * Library usage graph generator.
 */
interface LibraryUsageGraphGenerator {
    /**
     * Generates all usage graphs for the user project with respect to the library project.
     *
     * @param libraryProject library project
     * @param userProject library user project
     * @return list of graphs
     */
    fun generate(libraryProject: Project, userProject: Project): List<Node>
}
