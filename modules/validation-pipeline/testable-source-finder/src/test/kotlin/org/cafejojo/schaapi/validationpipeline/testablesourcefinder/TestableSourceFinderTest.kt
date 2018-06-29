package org.cafejojo.schaapi.validationpipeline.testablesourcefinder

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.it
import java.io.File

object TestableSourceFinderTest : Spek({
    it("can retrieve the source code of a pattern found in a test class") {
        val testFile = File(TestableSourceFinderTest::class.java.getResource("/Patterns_ESTest.java").file)
        val sourceFile = File(TestableSourceFinderTest::class.java.getResource("/Patterns.class").file)

        val source = SingleTestPerFileTestableSourceFinder().find(testFile, sourceFile)

        assertThat(source?.replace("\r\n", "\n")).isEqualTo("""
            // Variables with automatically generated values:
            int var0;

            // Pattern:
            {
                Calculator var1 = new Calculator();
                int var2 = var1.sum(var0, 5);
                return var1.sum(var2, 2);
            }
        """.trimIndent())
    }
})
