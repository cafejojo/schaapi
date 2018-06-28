package org.cafejojo.validationpipeline.testablesourcefinder

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.it
import java.io.File
import java.nio.file.Files

object DecompilerTest : Spek({
    lateinit var target: File

    beforeEachTest {
        target = Files.createTempDirectory("schaapi-decompiler").toFile()
    }

    afterEachTest {
        target.deleteRecursively()
    }

    it("can decompile a class file to a Java file") {
        val classFile = File(DecompilerTest::class.java.getResource("/Patterns.class").file)

        val decompiled = Decompiler.decompile(classFile, target)

        val expectedDecompiledFile = File(DecompilerTest::class.java.getResource("/DecompiledPatterns.java").file)
        assertThat(decompiled?.readText()?.replace(" ", ""))
            .isEqualTo(expectedDecompiledFile.readText().replace(" ", ""))
    }

    it("cannot decompile a non class file") {
        val classFile = File(DecompilerTest::class.java.getResource("/non-class-file.txt").file)

        val decompiled = Decompiler.decompile(classFile, target)

        assertThat(decompiled).isNull()
    }
})
