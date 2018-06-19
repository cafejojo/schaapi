package org.cafejojo.schaapi.validationpipeline.cijob

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.result.Result
import mu.KLogging
import org.cafejojo.schaapi.miningpipeline.projectcompiler.javamaven.MavenInstaller
import org.cafejojo.schaapi.miningpipeline.projectcompiler.javamaven.ProjectCompiler
import org.cafejojo.schaapi.models.project.JavaMavenProject
import org.cafejojo.schaapi.validationpipeline.TestResults
import org.cafejojo.schaapi.validationpipeline.testrunner.junit.TestRunner
import org.zeroturnaround.zip.ZipUtil
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.Callable
import javax.tools.ToolProvider

/**
 * A CI job that performs the execution of all involved steps.
 */
class CIJob(private val identifier: String, private val projectDirectory: File, private val downloadUrl: String) :
    Callable<TestResults> {
    private val zipFile = File(projectDirectory, "builds").let { it.mkdirs(); File(it, "$identifier.zip") }
    private val newProjectFiles = File(projectDirectory, "builds/$identifier").also { it.mkdirs() }
    private val testsDirectory = File(projectDirectory, "tests")

    private companion object : KLogging()

    /**
     * Executes the necessary steps to run tests and report the results.
     */
    override fun call(): TestResults {
        if (!testsDirectory.exists()) throw CIJobException("This project has not been initialized yet. $testsDirectory")

        MavenInstaller().installMaven(JavaMavenProject.DEFAULT_MAVEN_HOME)

        next("Downloading...") { download() }
        next("Extracting zip...") { extractZip() }
        val library = next("Compiling library...") { compileLibrary() }
        val tests = next("Compiling tests...") { compileTests(library) }
        return next("Running tests...") { TestRunner().run(testsDirectory, tests, listOf(library.classDir)) }
    }

    private fun download() = catchExceptions("New source code could not be downloaded from $downloadUrl.") {
        Fuel.head(downloadUrl).responseOrThrowException().let { (_, urlResponse, _) ->
            Fuel.download(urlResponse.url.toString())
                .destination { _, _ -> zipFile }
                .responseOrThrowException()
        }
    }

    private fun extractZip() = catchExceptions("Error occurred while unzipping new source code.") {
        ZipUtil.unpack(zipFile, newProjectFiles, { it.substringAfter("/") })
    }

    private fun compileLibrary() = catchExceptions("New library source code could not be compiled.") {
        ProjectCompiler().compile(JavaMavenProject(newProjectFiles))
    }

    private fun compileTests(library: JavaMavenProject): List<File> {
        val compiler = ToolProvider.getSystemJavaCompiler()
        val errorOutput = ByteArrayOutputStream()

        val classPath = mutableListOf<String>().run {
            addAll(testsDirectory.listFiles().filter { it.extension == "class" }.map { it.parent })
            add(library.classDir.absolutePath)
            add(CIJob::class.java.getResource("/junit-4.12.jar").file)

            joinToString(File.pathSeparator)
        }

        return testsDirectory.listFiles().filter { it.extension == "java" }
            .also { files ->
                if (files.any { compiler.run(null, null, errorOutput, it.absolutePath, "-cp", classPath) != 0 }) {
                    throw CIJobException("User test compilation failed:\n\n${errorOutput.toString("UTF-8")}")
                }
            }
            .map { File(testsDirectory, "${it.nameWithoutExtension}.class") }
    }

    private fun <T> next(logMessage: String, action: () -> T): T {
        logger.info("[build-$projectDirectory-$identifier] $logMessage")

        return action()
    }

    @Suppress("TooGenericExceptionCaught") // That is exactly what we want here :)
    private fun <T> catchExceptions(message: String, action: () -> T): T {
        try {
            return action()
        } catch (exception: Exception) {
            throw CIJobException(message, exception)
        }
    }
}

internal fun Request.responseOrThrowException() = response().also { (_, _, result) ->
    when (result) {
        is Result.Success -> result.get()
        is Result.Failure -> throw result.getException()
    }
}

/**
 * Exception that gets thrown when an error occurs during the execution of a CI job.
 */
class CIJobException(message: String, cause: Throwable? = null) : Exception(message, cause)
