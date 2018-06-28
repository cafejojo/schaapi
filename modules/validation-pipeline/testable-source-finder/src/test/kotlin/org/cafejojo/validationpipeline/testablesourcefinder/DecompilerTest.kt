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

    it("can decompile a class file to a java file") {
        val classFile = File(DecompilerTest::class.java.getResource("/Patterns.class").file)
        val destinationFile = File(target, "Patterns.java")

        val decompiled = Decompiler.decompile(classFile, destinationFile)

        val expectedDecompiledFile = File(DecompilerTest::class.java.getResource("/DecompiledPatterns.java").file)
        assertThat(decompiled.readText()).isEqualTo(expectedDecompiledFile.readText())
    }
})
