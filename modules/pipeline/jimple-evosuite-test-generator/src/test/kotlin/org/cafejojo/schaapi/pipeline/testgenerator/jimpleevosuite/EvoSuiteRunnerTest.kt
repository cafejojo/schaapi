package org.cafejojo.schaapi.pipeline.testgenerator.jimpleevosuite

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.io.File

internal class EvoSuiteRunnerTest : Spek({
    val classPath = EvoSuiteRunnerTest::class.java.getResource("../../../../../../").toURI().path
    val evoSuiteTestOutput = File("$classPath/evosuite-tests/")

    fun deleteTestOutput() {
        if (evoSuiteTestOutput.exists()) {
            evoSuiteTestOutput.deleteRecursively()
        }
    }

    beforeGroup {
        deleteTestOutput()
    }

    afterEachTest {
        deleteTestOutput()
    }

    describe("execution of the EvoSuite test generator") {
        it("generates tests for a test class") {
            val evoSuiteRunner = EvoSuiteRunner(
                "org.cafejojo.schaapi.test.EvoSuiteTestClass",
                classPath,
                classPath,
                generationTimeoutSeconds = 5
            )

            evoSuiteRunner.run()

            assertThat(File("${evoSuiteTestOutput.path}/org/cafejojo/schaapi/test/EvoSuiteTestClass_ESTest.java"))
                .exists()
        }

        it("throws an exception when the class can't be found on the given class path") {
            val evoSuiteRunner = EvoSuiteRunner(
                "org.cafejojo.schaapi.test.EvoSuiteTestClass",
                ".",
                classPath,
                generationTimeoutSeconds = 5
            )

            assertThatThrownBy {
                evoSuiteRunner.run()
            }.isInstanceOf(EvoSuiteRuntimeException::class.java)
        }

        it("throws an exception when the class doesn't exist") {
            val evoSuiteRunner = EvoSuiteRunner(
                "no.way.this.exists.SampleClass",
                classPath,
                classPath,
                generationTimeoutSeconds = 5
            )

            assertThatThrownBy {
                evoSuiteRunner.run()
            }.isInstanceOf(EvoSuiteRuntimeException::class.java)
        }
    }
})
