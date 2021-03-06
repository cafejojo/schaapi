package org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple

import mu.KLogging
import org.cafejojo.schaapi.miningpipeline.LibraryUsageGraphGenerator
import org.cafejojo.schaapi.miningpipeline.patternfilter.jimple.libraryusagefilters.BranchStatementFilter
import org.cafejojo.schaapi.miningpipeline.patternfilter.jimple.libraryusagefilters.RecursiveGotoFilter
import org.cafejojo.schaapi.miningpipeline.patternfilter.jimple.libraryusagefilters.StatementFilter
import org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.processors.UserUsageProcessor
import org.cafejojo.schaapi.models.DfsIterator
import org.cafejojo.schaapi.models.libraryusagegraph.jimple.JimpleNode
import org.cafejojo.schaapi.models.libraryusagegraph.jimple.SootNameEquivalenceChanger
import org.cafejojo.schaapi.models.project.JavaProject
import soot.Body
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
class JimpleLibraryUsageGraphGenerator : LibraryUsageGraphGenerator<JavaProject, JavaProject, JimpleNode> {
    companion object : KLogging() {
        init {
            Options.v().set_verbose(true)
            SootNameEquivalenceChanger.activate()

            Options.v().set_whole_program(true)
            Options.v().set_allow_phantom_refs(true)
        }
    }

    /**
     * Contains the statistics of this object's method generation process.
     */
    val lugStatistics = LugStatistics()

    override fun generate(libraryProject: JavaProject, userProject: JavaProject): List<JimpleNode> {
        logger.debug { "Generating library-usage graphs for ${userProject.projectDir.absolutePath}." }

        setUpSootEnvironment(libraryProject, userProject)

        return userProject.classNames.flatMap { className ->
            val sootClass = createSootClass(className)

            sootClass.methods
                .filter { it.isConcrete }.also { lugStatistics.concreteMethods += it.size }
                .mapNotNull { generateMethodGraph(libraryProject, it) }
                .filter { methodGraph ->
                    DfsIterator(methodGraph).asSequence().toList().any {
                        it !is JimpleNode || !isMeaninglessStatementWithoutContext(it.statement)
                    }
                }
        }
    }

    /**
     * Sets up the static Soot analysis environment.
     *
     * @param libraryProject library project containing the classes defined by the library
     * @param userProject user project to be analysed
     */
    private fun setUpSootEnvironment(libraryProject: JavaProject, userProject: JavaProject) {
        Scene.v().sootClassPath = arrayOf(
            System.getProperty("java.home") + "${File.separator}lib${File.separator}rt.jar",
            System.getProperty("java.home") + "${File.separator}lib${File.separator}jce.jar",
            libraryProject.classDir.absolutePath,
            userProject.classpath
        ).joinToString(File.pathSeparator)
        Scene.v().loadNecessaryClasses()
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
    @Suppress("TooGenericExceptionCaught") // Soot throws generic exceptions
    private fun generateMethodGraph(libraryProject: JavaProject, method: SootMethod): JimpleNode? {
        val methodBody: Body
        try {
            methodBody = method.retrieveActiveBody()
        } catch (e: RuntimeException) {
            logger.warn { e.message }
            return null
        }
        lugStatistics.allStatements += methodBody.units.size

        listOf(
            StatementFilter(libraryProject),
            BranchStatementFilter(libraryProject),
            RecursiveGotoFilter()
        ).forEach { it.apply(methodBody) }

        listOf(
            UserUsageProcessor(libraryProject)
        ).forEach { it.process(methodBody) }

        lugStatistics.validStatements += methodBody.units.size
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
