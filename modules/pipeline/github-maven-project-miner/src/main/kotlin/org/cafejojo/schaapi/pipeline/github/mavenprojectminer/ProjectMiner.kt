package org.cafejojo.schaapi.pipeline.github.mavenprojectminer

import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon
import okhttp3.Credentials
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.cafejojo.schaapi.models.Project
import org.cafejojo.schaapi.pipeline.ProjectMiner
import java.io.File
import java.io.IOException

/**
 * Mines projects on GitHub using the GitHub REST API v3.
 *
 * Credentials must be provided to enable code searching. A code search is done using the group id, artifact id, and
 * version (number) of the desired library. String matching is done to find projects which contain a pom file which
 * likely contain a dependency on the desired library. No guarantees however are given, as GitHub does not provide
 * information on which projects have a dependency on a given library.
 *
 * @property username username of GitHub user
 * @property password password of GitHub user
 * @property outputDirectory directory to store all the project directories
 * @property projectPacker packer which determines what type of [Project] to wrap the project directory in
 */
@Suppress("PrintStackTrace") // TODO use a logger
class ProjectMiner(
    private val username: String, private val password: String,
    private val outputDirectory: File,
    private val projectPacker: (projectDirectory: File) -> Project
) : ProjectMiner {
    init {
        require(outputDirectory.isDirectory) { "Output directory must be a directory." }
    }

    private val filename = "pom"
    private val extension = "xml"

    override fun mine(groupId: String, artifactId: String, version: String): List<Project> {
        val url = HttpUrl.Builder()
            .apply { scheme("https") }
            .apply { host("api.github.com") }
            .apply { addPathSegment("search") }
            .apply { addPathSegment("code") }
            .apply { addQueryParameter("q", buildCodeSearchQueryText(groupId, artifactId, version)) }
            .build()

        val request = Request.Builder()
            .apply { url(url) }
            .apply { header("Authorization", Credentials.basic(username, password)) }
            .build()

        val responseBody = executeRequest(request)
        val projectNames = getProjectNames(responseBody)

        return GithubProjectDownloader(
            projectNames,
            outputDirectory,
            projectPacker
        ).download()
    }

    internal fun executeRequest(request: Request): String =
        try {
            val response = OkHttpClient().newCall(request).execute()
            response.body()?.string() ?: ""
        } catch (e: IOException) {
            e.printStackTrace() // TODO add logger
            ""
        }

    internal fun getProjectNames(requestBody: String): Set<String> {
        if (requestBody.isEmpty()) return emptySet()

        val items = Klaxon().parse<CodeSearchResponse>(requestBody)?.items ?: return emptySet()
        return items.map { it.repository.fullName }.toSet()
    }

    private fun buildCodeSearchQueryText(groupId: String, artifactId: String, version: String): String =
        "$groupId+$artifactId+$version+in:file+filename:$filename+extension:$extension"
}

private data class CodeSearchResponse(val items: List<FileResponse>)
private data class FileResponse(val repository: RepositoryResponse)
private data class RepositoryResponse(@Json(name = "full_name") val fullName: String)
