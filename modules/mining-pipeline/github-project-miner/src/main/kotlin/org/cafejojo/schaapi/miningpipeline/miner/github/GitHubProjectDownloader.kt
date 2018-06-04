package org.cafejojo.schaapi.miningpipeline.miner.github

import mu.KLogging
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
 * Downloads the zip files of the given GitHub repositories and returns searchContent list of Java projects.
 *
 * @property projectNames the names of all repositories to be downloaded
 * @property outProjects the directory to store all the project directories
 * @property projectPacker packer which determines what type of [Project] to wrap the project directory in
 */
internal class GitHubProjectDownloader<P : Project>(
    private val projectNames: Collection<String>,
    private val outProjects: File,
    private val projectPacker: (File) -> P
) {
    private companion object : KLogging()

    private fun File.getMasterFile(): File {
        require(this.listFiles().isNotEmpty()) { "GitHub Project must contain files" }
        return this.listFiles().first()
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
            .also { logger.info { "${it.size} extractions of projects from GitHub were successful." } }

    private fun downloadAndSaveProject(projectName: String): P? {
        val connection = getConnection(projectName) ?: return null

        try {
            val githubProjectZip = saveZipToFile(connection.inputStream, projectName) ?: return null
            val gitHubProject = unzip(githubProjectZip) ?: return null

            return if (gitHubProject.exists()) {
                try {
                    val masterDir = gitHubProject.getMasterFile()
                    projectPacker(masterDir).also { logger.info { "Created project of $masterDir." } }
                } catch (e: IllegalArgumentException) {
                    logger.warn("Unable to pack $gitHubProject in project.", e)
                    gitHubProject.deleteRecursively()
                    null
                }
            } else {
                null
            }
        } finally {
            connection.disconnect()
        }
    }

    internal fun saveZipToFile(input: InputStream, projectName: String): File? {
        val alphaNumericRegex = Regex("[^A-Za-z0-9]")
        val outputFile = File(
            outProjects,
            "${alphaNumericRegex.replace(projectName, "")}${projectNames.indexOf(projectName)}.zip"
        )

        try {
            if (outputFile.exists()) {
                logger.info("Output file ${outputFile.path} already exists and will be deleted.")
                outputFile.delete()
            }

            if (outputFile.createNewFile()) {
                input.copyTo(FileOutputStream(outputFile))
            } else {
                logger.warn("Output file ${outputFile.path} could not be created.")
            }
        } catch (e: IOException) {
            logger.warn("Could not save project to ${outputFile.path}.", e)
            return null
        }

        return outputFile
    }

    internal fun unzip(projectZipFile: File): File? {
        val githubProject = File(projectZipFile.parent, projectZipFile.nameWithoutExtension)
        if (githubProject.exists()) {
            logger.info { "File ${githubProject.path} already exists and will be overwritten." }
            githubProject.deleteRecursively()
        }

        try {
            ZipUtil.unpack(projectZipFile, githubProject)
            logger.info { "Successfully unzipped file ${projectZipFile.name}." }
        } catch (e: IOException) {
            logger.warn("Could not unzip ${projectZipFile.name}.", e)
            return null
        } catch (e: ZipException) {
            logger.warn("Could not unzip ${projectZipFile.name}.", e)
            return null
        } finally {
            if (projectZipFile.exists()) {
                logger.info { "Deleting $projectZipFile." }
                projectZipFile.delete()
            }
        }

        return githubProject
    }

    internal fun getConnection(projectName: String): HttpURLConnection? {
        val url = getURl(projectName)

        try {
            val connection = url.openConnection() as? HttpURLConnection ?: return null
            logger.info { "Established a connection with '$url'." }

            return connection.apply { requestMethod = "GET" }
        } catch (e: IOException) {
            logger.warn("Could not connect to project $projectName.", e)
            return null
        }
    }

    internal fun getURl(projectName: String) = URL("https://github.com/$projectName/archive/master.zip")
}
