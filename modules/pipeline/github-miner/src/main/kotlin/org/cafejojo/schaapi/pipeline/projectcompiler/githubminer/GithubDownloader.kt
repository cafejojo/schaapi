package org.cafejojo.schaapi.pipeline.projectcompiler.githubminer

import org.cafejojo.schaapi.models.Project
import org.zeroturnaround.zip.ZipUtil
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.InputStream
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
    fun download(projectPacker: (projectDirectory: File) -> Project): List<Project> {
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
