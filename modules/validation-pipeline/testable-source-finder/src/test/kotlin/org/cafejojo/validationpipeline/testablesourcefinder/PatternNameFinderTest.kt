package org.cafejojo.validationpipeline.testablesourcefinder

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.it

object PatternNameFinderTest : Spek({
    it("can find a pattern name within a test") {
        val testSource = """
            public class Patterns_ESTest {

                @Test(timeout = 4000)
                public void test0()  throws Throwable  {
                    int result = Patterns.pattern0(35);

                    assertEquals(42, result);
                }
            }
        """.trimIndent()

        val patternNames = PatternNameFinder(testSource).find()

        assertThat(patternNames).hasSize(1)
        assertThat(patternNames.first()).isEqualTo("pattern0")
    }

    it("can find multiple pattern names within the source code") {
        val testSource = """
            public class Patterns_ESTest {

                @Test(timeout = 4000)
                public void test0()  throws Throwable  {
                    int result = Patterns.pattern0(35);

                    assertEquals(42, result);
                }

                @Test(timeout = 4000)
                public void test0()  throws Throwable  {
                    int result = Patterns.pattern234789(35);

                    assertEquals(42, result);
                }
            }
        """.trimIndent()

        val patternNames = PatternNameFinder(testSource).find()

        assertThat(patternNames).hasSize(2)
        assertThat(patternNames[0]).isEqualTo("pattern0")
        assertThat(patternNames[1]).isEqualTo("pattern234789")
    }

    it("gives an empty list of no patterns can be found") {
        val testSource = """
            public class Patterns_ESTest {

                @Test(timeout = 4000)
                public void test0()  throws Throwable  {
                    int result = Random().nextInt();

                    assertEquals(42, result);
                }
            }
        """.trimIndent()

        val patternNames = PatternNameFinder(testSource).find()

        assertThat(patternNames).isEmpty()
    }
})
