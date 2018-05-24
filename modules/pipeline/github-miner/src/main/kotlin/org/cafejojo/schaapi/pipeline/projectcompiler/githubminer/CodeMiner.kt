package org.cafejojo.schaapi.pipeline.projectcompiler.githubminer

import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon
import okhttp3.Credentials
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.cafejojo.schaapi.models.Project
import org.cafejojo.schaapi.pipeline.CodeMiner
import java.io.File

/**
 * Mine Github using the search API.
 *
 * Credentials must be provided to enable code searching. A code search is done using the group id id artifact id and
 * version of the desired library. String matching is done to find projects which contain a pom file which likely
 * contain a dependency on the desired library. No guarantees however are given.
 *
 * @property username username of github user
 * @property password password of github user
 * @property projectPacker packer which determines what type of [Project] to wrap the project directory in
 */
class CodeMiner(private val username: String, private val password: String,
                private val projectPacker: (projectDirectory: File) -> Project) : CodeMiner {
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

        val response = OkHttpClient().newCall(request).execute()
        val projectNames = projectNames(response.body()?.string() ?: "")

        return GithubDownloader(projectNames).download(projectPacker)
    }

    internal fun projectNames(jsonBody: String): Set<String> {
        val items = Klaxon().parse<CodeSearchResponse>(jsonBody)?.items ?: return emptySet()
        return items.map { it.repository.fullName }.toSet()
    }

    private fun buildCodeSearchQueryText(groupId: String, artifactId: String, version: String): String =
        "$groupId+$artifactId+$version+in:file+filename:$filename+extension:$extension"
}

private data class CodeSearchResponse(val items: List<FileResponse>)
private data class FileResponse(val repository: RepositoryResponse)
private data class RepositoryResponse(@Json(name = "full_name") val fullName: String)
