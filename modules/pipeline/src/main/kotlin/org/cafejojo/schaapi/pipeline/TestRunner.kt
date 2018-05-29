package org.cafejojo.schaapi.pipeline

import java.io.File

/**
 * Runs a set of tests and reports on their execution results.
 */
interface TestRunner {
    /**
     * Runs the tests contained in the files in [testDir].
     *
     * @param testDir the directory that contains the files with the tests to be executed
     * @return the results of the executed tests
     */
    fun run(testDir: File): TestResults
}

/**
 * A hierarchical model of the results of a test execution.
 *
 * The counts (i.e. [passCount], [ignoreCount], [failureCount]) should not include the counts from the [subResults].
 */
open class TestResults(
    /**
     * The results of the tests in a subcategory.
     */
    val subResults: Map<String, TestResults> = emptyMap(),
    /**
     * The number of passed tests.
     */
    val passCount: Int = 0,
    /**
     * The number of ignored tests.
     */
    val ignoreCount: Int = 0,
    /**
     * Descriptions of the failed tests.
     */
    val failures: Collection<String> = emptyList()
) {
    /**
     * The number of failed tests.
     */
    val failureCount: Int
        get() = failures.size
}
