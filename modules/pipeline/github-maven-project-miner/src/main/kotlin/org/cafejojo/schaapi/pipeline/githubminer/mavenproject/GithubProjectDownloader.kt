package org.cafejojo.schaapi.pipeline.githubminer.mavenproject

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
 * @property repositoryNames the names of all repositories to be downloaded
 * @property projectPacker packer which determines what type of [Project] to wrap the project directory in
 */
@Suppress("PrintStackTrace") // TODO use a logger
class GithubProjectDownloader(private val repositoryNames: Collection<String>,
                              private val projectPacker: (projectDirectory: File) -> Project) {
    /**
     * Start downloading repositories.
     *
     * @return list of [Project]s which reference the downloaded projects from github
     */
    fun download(): List<Project> {
        val projects = mutableListOf<Project>()

        for (repoName in repositoryNames) {
            val input = getInputStream(repoName) ?: continue

            val outputFile = saveToFile(input, repoName)
            val unzippedFile = unzip(outputFile)

            if (unzippedFile.exists()) projects.add(projectPacker(unzippedFile))
        }

        return projects
    }

    internal fun saveToFile(input: InputStream, projectName: String): File {
        val resourceFolder = javaClass.getResource("")

        val regex = Regex("[^A-Za-z0-9 ]")
        val outputFile = File("$resourceFolder${regex.replace(projectName, "")}-project.zip")

        try {
            // TODO log if file not created
            if (outputFile.createNewFile()) input.copyTo(FileOutputStream(outputFile))
        } catch (e: IOException) {
            e.printStackTrace() // TODO use logger
        }

        return outputFile
    }

    private fun getInputStream(projectName: String): InputStream? {
        val url = URL("https://github.com/$projectName/archive/master.zip")

        return try {
            val connection = url.openConnection()
            if (connection is HttpURLConnection) connection.requestMethod = "GET"

            connection.inputStream
        } catch (e: IOException) {
            e.printStackTrace() // TODO use logger
            null
        }
    }

    private fun unzip(zipDirectory: File): File {
        val output = File(zipDirectory.nameWithoutExtension)

        try {
            ZipUtil.explode(zipDirectory)
        } catch (e: IOException) {
            e.printStackTrace() // TODO use logger
        } finally {
            if (zipDirectory.exists()) zipDirectory.deleteRecursively()
        }

        return output
    }
}
