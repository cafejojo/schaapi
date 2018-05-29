package org.cafejojo.schaapi.pipeline.testrunner.junit

import org.cafejojo.schaapi.pipeline.TestResults
import org.cafejojo.schaapi.pipeline.TestRunner
import org.junit.runner.JUnitCore
import java.io.File
import org.junit.runner.Result as JUnitResults

/**
 * Runs JUnit 4 tests and reports on the results.
 */
class TestRunner : TestRunner {
    /**
     * Runs the tests contained in the classes recursively contained in [testDir]. The classes in [testDir] should
     * already be on the classpath.
     *
     * @param testDir the directory that contains the classes with the tests to be executed
     * @return the results of the executed tests, where each test class has its own entry in [TestResults.subResults]
     */
    override fun run(testDir: File) =
        TestResults(
            testDir.walkTopDown()
                .map { testFile ->
                    val className = testFile.toRelativeString(testDir).replace('/', '.')
                    Pair(className, JUnitCore.runClasses(this.javaClass.classLoader.loadClass(className)))
                }
                .map { (name, results) -> Pair(name, gatherResults(results)) }
                .toMap()
        )

    /**
     * Converts [JUnitResults] to [TestResults].
     *
     * @param results the JUnit results to convert
     */
    private fun gatherResults(results: JUnitResults) =
        TestResults(
            subResults = emptyMap(),
            passCount = results.runCount - results.failureCount - results.ignoreCount,
            ignoreCount = results.ignoreCount,
            failures = results.failures.map { it.testHeader }
        )
}
