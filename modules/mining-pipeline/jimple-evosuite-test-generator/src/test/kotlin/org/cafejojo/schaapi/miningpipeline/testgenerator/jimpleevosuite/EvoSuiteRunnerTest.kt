package org.cafejojo.schaapi.miningpipeline.testgenerator.jimpleevosuite

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.io.File

internal object EvoSuiteRunnerTest : Spek({
    val classpath = EvoSuiteRunnerTest::class.java.getResource("../../../../../../").toURI().path
    val evoSuiteTestOutput = File("$classpath/evosuite-tests/")

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
                "org.cafejojo.schaapi.test",
                classpath,
                classpath,
                generationTimeoutSeconds = 5
            )

            evoSuiteRunner.run()

            assertThat(File("${evoSuiteTestOutput.path}/org/cafejojo/schaapi/test/EvoSuiteTestClass_ESTest_0.java"))
                .exists()
        }

        it("throws an exception when no classes exist") {
            val evoSuiteRunner = EvoSuiteRunner(
                "no.way.this.exists",
                classpath,
                classpath,
                generationTimeoutSeconds = 5
            )

            assertThatThrownBy { evoSuiteRunner.run() }.isInstanceOf(EvoSuiteRuntimeException::class.java)
        }
    }
})
