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
import java.nio.file.Files
import kotlin.streams.toList

/**
 * Downloads the zip files of the given GitHub repositories and returns searchContent list of Java projects.
 *
 * @property projectNames the names of all repositories to be downloaded
 * @property outputDirectory the directory to store all the project directories
 * @property projectPacker packer which determines what type of [Project] to wrap the project directory in
 */
internal class GitHubProjectDownloader<P : Project>(
    private val projectNames: Collection<String>,
    private val outputDirectory: File,
    private val projectPacker: (File) -> P
) {
    private companion object : KLogging()

    private fun File.moveUpOneLevel() = Files.move(this.toPath(), File(this.parentFile.parentFile, this.name).toPath())

    /**
     * Extracts the directory denoting the master branch in a GitHub project by moving the up one level.
     */
    private fun File.extractMasterFile() {
        require(this.listFiles().size == 1) {
            "GitHub project must contain exactly one file, but contained ${this.listFiles().size} files."
        }

        val masterFile = this.listFiles().first()
        require(masterFile.isDirectory) { "GitHub Project must contain a directory for master." }

        if (masterFile.listFiles().isEmpty()) logger.warn { "${masterFile.absolutePath} did not contain any files." }
        masterFile.listFiles().forEach { it.moveUpOneLevel() }

        masterFile.delete()
    }

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
            .parallelStream()
            .map { downloadAndSaveProject(it) }
            .toList()
            .filterNotNull()

    private fun downloadAndSaveProject(projectName: String): P? = getInputStream(projectName)?.use { inputStream ->
        val githubProjectZip = saveZipToFile(inputStream, projectName) ?: return null
        val gitHubProject = unzip(githubProjectZip) ?: return null

        try {
            gitHubProject.extractMasterFile()
            projectPacker(gitHubProject).also { logger.debug { "Created project of $gitHubProject." } }
        } catch (e: IllegalArgumentException) {
            logger.warn("Unable to pack $gitHubProject into project.", e)
            gitHubProject.deleteRecursively()
            null
        }
    }

    internal fun saveZipToFile(input: InputStream, projectName: String): File? {
        val alphaNumericRegex = Regex("[^A-Za-z0-9]")
        val zipFile = File(
            outputDirectory,
            "${alphaNumericRegex.replace(projectName, "")}${projectNames.indexOf(projectName)}.zip"
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
            ZipUtil.unpack(projectZipFile, githubProject)
            logger.debug { "Successfully unzipped file ${projectZipFile.absolutePath}." }
        } catch (e: IOException) {
            logger.warn("Could not unzip ${projectZipFile.absolutePath}.", e)
            return null
        } catch (e: ZipException) {
            logger.warn("Could not unzip ${projectZipFile.absolutePath}.", e)
            return null
        } finally {
            projectZipFile.delete()
        }

        return if (githubProject.exists()) githubProject else null
    }

    internal fun getInputStream(projectName: String): InputStream? {
        val url = getURl(projectName)

        try {
            val connection = url.openConnection() as? HttpURLConnection ?: return null
            logger.debug { "Established a connection with '$url'." }

            connection.apply { requestMethod = "GET" }
            return connection.inputStream
        } catch (e: IOException) {
            logger.warn("Could not connect to project $projectName", e)
            return null
        }
    }

    internal fun getURl(projectName: String) = URL("https://github.com/$projectName/archive/master.zip")
}
