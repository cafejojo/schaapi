package org.cafejojo.schaapi.projectcompiler

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.io.File

internal class ZipExtractorTest : Spek({
    describe("the ZIP extractor") {
        val target = File("./test")

        beforeGroup {
            target.deleteRecursively()
        }

        afterEachTest {
            target.deleteRecursively()
        }

        it("extracts an empty ZIP") {
            val emptyZip = javaClass.getResourceAsStream("/ZipExtractor/empty.zip")
            ZipExtractor(emptyZip).extractTo(target)

            assertThat(target.listFiles()).isEmpty()
        }

        it("extracts a ZIP with only directories") {
            val dirZip = javaClass.getResourceAsStream("/ZipExtractor/dirs.zip")
            ZipExtractor(dirZip).extractTo(target)

            assertThat(target.resolve("dirA")).isDirectory()
            assertThat(target.resolve("dirB")).isDirectory()
            assertThat(target.resolve("dirB/dirD")).isDirectory()
            assertThat(target.resolve("dirC")).isDirectory()
        }

        it("extracts a ZIP with files and directories") {
            val dirZip = javaClass.getResourceAsStream("/ZipExtractor/files.zip")
            ZipExtractor(dirZip).extractTo(target)

            assertThat(target.resolve("dirA")).isDirectory()
            assertThat(target.resolve("dirA/fileA.txt")).isFile()
            assertThat(target.resolve("dirA/fileB.txt")).isFile()
            assertThat(target.resolve("dirB")).isDirectory()
            assertThat(target.resolve("dirC")).isDirectory()
            assertThat(target.resolve("dirC/fileC.txt")).isFile()
            assertThat(target.resolve("fileD.txt")).isFile()
        }
    }
})
