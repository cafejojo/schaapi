package org.cafejojo.schaapi.testgenerator

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.URLClassLoader


/**
 * EvoSuite launcher.
 *
 * Executes the EvoSuite test generator on a new process and returns once that process finishes.
 *
 * @property fullyQualifiedClassName the class that EvoSuite should generate tests for
 * @property classPath the class path on which to find the class that should be tested
 * @property outputDirectory the output directory path for the generated EvoSuite tests
 * @property generationTimeoutSeconds how long to let the EvoSuite test generator run (in seconds)
 * @property logEvoSuiteOutput whether to output EvoSuite logs to [System.out]
 */
class EvoSuiteRunner(
        private val fullyQualifiedClassName: String,
        private val classPath: String,
        private val outputDirectory: String,
        private val generationTimeoutSeconds: Int = 60,
        private val logEvoSuiteOutput: Boolean = false
) {
    private val currentClassPath: String

    init {
        val classLoader = ClassLoader.getSystemClassLoader()
        val urls = (classLoader as URLClassLoader).urLs
        currentClassPath = urls.joinToString(separator = File.pathSeparator) { it.file }
    }

    /**
     * Runs the EvoSuite test generator in a new process.
     */
    fun run() {
        val process = buildProcess()
        receiveProcessOutput(process)
    }

    private fun buildProcess(): Process {
        val processBuilder = ProcessBuilder(
                "java", "-cp", currentClassPath, "org.evosuite.EvoSuite",
                "-class", fullyQualifiedClassName,
                "-base_dir", outputDirectory,
                "-projectCP", classPath,
                "-Dsearch_budget=$generationTimeoutSeconds",
                "-Dstopping_condition=MaxTime"
        )

        return processBuilder.start()
    }

    private fun receiveProcessOutput(process: Process) {
        val bufferedReader = BufferedReader(InputStreamReader(process.inputStream))
        var line = bufferedReader.readLine()
        while (line != null) {
            if (logEvoSuiteOutput) println(line)

            line = bufferedReader.readLine()
        }

        if (process.exitValue() != 0) {
            throw EvoSuiteRuntimeException("EvoSuite exited with non-zero exit code: " +
                    "${process.exitValue()} - ${process.errorStream}")
        }
    }
}

class EvoSuiteRuntimeException(message: String? = null) : RuntimeException(message)
