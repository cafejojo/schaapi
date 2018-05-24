package org.cafejojo.schaapi.pipeline.usagegraphgenerator.jimple

import org.cafejojo.schaapi.models.Node
import org.cafejojo.schaapi.models.Project
import org.cafejojo.schaapi.models.project.java.JavaProject
import org.cafejojo.schaapi.pipeline.LibraryUsageGraphGenerator
import org.cafejojo.schaapi.pipeline.usagegraphgenerator.jimple.filters.BranchStatementFilter
import org.cafejojo.schaapi.pipeline.usagegraphgenerator.jimple.filters.StatementFilter
import soot.Scene
import soot.SootClass
import soot.SootMethod
import soot.options.Options
import java.io.File

/**
 * Library usage graph generator based on Soot.
 */
object LibraryUsageGraphGenerator : LibraryUsageGraphGenerator {
    override fun generate(libraryProject: Project, userProject: Project): List<Node> {
        if (libraryProject !is JavaProject) throw IllegalArgumentException("Library project must be JavaProject.")
        if (userProject !is JavaProject) throw IllegalArgumentException("User project must be JavaProject.")

        return userProject.classNames.flatMap {
            val sootClass = createSootClass(userProject.classpath, it)

            sootClass.methods
                .filter { it.isConcrete }
                .map { generateMethodGraph(libraryProject, it) }
        }
    }

    /**
     * Creates instance of a Soot class.
     *
     * @param classpath classpath of the project
     * @param className name of the class
     * @return a Soot class
     */
    private fun createSootClass(classpath: String, className: String): SootClass {
        Scene.v().sootClassPath = arrayOf(
            System.getProperty("java.home") + "${File.separator}lib${File.separator}rt.jar",
            System.getProperty("java.home") + "${File.separator}lib${File.separator}jce.jar",
            classpath
        ).joinToString(File.pathSeparator)
        Options.v().set_whole_program(true)

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

        return ControlFlowGraphGenerator.create(methodBody)
            ?: throw IllegalStateException("Control flow graph could not be generated")
    }
}
