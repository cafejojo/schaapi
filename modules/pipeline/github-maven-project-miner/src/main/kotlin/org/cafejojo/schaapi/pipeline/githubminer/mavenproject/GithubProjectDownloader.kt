package org.cafejojo.schaapi.pipeline.githubminer.mavenproject

import org.cafejojo.schaapi.models.Project
import org.zeroturnaround.zip.ZipUtil
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Downloads the zip files of the give github repositories and returns a list of java projects.
 *
 * @property repositoryNames the names of all repositories to be downloaded
 * @property projectPacker packer which determines what type of [Project] to wrap the project directory in
 */
class GithubProjectDownloader(private val repositoryNames: Collection<String>,
                              private val projectPacker: (projectDirectory: File) -> Project) {
    /**
     * Start downloading repositories.
     *
     * @return list of [Project]s which reference the downloaded projects from github
     */
    fun download(): List<Project> {
        val projects = mutableListOf<Project>()

        repositoryNames.forEachIndexed { index, projectName ->
            try {
                val input = getInputStream(projectName)

                val resourceFolder = javaClass.getResource("")
                val outputFile = File("$resourceFolder$index-project.zip")
                val outputStream = FileOutputStream(outputFile)

                input.copyTo(outputStream)

                val unzippedFileUrl = unzip(outputFile)
                projects.add(projectPacker(unzippedFileUrl))
            } catch (e: FileNotFoundException) {
                // TODO use logger
                e.printStackTrace()
            }
        }

        return projects
    }

    private fun getInputStream(projectName: String): InputStream {
        val url = URL("https://github.com/$projectName/archive/master.zip")
        val connection = url.openConnection()

        if (connection is HttpURLConnection) connection.requestMethod = "GET"

        return connection.inputStream
    }

    private fun unzip(zipDirectory: File): File {
        val output = File(zipDirectory.nameWithoutExtension)
        ZipUtil.explode(zipDirectory)

        return output
    }
}
