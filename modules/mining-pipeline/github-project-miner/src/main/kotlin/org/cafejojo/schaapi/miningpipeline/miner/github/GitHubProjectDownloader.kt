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
import java.util.concurrent.atomic.AtomicInteger
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
    private val projectPacker: (File) -> P,
    private val maxProjectDownloads: Int
) {
    private val outputProject = outputDirectory.resolve("projects/").apply { mkdirs() }
    private var downloaded: AtomicInteger = AtomicInteger(0)

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
            .parallelStream()
            .map { downloadAndSaveProject(it) }
            .toList()
            .also { logger.info { "Tried to extract ${it.size} projects in total from github." } }
            .filterNotNull()
            .also { logger.info { "Of which ${it.size} extractions were successful" } }

    private fun downloadAndSaveProject(projectName: String): P? {
        if (downloaded.getAndIncrement() >= maxProjectDownloads) return null

        val connection = getConnection(projectName) ?: return null

        try {
            val zipFile = saveToFile(connection.inputStream, projectName) ?: return null
            val unzippedFile = unzip(zipFile) ?: return null

            return if (unzippedFile.exists()) {
                try {
                    projectPacker(unzippedFile.listFiles().first())
                        .also { logger.info { "Created project of $unzippedFile." } }
                } catch (e: RuntimeException) {
                    logger.warn("Unable to pack $unzippedFile in project.", e)
                    unzippedFile.deleteRecursively()
                    null
                }
            } else {
                null
            }
        } finally {
            connection.disconnect()
        }
    }

    internal fun saveToFile(input: InputStream, projectName: String): File? {
        val alphaNumericRegex = Regex("[^A-Za-z0-9]")
        val outputFile = File(
            outputProject,
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

    internal fun unzip(zipFile: File): File? {
        val output = File(zipFile.parent, zipFile.nameWithoutExtension)
        if (output.exists()) {
            logger.info { "File $output existed and will be overwritten by $output." }
            output.deleteRecursively()
        }

        try {
            ZipUtil.unpack(zipFile, output)
            logger.info { "Successfully unzipped file ${zipFile.name}." }
        } catch (e: IOException) {
            logger.warn("Could not unzip ${zipFile.name}.", e)
            return null
        } catch (e: ZipException) {
            logger.warn("Could not unzip ${zipFile.name}.", e)
            return null
        } finally {
            if (zipFile.exists()) {
                logger.info { "Deleting $zipFile." }
                zipFile.delete()
            }
        }

        return output
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
