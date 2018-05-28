package org.cafejojo.schaapi.pipeline.usagegraphgenerator.jimple

import org.cafejojo.schaapi.models.DfsIterator
import org.cafejojo.schaapi.models.Node
import org.cafejojo.schaapi.models.Project
import org.cafejojo.schaapi.models.libraryusagegraph.jimple.JimpleNode
import org.cafejojo.schaapi.models.project.java.JavaProject
import org.cafejojo.schaapi.pipeline.LibraryUsageGraphGenerator
import org.cafejojo.schaapi.pipeline.usagegraphgenerator.jimple.filters.BranchStatementFilter
import org.cafejojo.schaapi.pipeline.usagegraphgenerator.jimple.filters.StatementFilter
import soot.Scene
import soot.SootClass
import soot.SootMethod
import soot.jimple.GotoStmt
import soot.jimple.IfStmt
import soot.jimple.Jimple
import soot.jimple.ReturnStmt
import soot.jimple.ReturnVoidStmt
import soot.jimple.Stmt
import soot.jimple.SwitchStmt
import soot.options.Options
import java.io.File

/**
 * Library usage graph generator based on Soot.
 */
object LibraryUsageGraphGenerator : LibraryUsageGraphGenerator {
    override fun generate(libraryProject: Project, userProject: Project): List<Node> {
        if (libraryProject !is JavaProject) throw IllegalArgumentException("Library project must be JavaProject.")
        if (userProject !is JavaProject) throw IllegalArgumentException("User project must be JavaProject.")

        Scene.v().sootClassPath = arrayOf(
            System.getProperty("java.home") + "${File.separator}lib${File.separator}rt.jar",
            System.getProperty("java.home") + "${File.separator}lib${File.separator}jce.jar",
            libraryProject.classDir.absolutePath,
            userProject.classpath
        ).joinToString(File.pathSeparator)
        Options.v().set_whole_program(true)
        Options.v().set_allow_phantom_refs(true)
        Scene.v().loadNecessaryClasses()

        return userProject.classNames.flatMap {
            val sootClass = createSootClass(it)

            sootClass.methods
                .filter { it.isConcrete }
                .map { generateMethodGraph(libraryProject, it) }
                .filter {
                    !DfsIterator(it).asSequence().toList().all {
                        it is JimpleNode && isMeaninglessStatementWithoutContext(it.statement)
                    }
                }
        }
    }

    /**
     * Creates instance of a [SootClass].
     *
     * @param className name of the class
     * @return a [SootClass]
     */
    private fun createSootClass(className: String) =
        Scene.v().forceResolve(className, SootClass.BODIES).apply { setApplicationClass() }

    /**
     * Generates a library usage graph for a method.
     *
     * @param libraryProject library project containing the classes defined by the library
     * @param method method for which to generate the graph
     * @return library usage graph
     */
    private fun generateMethodGraph(libraryProject: JavaProject, method: SootMethod): JimpleNode {
        val methodBody = method.retrieveActiveBody()
        val filters = listOf(StatementFilter(libraryProject), BranchStatementFilter(libraryProject))
        filters.forEach { it.apply(methodBody) }

        if (methodBody.units.isEmpty()) methodBody.units.add(Jimple.v().newReturnVoidStmt())

        return ControlFlowGraphGenerator.create(methodBody)
            ?: throw IllegalStateException("Control flow graph could not be generated")
    }

    private fun isMeaninglessStatementWithoutContext(statement: Stmt) = when (statement) {
        is GotoStmt -> true
        is ReturnVoidStmt -> true
        is ReturnStmt -> true
        is SwitchStmt -> true
        is IfStmt -> true
        else -> false
    }
}
