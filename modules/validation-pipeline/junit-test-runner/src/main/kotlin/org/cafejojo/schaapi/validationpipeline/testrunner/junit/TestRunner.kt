package org.cafejojo.schaapi.validationpipeline.testrunner.junit

import org.cafejojo.schaapi.validationpipeline.TestResults
import org.cafejojo.schaapi.validationpipeline.TestRunner
import org.junit.runner.JUnitCore
import java.io.File
import java.net.URLClassLoader
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
     * @param testFiles the files that contain the tests to be run
     * @param classpathDirectories the directories that need to be put on the classpath during a test execution
     * @return the results of the executed tests, where each test class has its own entry in [TestResults.subResults]
     */
    override fun run(rootDir: File, testFiles: List<File>, classpathDirectories: List<File>): TestResults {
        require(rootDir.exists()) { "Given test directory does not exist." }
        require(rootDir.isDirectory) { "Given test directory is not a directory." }
        require(testFiles.all { it.exists() }) { "Not all given test files exist." }
        require(testFiles.all { it.isFile }) { "Not all test files are files." }
        require(testFiles.all { it.extension == "class" }) { "Not all test files are classes." }

        return TestResults(
            testFiles.map { testFile ->
                val className = testFile.toRelativeString(rootDir)
                    .replace(Regex("[/\\\\]"), ".")
                    .removeSuffix(".class")
                val classLoader = URLClassLoader(
                    classpathDirectories.map { it.toURI().toURL() }.toTypedArray() +
                        testFile.parentFile.toURI().toURL()
                )
                className to gatherResults(JUnitCore.runClasses(classLoader.loadClass(className)), testFile)
            }.toMap()
        )
    }

    /**
     * Converts [JUnitResults] to [TestResults].
     *
     * @param results the JUnit results to convert
     */
    private fun gatherResults(results: JUnitResults, testClass: File) =
        TestResults(
            localTotalCount = results.runCount,
            localPassCount = results.runCount - results.failureCount,
            localIgnoreCount = results.ignoreCount,
            localFailures = results.failures.map { testClass to it.message }.toMap()
        )
}

/**
 * A type of [TestResults] adjusted for the results returned by JUnit.
 */
data class TestResults(
    override val subResults: Map<String, TestResults> = emptyMap(),
    val localTotalCount: Int = 0,
    val localPassCount: Int = 0,
    val localIgnoreCount: Int = 0,
    val localFailures: Map<File, String> = mapOf()
) : TestResults {
    override val totalCount: Int
        get() = localTotalCount + subResults.map { (_, subResult) -> subResult.totalCount }.sum()

    override val passCount: Int
        get() = localPassCount + subResults.map { (_, subResult) -> subResult.passCount }.sum()

    override val ignoreCount: Int
        get() = localIgnoreCount + subResults.map { (_, subResult) -> subResult.ignoreCount }.sum()

    override val failures: Map<File, String>
        get() = localFailures + subResults.values.fold(mapOf<File, String>()) { acc, testResults ->
            acc + testResults.failures
        }

    override val failureCount: Int
        get() = failures.size

    override val isEmpty: Boolean
        get() = subResults.keys.all { it.isEmpty() } && totalCount == 0
}
