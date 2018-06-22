package org.cafejojo.schaapi.validationpipeline

import java.io.File

/**
 * Runs a set of tests and reports on their execution results.
 */
interface TestRunner {
    /**
     * Runs the tests contained in the files in [rootDir].
     *
     * @param rootDir the root directory of the project that contains the test files
     * @param testFiles the files that contain the tests to be run
     * @param classpathDirectories the directories that need to be put on the classpath during a test execution
     * @return the results of the executed tests
     */
    fun run(rootDir: File, testFiles: List<File>, classpathDirectories: List<File> = emptyList()): TestResults
}

/**
 * A hierarchical model of the results of a test execution.
 *
 * The counts (i.e. [passCount], [ignoreCount], [failureCount]) should not include the counts from the [subResults].
 */
interface TestResults {
    /**
     * The results of the tests in a subcategory.
     */
    val subResults: Map<String, TestResults>

    /**
     * The total number of tests executed.
     */
    val totalCount: Int
    /**
     * The number of passed tests.
     */
    val passCount: Int
    /**
     * The number of ignored tests.
     */
    val ignoreCount: Int
    /**
     * The number of failed tests.
     */
    val failureCount: Int
    /**
     * Descriptions of the failed tests.
     */
    val failures: Collection<String>

    /**
     * Equals true iff no tests were executed at this level nor in the [subResults].
     */
    val isEmpty: Boolean

    /**
     * True if one of the executed tests failed.
     */
    fun hasFailures() = failureCount > 0
}
