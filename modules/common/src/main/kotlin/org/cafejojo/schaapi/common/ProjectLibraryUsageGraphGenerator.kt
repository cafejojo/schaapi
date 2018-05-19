package org.cafejojo.schaapi.common

/**
 * Library usage graph generator.
 */
interface ProjectLibraryUsageGraphGenerator {
    /**
     * Generates usage graphs for each method in each class of the user project.
     *
     * @param libraryProject library project
     * @param userProject library user project
     * @return list of list of graphs
     */
    fun generate(libraryProject: JavaProject, userProject: JavaProject): List<List<Node>>
}
