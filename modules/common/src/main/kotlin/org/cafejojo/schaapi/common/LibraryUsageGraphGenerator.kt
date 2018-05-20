package org.cafejojo.schaapi.common

/**
 * Library usage graph generator.
 */
interface LibraryUsageGraphGenerator {
    /**
     * Generates usage graphs for each method in each class of the user project.
     *
     * @param libraryProject library project
     * @param userProject library user project
     * @return list of graphs
     */
    fun generate(libraryProject: Project, userProject: Project): List<Node>
}
