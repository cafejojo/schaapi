package org.cafejojo.schaapi.pipeline.github.mavenprojectminer

import org.cafejojo.schaapi.models.Project
import org.zeroturnaround.zip.ZipUtil
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Downloads the zip files of the give github repositories and returns a list of java projects.
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
     * Start downloading repositories.
     *
     * @return list of [Project]s which reference the downloaded projects from github
     */
    fun download(): List<Project> {
        val projects = mutableListOf<Project>()

        for (repoName in projectNames) {
            // TODO log if unable to make a connection
            val connection = getConnection(repoName) ?: continue

            val outputFile = saveToFile(connection.inputStream, repoName)
            connection.inputStream.close()

            val unzippedFile = unzip(outputFile)
            if (unzippedFile.exists()) projects.add(projectPacker(unzippedFile))
        }

        return projects
    }

    internal fun saveToFile(input: InputStream, projectName: String): File {
        val alphaNumericRegex = Regex("[^A-Za-z0-9]")
        val outputFile = File(
            outputDirectory,
            "${alphaNumericRegex.replace(projectName, "")}${projectNames.indexOf(projectName)}-project.zip"
        )

        try {
            // TODO log if file not created
            if (outputFile.exists()) outputFile.deleteRecursively()
            if (outputFile.createNewFile()) input.copyTo(FileOutputStream(outputFile))
        } catch (e: IOException) {
            e.printStackTrace() // TODO use logger
        }

        return outputFile
    }

    internal fun unzip(zipFile: File): File {
        val output = File(zipFile.parent, zipFile.nameWithoutExtension)

        try {
            ZipUtil.unpack(zipFile, output)
        } catch (e: IOException) {
            e.printStackTrace() // TODO use logger
        } finally {
            if (zipFile.exists()) zipFile.delete()
        }

        return output
    }

    internal fun getConnection(projectName: String): HttpURLConnection? {
        val url = URL("https://github.com/$projectName/archive/master.zip")

        return try {
            val connection = url.openConnection() as? HttpURLConnection ?: return null

            connection.requestMethod = "GET"
            connection
        } catch (e: IOException) {
            e.printStackTrace() // TODO use logger
            null
        }
    }
}
