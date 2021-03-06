package org.cafejojo.schaapi.miningpipeline.testgenerator.jimpleevosuite

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.PrintStream
import java.nio.charset.Charset

/**
 * EvoSuite launcher.
 *
 * Executes the EvoSuite test generator on a new process and returns once that process finishes.
 *
 * @property fullyQualifiedClassPrefix the prefix of the classes that EvoSuite should generate tests for
 * @property classpath the classpath on which to find the class that should be tested
 * @property outputDirectory the output directory path for the generated EvoSuite tests
 * @property generationTimeoutSeconds how long to let the EvoSuite test generator run (in seconds)
 * @property processStandardStream a stream to output EvoSuite's standard messages to
 * @property processErrorStream a stream to output EvoSuite's error messages to
 */
internal class EvoSuiteRunner(
    private val fullyQualifiedClassPrefix: String,
    private val classpath: String,
    private val outputDirectory: String,
    private val generationTimeoutSeconds: Int = 60,
    private val processStandardStream: PrintStream? = null,
    private val processErrorStream: PrintStream? = null
) {
    /**
     * Runs the EvoSuite test generator in a new process.
     */
    fun run() = receiveProcessOutput(buildProcess())

    private fun buildProcess() = ProcessBuilder(
        "java",
        "-cp", System.getProperty("java.class.path"),
        "org.evosuite.EvoSuite",
        "-prefix", fullyQualifiedClassPrefix,
        "-base_dir", outputDirectory,
        "-projectCP", classpath,
        "-Dno_runtime_dependency=true",
        "-Dvirtual_fs=false",
        "-Dvirtual_net=false",
        "-Dsearch_budget=$generationTimeoutSeconds",
        "-Dstatistics_backend=NONE",
        "-Doutput_granularity=TESTCASE"
    ).apply {
        environment()["JAVA_HOME"] = System.getProperty("java.home")
    }.start()

    private fun receiveProcessOutput(process: Process) {
        val lastLine = pipeAllLines(process.inputStream, processStandardStream)
        pipeAllLines(process.errorStream, processErrorStream)

        process.waitFor()

        if (!lastLine.toLowerCase().contains("computation finished")) {
            throw EvoSuiteRuntimeException(
                "EvoSuite did not terminate successfully. The last line of its output reads: \"$lastLine\""
            )
        }

        if (process.exitValue() != 0) {
            val errorOutput = String(process.errorStream.readBytes(), Charset.defaultCharset())
            throw EvoSuiteRuntimeException(
                "EvoSuite exited with non-zero exit code: ${process.exitValue()}\n$errorOutput"
            )
        }
    }

    private fun pipeAllLines(input: InputStream, output: PrintStream?): String {
        var lastLine = ""

        BufferedReader(InputStreamReader(input)).forEachLine {
            output?.println(it)
            lastLine = it
        }

        return lastLine
    }
}

/**
 * A [RuntimeException] occurring during the execution of the EvoSuite test generation tool.
 */
internal class EvoSuiteRuntimeException(message: String? = null) : RuntimeException(message)
