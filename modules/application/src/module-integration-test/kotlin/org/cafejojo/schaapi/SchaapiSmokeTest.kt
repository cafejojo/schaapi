package org.cafejojo.schaapi

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.it
import java.io.File
import java.net.URLDecoder
import java.nio.file.Files

/**
 * Smoke tests for Schaapi.
 */
internal class SchaapiSmokeTest : Spek({
    lateinit var target: File
    lateinit var mavenDir: File

    beforeEachTest {
        target = Files.createTempDirectory("schaapi-smoke").toFile()
        mavenDir = Files.createTempDirectory("schaapi-maven").toFile()
    }

    afterEachTest {
        target.deleteRecursively()
        mavenDir.deleteRecursively()
    }

    it("generates a test class from the patterns in a project using a library") {
        main(arrayOf(
            "directory",
            "-o", target.absolutePath,
            "-l", getResourcePath("/library/"),
            "-u", getResourcePath("/user/"),
            "--maven_dir", mavenDir.absolutePath,
            "--pattern_detector_minimum_count", "1",
            "--test_generator_timeout", "3"
        ))

        assertThat(target.resolve("patterns/org/cafejojo/schaapi/patterns/Patterns_0.class")).isFile()
        assertThat(
            target.resolve("tests/evosuite-tests/org/cafejojo/schaapi/patterns/Patterns_0_ESTest_0.java")
        ).isFile()
    }
})

private fun getResourcePath(path: String) =
    URLDecoder.decode(SchaapiSmokeTest::class.java.getResource(path).path, "UTF-8")
