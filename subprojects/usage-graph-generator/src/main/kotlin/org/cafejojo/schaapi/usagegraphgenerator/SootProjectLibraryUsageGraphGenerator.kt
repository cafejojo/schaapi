package org.cafejojo.schaapi.usagegraphgenerator

import org.cafejojo.schaapi.common.JavaProject
import org.cafejojo.schaapi.common.Node
import org.cafejojo.schaapi.common.ProjectLibraryUsageGraphGenerator
import org.cafejojo.schaapi.usagegraphgenerator.filters.BranchStatementFilter
import org.cafejojo.schaapi.usagegraphgenerator.filters.StatementFilter
import soot.Scene
import soot.SootClass
import soot.SootMethod
import soot.options.Options
import java.io.File

/**
 * Placeholder main method.
 */
fun main(args: Array<String>) {
    println("I am the usage graph generator!")
}

/**
 * Library usage graph generator based on Soot.
 */
object SootProjectLibraryUsageGraphGenerator : ProjectLibraryUsageGraphGenerator {
    /**
     * Generates usage graphs for each method in each class of the user project.
     *
     * @param libraryProject library project
     * @param userProject library user project
     * @return list of list of graphs
     */
    override fun generate(libraryProject: JavaProject, userProject: JavaProject) =
        userProject.classNames.map {
            val sootClass = createSootClass(userProject.classpath, it)

            sootClass.methods.map { generateMethodGraph(libraryProject, it) }
        }

    /**
     * Creates instance of a Soot class.
     *
     * @param classpath classpath of the project
     * @param className name of the class
     * @return a Soot class
     */
    private fun createSootClass(classpath: String, className: String): SootClass {
        Options.v().set_soot_classpath(
            arrayOf(
                System.getProperty("java.home") + "${File.separator}lib${File.separator}rt.jar",
                System.getProperty("java.home") + "${File.separator}lib${File.separator}jce.jar",
                classpath
            ).joinToString(File.pathSeparator)
        )

        Scene.v().loadNecessaryClasses()

        return Scene.v().forceResolve(className, SootClass.BODIES).apply { setApplicationClass() }
    }

    /**
     * Generates a library usage graph for a method.
     *
     * @param libraryProject library project containing the classes defined by the library
     * @param method method for which to generate the graph
     * @return library usage graph
     */
    private fun generateMethodGraph(libraryProject: JavaProject, method: SootMethod): Node {
        val methodBody = method.retrieveActiveBody()
        val filters = listOf(StatementFilter(libraryProject), BranchStatementFilter(libraryProject))
        filters.forEach { it.apply(methodBody) }

        return ControlFlowGraphCreator.create(methodBody)
            ?: throw IllegalStateException("Control flow graph could not be generated")
    }
}
