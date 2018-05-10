package org.cafejojo.schaapi.testgenerator

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintStream

internal class EvoSuiteRunnerTest : Spek({
    val classPath = EvoSuiteRunnerTest::class.java.getResource("../../../../").toURI().path

    describe("execution of the EvoSuite test generator") {
        it("generates tests for a test class") {
            EvoSuiteRunner.run(
                "org.cafejojo.schaapi.test.EvoSuiteTestClass",
                classPath,
                classPath,
                generationTimeout = 5
            )
//            assertThat(File("$classPath/evosuite-tests/org/cafejojo/schaapi/test/EvoSuiteTestClass_ESTest.java")).exists()
        }
    }
})

fun main(args: Array<String>) {
    val classPath = EvoSuiteRunnerTest::class.java.getResource("../../../../").toURI().path

    val process = Runtime.getRuntime().exec("java -cp $classPath org.evosuite.EvoSuite")
    process.waitFor()
    val error = BufferedReader(InputStreamReader(process.errorStream))
    error.lines().forEach {
        System.out.println(it)
    }
    error.close()
    val outputStream = process.outputStream
    val printStream = PrintStream(outputStream)
    printStream.println()
    printStream.flush()
    printStream.close()
//    val evosuite = EvoSuite()
//    evosuite.parseCommandLine(
//        arrayOf(
//            "-class", "org.cafejojo.schaapi.test.EvoSuiteTestClass",
//            "-base_dir", classPath,
//            "-projectCP", classPath,
//            "-Dsearch_budget=5",
//            "-Dstopping_condition=MaxTime"
//        )
//    )
}
