package org.cafejojo.schaapi.pipeline.github.mavenprojectminer

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.zeroturnaround.zip.ZipUtil
import java.io.File
import java.net.URL
import java.nio.file.Files

class GithubProjectDownloaderTest : Spek({
    val output = Files.createTempDirectory("project-downloader").toFile()

    // Create zip file with given dir name (+ zip extension) in output with a single text file with given file content
    fun addZipFile(dirName: String, fileContent: String): File {
        // Directory which represents what should be zipped
        val tempDir = File(output, dirName)
        val tempFile = File(output, "/$dirName/temp.txt")

        tempDir.mkdirs()
        tempFile.createNewFile()
        tempFile.writeText(fileContent)

        // File which represents the zip file
        val zipFile = File(output, "/$dirName.zip")

        zipFile.createNewFile()
        ZipUtil.pack(tempDir, zipFile)
        tempDir.deleteRecursively()

        assertThat(output.listFiles()).contains(zipFile)
        assertThat(output.listFiles()).doesNotContain(tempDir)

        return zipFile
    }

    afterEachTest {
        if (output.exists()) output.deleteRecursively()
        output.mkdir()
    }

    afterGroup { output.deleteRecursively() }

    describe("When saving a stream to file") {
        it("should save the contents of the file to stream") {
            val repoName = "testRepo"
            val repoNames = listOf(repoName)
            val zipStreamContent = "testZip"
            val repoZipStream = zipStreamContent.byteInputStream()

            GithubProjectDownloader(repoNames, output, ::testProjectPacker)
                .saveToFile(repoZipStream, repoName)

            assertThat(File(output, "${repoName}0-project.zip")).exists()
            assertThat(File(output, "${repoName}0-project.zip").readText()).isEqualTo(zipStreamContent)
        }

        it("should removes illegal characters from the repo name") {
            val repoName = ",./<>?;':\"[]{}-=_+!@#$%^&*()"
            val repoNames = listOf(repoName)
            val zipStreamContent = "testZip"
            val repoZipStream = zipStreamContent.byteInputStream()

            GithubProjectDownloader(repoNames, output, ::testProjectPacker)
                .saveToFile(repoZipStream, repoName)

            assertThat(File(output, "0-project.zip")).exists()
        }

        it("should be able to save multiple projects") {
            val repoName1 = "testRepo1"
            val repoName2 = "testRepo2"
            val zip1StreamContent = "testZip1"
            val zip2StreamContent = "testZip2"
            val repo1ZipStream = zip1StreamContent.byteInputStream()
            val repo2ZipStream = zip2StreamContent.byteInputStream()

            val repoNames = listOf(repoName1, repoName2)
            GithubProjectDownloader(repoNames, output, ::testProjectPacker)
                .apply { saveToFile(repo1ZipStream, repoName1) }
                .apply { saveToFile(repo2ZipStream, repoName2) }

            assertThat(File(output, "${repoName1}0-project.zip")).exists()
            assertThat(File(output, "${repoName2}1-project.zip")).exists()

            assertThat(File(output, "${repoName1}0-project.zip").readText()).isEqualTo(zip1StreamContent)
            assertThat(File(output, "${repoName2}1-project.zip").readText()).isEqualTo(zip2StreamContent)
        }
    }

    describe("when unzipping a file") {
        it("should create a new directory and remove the old") {
            val zipFile = addZipFile("testZipDirectory", "")
            val unzippedFile = GithubProjectDownloader(emptyList(), output, ::testProjectPacker)
                .unzip(zipFile)

            assertThat(output.listFiles()).doesNotContain(zipFile)
            assertThat(output.listFiles()).contains(unzippedFile)
        }

        it("should create a new directory which has a file with the expected content") {
            val zipFile = addZipFile("testZipDirectory", "test text")
            val unzippedFile = GithubProjectDownloader(emptyList(), output, ::testProjectPacker)
                .unzip(zipFile)

            assertThat(unzippedFile.listFiles().first().readText()).isEqualTo("test text")
        }
    }

    describe("when downloading a project") {
        it("should create a connection request with the correct url") {
            val connection = GithubProjectDownloader(emptyList(), output, ::testProjectPacker)
                .getConnection("cafejojo/schaapi")

            assertThat(connection?.url).isEqualTo(URL("https://github.com/cafejojo/schaapi/archive/master.zip"))
        }

        it("should create a connection request with a get request method") {
            val connection = GithubProjectDownloader(emptyList(), output, ::testProjectPacker)
                .getConnection("cafejojo/schaapi")

            assertThat(connection?.requestMethod).isEqualTo("GET")
        }
    }
})
