package org.cafejojo.schaapi

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.it
import java.io.File
import java.nio.file.Files

internal class SchaapiTest : Spek({
    lateinit var target: File

    beforeEachTest {
        target = Files.createTempDirectory("schaapi-smoke").toFile()
    }

    afterEachTest {
//        target.deleteRecursively()
    }

    it("just works") {
        main(arrayOf(
            "-o", target.absolutePath,
            "-l", getResourcePath("/library/"),
            "-u", getResourcePath("/user/a/"),
            "--pattern_detector_minimum_count", "1",
            "--test_generator_timeout", "3"
        ))

        assertThat(target.resolve("patterns/RegressionTest.class")).isFile()
        assertThat(target.resolve("tests/evosuite-tests/RegressionTest_ESTest.java")).isFile()
    }
})

private fun getResourcePath(path: String) =
    SchaapiTest::class.java.getResource(path).path
