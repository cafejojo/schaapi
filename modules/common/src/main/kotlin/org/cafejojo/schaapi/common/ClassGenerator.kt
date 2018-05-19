package org.cafejojo.schaapi.common

/**
 * Generates a class and allows methods to be generated for said class.
 */
interface ClassGenerator {
    /**
     * Generates a method containing the statements in the nodes.
     *
     * @param methodName the name the method should have
     * @param nodes a list of [Node]s which should be converted into a method
     */
    fun generateMethod(methodName: String, nodes: List<Node>)

    /**
     * Writes the generated class to a class file.
     *
     * @param targetDirectory the path to the base directory in which to place the class file structure
     */
    fun writeToFile(targetDirectory: String)
}
