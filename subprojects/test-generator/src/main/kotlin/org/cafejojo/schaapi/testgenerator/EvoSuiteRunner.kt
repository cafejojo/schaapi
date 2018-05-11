package org.cafejojo.schaapi.testgenerator

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintStream
import java.nio.charset.Charset

/**
 * EvoSuite launcher.
 *
 * Executes the EvoSuite test generator on a new process and returns once that process finishes.
 *
 * @property fullyQualifiedClassName the class that EvoSuite should generate tests for
 * @property classPath the class path on which to find the class that should be tested
 * @property outputDirectory the output directory path for the generated EvoSuite tests
 * @property generationTimeoutSeconds how long to let the EvoSuite test generator run (in seconds)
 * @property evoSuitePrintStream a stream to output EvoSuite's logs to (the caller has the responsibility to close this)
 */
class EvoSuiteRunner(
        private val fullyQualifiedClassName: String,
        private val classPath: String,
        private val outputDirectory: String,
        private val generationTimeoutSeconds: Int = 60,
        private val evoSuitePrintStream: PrintStream? = null
) {
    /**
     * Runs the EvoSuite test generator in a new process.
     */
    fun run() = receiveProcessOutput(buildProcess())

    private fun buildProcess() = ProcessBuilder(
            "java",
            "-cp", System.getProperty("java.class.path"),
            "org.evosuite.EvoSuite",
            "-class", fullyQualifiedClassName,
            "-base_dir", outputDirectory,
            "-projectCP", classPath,
            "-Dsearch_budget=$generationTimeoutSeconds",
            "-Dstatistics_backend=NONE"
    ).start()

    private fun receiveProcessOutput(process: Process) {
        val bufferedReader = BufferedReader(InputStreamReader(process.inputStream))
        var lastLine = ""

        bufferedReader.forEachLine {
            evoSuitePrintStream?.println(it)
            lastLine = it
        }

        process.waitFor()

        if (!lastLine.toLowerCase().contains("computation finished")) {
            throw EvoSuiteRuntimeException("EvoSuite did not terminate successfully. " +
                    "The last line of its output reads: $lastLine")
        }

        if (process.exitValue() != 0) {
            val errorOutput = String(process.errorStream.readBytes(), Charset.defaultCharset())
            throw EvoSuiteRuntimeException("EvoSuite exited with non-zero exit code: " +
                    "${process.exitValue()} - $errorOutput")
        }
    }
}

/**
 * A [RuntimeException] occurring during the execution of the EvoSuite test generation tool.
 */
class EvoSuiteRuntimeException(message: String? = null) : RuntimeException(message)