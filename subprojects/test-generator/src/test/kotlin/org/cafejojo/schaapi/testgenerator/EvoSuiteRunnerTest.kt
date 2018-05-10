package org.cafejojo.schaapi.testgenerator

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.io.File

internal class EvoSuiteRunnerTest : Spek({
    val classPath = EvoSuiteRunnerTest::class.java.getResource("../../../../").toURI().path
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
            EvoSuiteRunner(
                    "org.cafejojo.schaapi.test.EvoSuiteTestClass",
                    classPath,
                    classPath,
                    generationTimeoutSeconds = 5
            ).run()

            assertThat(File("${evoSuiteTestOutput.path}/org/cafejojo/schaapi/test/EvoSuiteTestClass_ESTest.java"))
                    .exists()
        }
    }
})
