package org.cafejojo.schaapi.miningpipeline.miner.github

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
import java.util.stream.Stream

internal object GitHubProjectDownloaderTest : Spek({
    var output = Files.createTempDirectory("project-downloader").toFile()

    /**
     * Creates a ZIP file with the given directory name (+ZIP extension) in [customOutput] containing a single text file
     * with [fileContents] as its contents.
     *
     * @param zipName the name of the directory that should be zipped
     * @param fileContents the contents to add to the only file in the ZIP
     * @param customOutput the directory to store the created ZIP in
     */
    fun addZipFile(zipName: String, fileContents: String, customOutput: File = output): File {
        // Directory which represents what should be zipped
        val tempDir = File(customOutput, zipName)
        val tempMasterDir = File(tempDir, "master").apply { mkdirs() }

        // Temporary file
        File(tempMasterDir, "temp.txt").apply {
            createNewFile()
            writeText(fileContents)
        }

        // File which represents the zip file
        val zipFile = File(customOutput, "$zipName.zip").apply {
            this.createNewFile()
            ZipUtil.pack(tempDir, this)
        }
        tempDir.deleteRecursively()

        check(zipFile in customOutput.listFiles())
        check(tempDir !in customOutput.listFiles())

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

            GitHubProjectDownloader(repoNames.stream(), output, ::testProjectPacker)
                .saveZipToFile(repoZipStream, repoName)

            assertThat(File(output, "$repoName.zip")).exists()
            assertThat(File(output, "$repoName.zip").readText()).isEqualTo(zipStreamContent)
        }

        it("should remove illegal characters from the repo name") {
            val repoName = ",./<>?;':\"a[]{}-=_+!@b#$%^&*()"
            val repoNames = listOf(repoName)
            val zipStreamContent = "testZip"
            val repoZipStream = zipStreamContent.byteInputStream()

            GitHubProjectDownloader(repoNames.stream(), output, ::testProjectPacker)
                .saveZipToFile(repoZipStream, repoName)

            assertThat(File(output, "ab.zip")).exists()
        }

        it("should be able to save multiple projects") {
            val repoName1 = "testRepo1"
            val repoName2 = "testRepo2"
            val zip1StreamContent = "testZip1"
            val zip2StreamContent = "testZip2"
            val repo1ZipStream = zip1StreamContent.byteInputStream()
            val repo2ZipStream = zip2StreamContent.byteInputStream()

            val repoNames = listOf(repoName1, repoName2)
            GitHubProjectDownloader(repoNames.stream(), output, ::testProjectPacker)
                .apply { saveZipToFile(repo1ZipStream, repoName1) }
                .apply { saveZipToFile(repo2ZipStream, repoName2) }

            assertThat(File(output, "$repoName1.zip")).exists()
            assertThat(File(output, "$repoName2.zip")).exists()

            assertThat(File(output, "$repoName1.zip").readText()).isEqualTo(zip1StreamContent)
            assertThat(File(output, "$repoName2.zip").readText()).isEqualTo(zip2StreamContent)
        }

        it("should overwrite directories which exist") {
            val repoName = "testRepo"
            val repoNames = listOf(repoName)
            val zipStreamContent = "testZip"
            val repoZipStream = zipStreamContent.byteInputStream()
            File(output, "testRepo.zip").createNewFile()

            assertThat(output.listFiles().size).isEqualTo(1)

            GitHubProjectDownloader(repoNames.stream(), output, ::testProjectPacker)
                .saveZipToFile(repoZipStream, repoName)

            assertThat(output.listFiles().size).isEqualTo(1)
        }
    }

    describe("when unzipping searchContent file") {
        it("should create searchContent new directory and remove the old") {
            val zipFile = addZipFile("testZipDirectory", "")
            val unzippedFile = GitHubProjectDownloader(Stream.of(), output, ::testProjectPacker).unzip(zipFile)

            assertThat(output.listFiles()).doesNotContain(zipFile)
            assertThat(output.listFiles()).contains(unzippedFile)
        }

        it("should create searchContent new directory which has searchContent file with the expected content") {
            val zipFile = addZipFile("testZipDirectory", "test text")
            val unzippedFile = GitHubProjectDownloader(Stream.of(), output, ::testProjectPacker).unzip(zipFile)

            assertThat(unzippedFile?.listFiles()?.first()?.readText()).isEqualTo("test text")
        }

        it("should delete the zip file after extraction") {
            val zipFile = addZipFile("testZipDirectory", "test text")

            assertThat(output.listFiles()).containsExactly(zipFile)

            val unzippedFile = GitHubProjectDownloader(Stream.of(), output, ::testProjectPacker).unzip(zipFile)

            assertThat(output.listFiles()).containsExactly(unzippedFile)
        }

        it("should overwrite searchContent directory which already exists") {
            val zipFile = addZipFile("testZipDirectory", "test text")
            File(output, "testZipDirectory").createNewFile()

            assertThat(output.listFiles().size).isEqualTo(2)

            GitHubProjectDownloader(Stream.of(), output, ::testProjectPacker).unzip(zipFile)

            assertThat(output.listFiles().size).isEqualTo(1)
        }

        it("should return null if the zip file does not exist") {
            val invisibleFile = File(output, "invisibleFile")

            assertThat(GitHubProjectDownloader(Stream.of(), output, ::testProjectPacker).unzip(invisibleFile)).isNull()
            assertThat(output.listFiles()).isEmpty()
        }

        it("should return null if the zip file is not searchContent zip file") {
            val notAZipFile = File(output, "notAZipFile")
            notAZipFile.createNewFile()

            assertThat(GitHubProjectDownloader(Stream.of(), output, ::testProjectPacker).unzip(notAZipFile)).isNull()
            assertThat(output.listFiles()).isEmpty()
        }
    }

    describe("when downloading searchContent project") {
        it("should save the unzipped file and delete the old zip file") {
            val zipFile = addZipFile("testProject", "content", Files.createTempDirectory("project-downloader").toFile())

            val downloader = spy(GitHubProjectDownloader(Stream.of("testProject"), output, ::testProjectPacker))
            val mockHttpURLConnection = mock<HttpURLConnection> {
                on(it.inputStream) doReturn FileInputStream(zipFile)
            }
            val mockURL = mock<URL> { on(it.openConnection()) doReturn mockHttpURLConnection }

            doReturn(mockURL).`when`(downloader).getUrl("testProject")

            assertThat(output.listFiles()).isEmpty()

            downloader.download()

            assertThat(output.listFiles()).hasSize(1)
            assertThat(output.listFiles().first().listFiles().first().readText()).isEqualTo("content")
        }
    }
})
