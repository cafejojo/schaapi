package org.cafejojo.schaapi.pipeline.projectcompiler.githubminer

import org.cafejojo.schaapi.models.Project
import org.cafejojo.schaapi.models.project.javamaven.JavaMavenProject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

/**
 * Clones the github repositories and returns a list of java projects.
 *
 * @property repositoryNames the names of all repositories to be downloaded
 */
class GithubDownloader(private val repositoryNames: Collection<String>) {
    /**
     * Start downloading repositories.
     */
    fun download(): List<Project> {
        val projects = mutableListOf<Project>()

        repositoryNames.forEachIndexed { index, projectName ->
            try {
                val input = getInputStream(projectName)

                val resourceFolder = javaClass.getResource("")
                val outputFile = File("$resourceFolder$index-project.zip")
                val outputStream = PrintStream(FileOutputStream(outputFile))

                input.copyTo(outputStream)

                val unzippedFileUrl = unzip(outputFile)
                projects.add(JavaMavenProject(unzippedFileUrl))
            } catch (e: FileNotFoundException) {
                // TODO use logger
                e.printStackTrace()
            }
        }

        return projects
    }

    private fun getInputStream(projectName: String): InputStream {
        val url = URL("https://github.com/$projectName/archive/master.zip")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        return connection.inputStream
    }

    private fun unzip(directory: File): File = File("")
}
