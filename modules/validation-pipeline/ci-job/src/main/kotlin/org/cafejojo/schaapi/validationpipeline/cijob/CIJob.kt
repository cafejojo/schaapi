package org.cafejojo.schaapi.validationpipeline.cijob

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.result.Result
import org.zeroturnaround.zip.ZipUtil
import java.io.File

/**
 * The CI job that performs the execution of all involved steps.
 */
class CIJob(private val identifier: String, private val projectDirectory: File, private val downloadUrl: String) {
    private val zipFile = File(projectDirectory, "builds").let { it.mkdirs(); File(it, "$identifier.zip") }
    private val newProjectFiles = File(projectDirectory, "builds/$identifier").also { it.mkdirs() }

    /**
     * Executes the necessary steps to run tests and report the results.
     */
    fun run() {
        download()
        extractZip()
    }

    private fun download() = Fuel.head(downloadUrl).responseOrThrowException().let { (_, urlResponse, _) ->
        Fuel.download(urlResponse.url.toString())
            .destination { _, _ -> zipFile }
            .responseOrThrowException()
    }

    private fun extractZip() = ZipUtil.unpack(zipFile, newProjectFiles, { it.substringAfter(File.separator) })
}

internal fun Request.responseOrThrowException() = response().also { (_, _, result) ->
    when (result) {
        is Result.Success -> result.get()
        is Result.Failure -> throw result.getException()
    }
}
