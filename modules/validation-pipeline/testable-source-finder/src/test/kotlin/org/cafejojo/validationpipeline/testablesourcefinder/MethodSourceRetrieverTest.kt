package org.cafejojo.validationpipeline.testablesourcefinder

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.it
import java.io.File

object MethodSourceRetrieverTest : Spek({
    it("can get the source of a method") {
        val sourceFile = File(MethodSourceRetrieverTest::class.java.getResource("/DecompiledPatterns.java").file)

        val methodSource = MethodSourceRetriever(sourceFile).getSourceOf("pattern0")

        assertThat(methodSource).isEqualTo("""
            {
                Calculator var1 = new Calculator();
                int var2 = var1.sum(var0, 5);
                return var1.sum(var2, 2);
            }
        """.trimIndent())
    }

    it("does something when the method does not exists") {}
})
