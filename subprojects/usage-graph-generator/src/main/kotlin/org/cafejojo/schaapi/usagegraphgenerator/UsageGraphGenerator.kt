package org.cafejojo.schaapi.usagegraphgenerator

import org.cafejojo.schaapi.common.Node
import org.cafejojo.schaapi.usagegraphgenerator.filters.StatementFilter
import soot.Scene
import soot.SootClass
import soot.options.Options
import java.io.File

/**
 * Placeholder main method.
 */
fun main(args: Array<String>) {
    println("I am the usage graph generator!")
}

/**
 * Generates a library usage graph.
 *
 * @param classPath class path of the library user
 * @param className class name of the user class for which to generate the graph
 * @param methodName method name of the user method for which to generate the graph
 * @return library usage graph
 */
fun generateLibraryUsageGraph(classPath: String, className: String, methodName: String): Node {
    Options.v().set_soot_classpath(
        arrayOf(
            System.getProperty("java.home") + "${File.separator}lib${File.separator}rt.jar",
            System.getProperty("java.home") + "${File.separator}lib${File.separator}jce.jar",
            classPath
        ).joinToString(File.pathSeparator)
    )

    Scene.v().loadNecessaryClasses()

    val sootClass = Scene.v().forceResolve(className, SootClass.BODIES).apply {
        setApplicationClass()
    }

    val methodBody = sootClass.getMethodByName(methodName).retrieveActiveBody().also { body ->
        body.units.snapshotIterator().forEach { if (!StatementFilter.retain(it)) body.units.remove(it) }
    }

    return ControlFlowGraphCreator.create(methodBody)
        ?: throw IllegalStateException("Control flow graph could not be generated")
}
