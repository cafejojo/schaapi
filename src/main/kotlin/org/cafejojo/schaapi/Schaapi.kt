package org.cafejojo.schaapi

import org.cafejojo.schaapi.patterndetector.PathEnumerator
import org.cafejojo.schaapi.patterndetector.PatternDetector
import org.cafejojo.schaapi.projectcompiler.JavaMavenProject
import org.cafejojo.schaapi.usagegraphgenerator.SootProjectLibraryUsageGraphGenerator
import java.io.File

private const val PATTERN_DETECTOR_MINIMUM = 3

/**
 * Runs the complete first phase of the Schaapi pipeline.
 *
 * @param args the path to the library project and the paths to the user projects
 */
fun main(args: Array<String>) {
    if (args.size < 2) {
        println("Expected at least 2 arguments but received ${args.size}.")
        return
    }

    val library = JavaMavenProject(File(args[0]))
    val users = args.takeLast(args.size - 1).map { JavaMavenProject(File(it)) }

    library.compile()
    users.forEach { it.compile() }

    val graphGenerator = SootProjectLibraryUsageGraphGenerator
    val userGraphs = users.map { graphGenerator.generate(library, it) }
    val userPaths = userGraphs.flatMap { it.flatMap { it.flatMap { PathEnumerator(it).enumerate() } } }

    /*val patterns = */PatternDetector(userPaths, PATTERN_DETECTOR_MINIMUM).findFrequentSequences()

    // Generate testable class here
}
