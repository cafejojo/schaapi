package org.cafejojo.schaapi.pipeline.miner.github

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.spy
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.zeroturnaround.zip.ZipUtil
import java.io.File
import java.io.FileInputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.Files

class GithubProjectDownloaderTest : Spek({
    var output = Files.createTempDirectory("project-downloader").toFile()

    // Create zip file with given dir name (+ zip extension) in output with searchContent single text file with given
    // file content
    fun addZipFile(dirName: String, fileContent: String, customOutput: File = output): File {
        // Directory which represents what should be zipped
        val tempDir = File(customOutput, dirName)
        val tempFile = File(tempDir, "temp.txt")

        tempDir.mkdirs()
        tempFile.createNewFile()
        tempFile.writeText(fileContent)

        // File which represents the zip file
        val zipFile = File(customOutput, "$dirName.zip")

        zipFile.createNewFile()
        ZipUtil.pack(tempDir, zipFile)
        tempDir.deleteRecursively()

        assertThat(customOutput.listFiles()).contains(zipFile)
        assertThat(customOutput.listFiles()).doesNotContain(tempDir)

        return zipFile
    }

    beforeEachTest { output = Files.createTempDirectory("project-downloader").toFile() }
    afterEachTest { output.deleteRecursively() }

    describe("When saving searchContent stream to file") {
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

        it("should remove illegal characters from the repo name") {
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

        it("should overwrite directories which exist") {
            val repoName = "testRepo"
            val repoNames = listOf(repoName)
            val zipStreamContent = "testZip"
            val repoZipStream = zipStreamContent.byteInputStream()
            File(output, "testRepo0-project.zip").createNewFile()

            assertThat(output.listFiles().size).isEqualTo(1)

            GithubProjectDownloader(repoNames, output, ::testProjectPacker)
                .saveToFile(repoZipStream, repoName)

            assertThat(output.listFiles().size).isEqualTo(1)
        }
    }

    describe("when unzipping searchContent file") {
        it("should create searchContent new directory and remove the old") {
            val zipFile = addZipFile("testZipDirectory", "")
            val unzippedFile = GithubProjectDownloader(emptyList(), output, ::testProjectPacker)
                .unzip(zipFile)

            assertThat(output.listFiles()).doesNotContain(zipFile)
            assertThat(output.listFiles()).contains(unzippedFile)
        }

        it("should create searchContent new directory which has searchContent file with the expected content") {
            val zipFile = addZipFile("testZipDirectory", "test text")
            val unzippedFile = GithubProjectDownloader(emptyList(), output, ::testProjectPacker)
                .unzip(zipFile)

            assertThat(unzippedFile?.listFiles()?.first()?.readText()).isEqualTo("test text")
        }

        it("should delete the zip file after extraction") {
            val zipFile = addZipFile("testZipDirectory", "test text")

            assertThat(output.listFiles()).containsExactly(zipFile)

            val unzippedFile = GithubProjectDownloader(
                emptyList(),
                output,
                ::testProjectPacker
            ).unzip(zipFile)

            assertThat(output.listFiles()).containsExactly(unzippedFile)
        }

        it("should overwrite searchContent directory which already exists") {
            val zipFile = addZipFile("testZipDirectory", "test text")
            File(output, "testZipDirectory").createNewFile()

            assertThat(output.listFiles().size).isEqualTo(2)

            GithubProjectDownloader(emptyList(), output, ::testProjectPacker).unzip(zipFile)

            assertThat(output.listFiles().size).isEqualTo(1)
        }

        it("should return null if the zip file does not exist") {
            val invisibleFile = File(output, "invisibleFile")

            assertThat(GithubProjectDownloader(emptyList(), output, ::testProjectPacker).unzip(invisibleFile)).isNull()
            assertThat(output.listFiles()).isEmpty()
        }

        it("should return null if the zip file is not searchContent zip file") {
            val notAZipFile = File(output, "notAZipFile")
            notAZipFile.createNewFile()

            assertThat(GithubProjectDownloader(emptyList(), output, ::testProjectPacker).unzip(notAZipFile)).isNull()
            assertThat(output.listFiles()).isEmpty()
        }
    }

    describe("when downloading searchContent project") {
        it("should create searchContent connection request with the correct url") {
            val connection = GithubProjectDownloader(emptyList(), output, ::testProjectPacker)
                .getConnection("cafejojo/schaapi")

            assertThat(connection?.url).isEqualTo(URL("https://github.com/cafejojo/schaapi/archive/master.zip"))
        }

        it("should create searchContent connection request with searchContent get request method") {
            val connection = GithubProjectDownloader(emptyList(), output, ::testProjectPacker)
                .getConnection("cafejojo/schaapi")

            assertThat(connection?.requestMethod).isEqualTo("GET")
        }

        it("should save the unzipped file") {
            val zipFile = addZipFile("testProject", "content", Files.createTempDirectory("project-downloader").toFile())

            val downloader = spy(GithubProjectDownloader(listOf("testProject"), output, ::testProjectPacker))
            val mockHttpURLConnection = mock<HttpURLConnection> {
                on(it.inputStream) doReturn FileInputStream(zipFile)
            }
            val mockURL = mock<URL> { on(it.openConnection()) doReturn mockHttpURLConnection }

            doReturn(mockURL).`when`(downloader).getURl("testProject")

            assertThat(output.listFiles()).isEmpty()

            downloader.download()

            assertThat(output.listFiles()).isNotEmpty()
            assertThat(output.listFiles().first().listFiles().first().readText()).isEqualTo("content")
        }
    }
})