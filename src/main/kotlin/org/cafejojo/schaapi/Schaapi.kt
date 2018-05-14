package org.cafejojo.schaapi

import org.cafejojo.schaapi.patterndetector.PathEnumerator
import org.cafejojo.schaapi.patterndetector.PatternDetector
import org.cafejojo.schaapi.projectcompiler.JavaMavenProject
import org.cafejojo.schaapi.testgenerator.EvoSuiteRunner
import org.cafejojo.schaapi.testgenerator.SootClassGenerator
import org.cafejojo.schaapi.testgenerator.SootClassWriter
import org.cafejojo.schaapi.usagegraphgenerator.SootNode
import org.cafejojo.schaapi.usagegraphgenerator.SootProjectLibraryUsageGraphGenerator
import soot.jimple.Stmt
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

private const val MIN_ARG_COUNT = 3
private const val PATTERN_DETECTOR_MINIMUM = 3

/**
 * Runs the complete first phase of the Schaapi pipeline.
 *
 * @param args the path to the output directory, the path to the library project, and the paths to the user projects
 */
@SuppressWarnings("UnsafeCast")
fun main(args: Array<String>) {
    if (args.size < MIN_ARG_COUNT) {
        println("Expected at least 3 arguments but received ${args.size}.")
        return
    }
    if (Files.isRegularFile(Paths.get(args[0]))) {
        println("Output directory is actually a file.")
        return
    }

    val output = File(args[0])
    val outputPatterns = output.resolve("patterns/")
    val outputTests = output.resolve("tests/")
    val library = JavaMavenProject(File(args[1]))
    val users = args.drop(2).map { JavaMavenProject(File(it)) }

    library.compile()
    users.forEach { it.compile() }

    val graphGenerator = SootProjectLibraryUsageGraphGenerator
    val userGraphs = users.map { graphGenerator.generate(library, it) }
    val userPaths = userGraphs.flatMap { it.flatMap { it.flatMap { PathEnumerator(it).enumerate() } } }

    val patterns = PatternDetector(userPaths, PATTERN_DETECTOR_MINIMUM).findFrequentSequences()

    val classGenerator = SootClassGenerator("RegressionTest")
    patterns.forEachIndexed { index, pattern ->
        classGenerator.generateMethod("pattern$index", pattern.map { unit -> (unit as SootNode).unit as Stmt })
    }

    SootClassWriter.writeToFile(classGenerator.sootClass, outputPatterns.absolutePath)

    EvoSuiteRunner("RegressionTest", outputPatterns.absolutePath, outputTests.absolutePath).run()
}
