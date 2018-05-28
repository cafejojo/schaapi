package org.cafejojo.schaapi.pipeline.miner.github

import org.cafejojo.schaapi.models.Project
import org.zeroturnaround.zip.ZipException
import org.zeroturnaround.zip.ZipUtil
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.streams.toList

/**
 * Downloads the zip files of the given GitHub repositories and returns a list of Java projects.
 *
 * @property projectNames the names of all repositories to be downloaded
 * @property outputDirectory the directory to store all the project directories
 * @property projectPacker packer which determines what type of [Project] to wrap the project directory in
 */
@Suppress("PrintStackTrace") // TODO use a logger
class GithubProjectDownloader(
    private val projectNames: Collection<String>,
    private val outputDirectory: File,
    private val projectPacker: (File) -> Project
) {
    /**
     * Starts downloading repositories.
     *
     * A project is not returned if any of the following occurred:
     * * A connection could not be established or the zip file could simply not be downloaded
     * * No zip file could be created to store the downloaded file
     * * The downloaded zip could not be extracted
     * * No file could be created to store the extracted project in
     *
     * @return list of [Project]s which reference the downloaded projects from GitHub
     */
    fun download(): List<Project> =
        projectNames
            .parallelStream()
            .map { downloadAndSaveProject(it) }
            .toList()
            .filterNotNull()

    private fun downloadAndSaveProject(projectName: String): Project? {
        val connection = getConnection(projectName) ?: return null

        try {
            val zipFile = saveToFile(connection.inputStream, projectName) ?: return null
            val unzippedFile = unzip(zipFile) ?: return null

            return if (unzippedFile.exists()) projectPacker(unzippedFile) else null
        } finally {
            connection.disconnect()
        }
    }

    internal fun saveToFile(input: InputStream, projectName: String): File? {
        val alphaNumericRegex = Regex("[^A-Za-z0-9]")
        val outputFile = File(
            outputDirectory,
            "${alphaNumericRegex.replace(projectName, "")}${projectNames.indexOf(projectName)}-project.zip"
        )

        try {
            if (outputFile.exists()) outputFile.delete()
            if (outputFile.createNewFile()) input.copyTo(FileOutputStream(outputFile))
        } catch (e: IOException) {
            e.printStackTrace() // TODO use logger
            return null
        }

        return outputFile
    }

    internal fun unzip(zipFile: File): File? {
        val output = File(zipFile.parent, zipFile.nameWithoutExtension)
        if (output.exists()) output.deleteRecursively()

        try {
            ZipUtil.unpack(zipFile, output)
        } catch (e: IOException) {
            e.printStackTrace() // TODO use logger
            return null
        } catch (e: ZipException) {
            e.printStackTrace() // TODO use logger
            return null
        } finally {
            if (zipFile.exists()) zipFile.delete()
        }

        return output
    }

    internal fun getConnection(projectName: String): HttpURLConnection? {
        val url = getURl(projectName)

        try {
            val connection = url.openConnection() as? HttpURLConnection ?: return null
            return connection.apply { requestMethod = "GET" }
        } catch (e: IOException) {
            e.printStackTrace() // TODO use logger
            return null
        }
    }

    internal fun getURl(projectName: String) = URL("https://github.com/$projectName/archive/master.zip")
}
