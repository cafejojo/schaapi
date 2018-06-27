package org.cafejojo.schaapi.validationpipeline.cijob

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.cafejojo.schaapi.validationpipeline.CIJobException
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.it
import java.io.File
import java.nio.file.Files

object CIJobTest : Spek({
    lateinit var storageDirectory: File
    lateinit var projectDirectory: File
    lateinit var testsDirectory: File

    beforeEachTest {
        storageDirectory = Files.createTempDirectory("schaapi-ci").toFile()
        projectDirectory = File(storageDirectory, "projects/cafejojo/dummy-simple-maven-library").also { it.mkdirs() }
        testsDirectory = File(projectDirectory, "tests").also { it.mkdir() }

        File(CIJobTest::class.java.getResource("/projects/cafejojo/dummy-simple-maven-library/tests").file)
            .listFiles()
            .forEach { it.copyTo(File(testsDirectory, it.name), overwrite = true) }
    }

    it("executes a CI job") {
        val commitHash = "ced7a679ba6337977d99effccdb4ac66b3ba34e0"
        val downloadUrl = "https://github.com/cafejojo/dummy-simple-maven-library/archive/$commitHash.zip"

        val ciJob = CIJob(commitHash, projectDirectory, downloadUrl)

        val testResults = ciJob.call()

        assertThat(File(projectDirectory, "builds/$commitHash/README.md")).exists()
        assertThat(testResults.totalCount).isEqualTo(1)
        assertThat(testResults.passCount).isEqualTo(1)
    }

    it("throws an exception when the source code cannot be downloaded") {
        val downloadUrl = "https://non-existing-domain.test/files.zip"

        val ciJob = CIJob("id", projectDirectory, downloadUrl)

        assertThatThrownBy { ciJob.call() }
            .isInstanceOf(CIJobException::class.java)
            .hasMessageContaining("could not be downloaded")
            .hasMessageContaining(downloadUrl)
    }

    it("throws an exception when the source code cannot be extracted") {
        val downloadUrl = "https://httpbin.org/image"

        val ciJob = CIJob("id", projectDirectory, downloadUrl)

        assertThatThrownBy { ciJob.call() }
            .isInstanceOf(CIJobException::class.java)
            .hasMessageContaining("unzipping")
    }

    it("throws an exception when the source code cannot be compiled") {
        val commitHash = "29fecd2f7391023d907957ed42949ca2bf6fedcc" // failing commit
        val downloadUrl = "https://github.com/cafejojo/dummy-simple-maven-library/archive/$commitHash.zip"

        val ciJob = CIJob("id", projectDirectory, downloadUrl)

        assertThatThrownBy { ciJob.call() }
            .isInstanceOf(CIJobException::class.java)
            .hasMessageContaining("library source code")
            .hasMessageContaining("compile")
    }

    it("throws an exception when the test code cannot be compiled") {
        File(CIJobTest::class.java.getResource(
            "/projects/cafejojo/dummy-simple-maven-library/failing-tests/FailingPatterns_ESTest.java"
        ).file).apply {
            copyTo(File(testsDirectory, name), overwrite = true)
        }

        val commitHash = "ced7a679ba6337977d99effccdb4ac66b3ba34e0" // passing commit
        val downloadUrl = "https://github.com/cafejojo/dummy-simple-maven-library/archive/$commitHash.zip"

        val ciJob = CIJob("id", projectDirectory, downloadUrl)

        assertThatThrownBy { ciJob.call() }
            .isInstanceOf(CIJobException::class.java)
            .hasMessageContaining("test compilation")
            .hasMessageContaining("boolean result = Patterns.pattern0(35);")
    }

    it("reports test failures correctly") {
        val commitHash = "78ab65330411737209587d790f94f12e3d9a95d0" // failing commit
        val downloadUrl = "https://github.com/cafejojo/dummy-simple-maven-library/archive/$commitHash.zip"

        val ciJob = CIJob("id", projectDirectory, downloadUrl)

        val testResults = ciJob.call()

        assertThat(testResults.totalCount).isEqualTo(1)
        assertThat(testResults.failureCount).isEqualTo(1)
    }
})
