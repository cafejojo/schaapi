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
     * Runs the tests contained in the classes recursively contained in [rootDir]. The classes in [rootDir] should
     * already be on the classpath.
     *
     * @param rootDir the directory that contains the classes with the tests to be executed
     * @return the results of the executed tests, where each test class has its own entry in [TestResults.subResults]
     */
    override fun run(rootDir: File, testFiles: List<File>): TestResults {
        require(rootDir.exists()) { "Given test directory does not exist." }
        require(rootDir.isDirectory) { "Given test directory is not a directory." }
        require(testFiles.all { it.exists() }) { "Not all given test files exist." }
        require(testFiles.all { it.isFile }) { "Not all test files are files." }
        require(testFiles.all { it.extension == "class" }) { "Not all test files are classes." }

        return TestResults(
            testFiles
                .map { testFile ->
                    val className = testFile.toRelativeString(rootDir)
                        .replace(Regex("[/\\\\]"), ".")
                        .removeSuffix(".class")
                    Pair(className, JUnitCore.runClasses(this.javaClass.classLoader.loadClass(className)))
                }
                .map { (name, results) -> Pair(name, gatherResults(results)) }
                .toMap()
        )
    }

    /**
     * Converts [JUnitResults] to [TestResults].
     *
     * @param results the JUnit results to convert
     */
    private fun gatherResults(results: JUnitResults) =
        TestResults(
            subResults = emptyMap(),
            totalCount = results.runCount,
            passCount = results.runCount - results.failureCount,
            ignoreCount = results.ignoreCount,
            failures = results.failures.map { it.testHeader }
        )
}

/**
 * A type of [TestResults] adjusted for the results returned by JUnit.
 */
class TestResults(
    override val subResults: Map<String, TestResults> = emptyMap(),
    override val totalCount: Int = 0,
    override val passCount: Int = 0,
    override val ignoreCount: Int = 0,
    override val failures: Collection<String> = emptyList()
) : TestResults {
    override val failureCount: Int
        get() = failures.size

    override val isEmpty: Boolean
        get() = subResults.keys.all { it.isEmpty() } && totalCount == 0
}
