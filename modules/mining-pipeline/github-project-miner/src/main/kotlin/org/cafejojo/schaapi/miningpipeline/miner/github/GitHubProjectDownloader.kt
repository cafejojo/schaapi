package org.cafejojo.schaapi.miningpipeline.miner.github

import mu.KLogging
import org.cafejojo.schaapi.models.Project
import org.zeroturnaround.zip.ZipException
import org.zeroturnaround.zip.ZipUtil
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.stream.Stream
import kotlin.streams.toList

/**
 * Downloads the zip files of the given GitHub repositories and returns searchContent list of Java projects.
 *
 * @property projectNames a stream of the names of all repositories to be downloaded
 * @property outputDirectory the directory to store all the project directories
 * @property projectPacker packer which determines what type of [Project] to wrap the project directory in
 */
internal class GitHubProjectDownloader<P : Project>(
    private val projectNames: Stream<String>,
    private val outputDirectory: File,
    private val projectPacker: (File) -> P
) {
    private companion object : KLogging()

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
    fun download(): List<P> =
        projectNames
            .map { downloadAndSaveProject(it) }
            .toList()
            .filterNotNull()

    private fun downloadAndSaveProject(projectName: String): P? =
        getInputStream(projectName)?.use { inputStream ->
            val gitHubProjectZip = saveZipToFile(inputStream, projectName) ?: return null
            val gitHubProject = unzip(gitHubProjectZip) ?: return null

            return projectPacker(gitHubProject)
        }

    internal fun saveZipToFile(input: InputStream, projectName: String): File? {
        val alphaNumericRegex = Regex("[^A-Za-z0-9]")
        val zipFile = File(
            outputDirectory,
            "${alphaNumericRegex.replace(projectName, "")}.zip"
        )

        try {
            if (zipFile.exists()) {
                logger.debug { "Output file ${zipFile.path} already exists and will be deleted." }
                zipFile.delete()
            }

            if (zipFile.createNewFile()) {
                zipFile.outputStream().use { input.copyTo(it) }
            } else {
                logger.warn { "Output file ${zipFile.path} could not be created." }
            }
        } catch (e: IOException) {
            logger.warn("Could not save project to ${zipFile.path}.", e)
            return null
        }

        return zipFile
    }

    internal fun unzip(projectZipFile: File): File? {
        val githubProject = File(projectZipFile.parent, projectZipFile.nameWithoutExtension)
        if (githubProject.exists()) {
            logger.debug { "File ${githubProject.path} already exists and will be overwritten." }
            githubProject.deleteRecursively()
        }

        try {
            ZipUtil.unpack(projectZipFile, githubProject) { it.drop(it.indexOf('/') + 1).replace(':', '_') }
            logger.debug { "Successfully unzipped file ${projectZipFile.absolutePath}." }
        } catch (e: IOException) {
            logger.warn("Could not unzip ${projectZipFile.absolutePath}.", e)
            githubProject.deleteRecursively()
            return null
        } catch (e: ZipException) {
            logger.warn("Could not unzip ${projectZipFile.absolutePath}.", e)
            githubProject.deleteRecursively()
            return null
        } finally {
            projectZipFile.delete()
        }

        return if (githubProject.exists()) githubProject else null
    }

    private fun getInputStream(projectName: String): InputStream? {
        val url = getUrl(projectName)

        try {
            val connection = url.openConnection() as? HttpURLConnection ?: return null
            logger.debug { "Established a connection with '$url'." }

            return connection.apply { requestMethod = "GET" }.inputStream
        } catch (e: IOException) {
            logger.warn("Could not connect to project $projectName.", e)
            return null
        }
    }

    internal fun getUrl(projectName: String) = URL("https://github.com/$projectName/archive/master.zip")
}
