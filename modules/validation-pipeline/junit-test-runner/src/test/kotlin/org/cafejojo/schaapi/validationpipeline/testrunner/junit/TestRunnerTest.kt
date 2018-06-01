package org.cafejojo.schaapi.validationpipeline.testrunner.junit

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.io.File
import java.net.URLDecoder

internal object TestRunnerTest : Spek({
    val testPackage = "org.cafejojo.schaapi.validationpipeline.testrunner.junit.test"
    val testDirectory = testPackage.replace(Regex("[.]"), "/")

    fun getResourceAsFile(path: String) =
        File(URLDecoder.decode(TestRunnerTest::class.java.getResource(path).path, "UTF-8"))

    fun getFileFromClasspath(path: String) =
        File(TestRunnerTest::class.java.getResource("../../../../../../").toURI().path, path)

    describe("JUnit test runner") {
        lateinit var runner: TestRunner

        beforeEachTest {
            runner = TestRunner()
        }

        it("throws an exception when the test directory does not exist") {
            assertThatThrownBy { runner.run(File("does_not_exist"), emptyList()) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("Given test directory does not exist.")
        }

        it("throws an exception when the test directory is not a directory") {
            assertThatThrownBy { runner.run(getResourceAsFile("/a.file"), emptyList()) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("Given test directory is not a directory.")
        }

        it("throws an exception if a test file does not exist") {
            assertThatThrownBy {
                runner.run(
                    getResourceAsFile("/directory"),
                    listOf(File("does_not_exist"))
                )
            }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("Not all given test files exist.")
        }

        it("throws an exception when a test file is not actually a file") {
            assertThatThrownBy {
                runner.run(
                    getResourceAsFile("/directory"),
                    listOf(getResourceAsFile("/directory/subdirectory"))
                )
            }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("Not all test files are files.")
        }

        it("throws an exception when a test file is not a class") {
            assertThatThrownBy {
                runner.run(
                    getResourceAsFile("/directory"),
                    listOf(getResourceAsFile("/directory/another.file"))
                )
            }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("Not all test files are classes.")
        }

        it("returns empty test results for an empty list of files") {
            val results = runner.run(getResourceAsFile("/directory"), emptyList())

            assertThat(results.isEmpty).isTrue()
        }

        it("returns empty test results for an empty test class") {
            val results = runner.run(
                getFileFromClasspath(""),
                listOf(getFileFromClasspath("$testDirectory/EmptyTest.class"))
            )

            assertThat(results.totalCount).isZero()
            assertThat(results.subResults).containsOnlyKeys("$testPackage.EmptyTest")

            val subResult = results.subResults["$testPackage.EmptyTest"]
            assertThat(subResult?.totalCount).isEqualTo(1)
            assertThat(subResult?.failureCount).isEqualTo(1)
            assertThat(subResult?.subResults).isEmpty()
        }

        it("passes, ignores, and fails the tests in a given test file") {
            val results = runner.run(
                getFileFromClasspath(""),
                listOf(getFileFromClasspath("$testDirectory/SimpleTest.class"))
            )

            assertThat(results.totalCount).isZero()
            assertThat(results.subResults).containsOnlyKeys("$testPackage.SimpleTest")

            val subResult = results.subResults["$testPackage.SimpleTest"]
            assertThat(subResult?.totalCount).isEqualTo(3)
            assertThat(subResult?.passCount).isEqualTo(2)
            assertThat(subResult?.ignoreCount).isEqualTo(1)
            assertThat(subResult?.failureCount).isEqualTo(1)
            assertThat(subResult?.subResults).isEmpty()
        }

        it("can execute multiple test classes") {
            val results = runner.run(
                getFileFromClasspath(""),
                listOf(
                    getFileFromClasspath("$testDirectory/SimpleTest.class"),
                    getFileFromClasspath("$testDirectory/AnotherSimpleTest.class")
                )
            )

            assertThat(results.totalCount).isZero()
            assertThat(results.subResults).containsOnlyKeys(
                "$testPackage.SimpleTest",
                "$testPackage.AnotherSimpleTest"
            )
        }
    }
})
