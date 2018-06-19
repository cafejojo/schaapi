package org.cafejojo.schaapi.models.project

import java.io.File

/**
 * A Java project contained in a JAR file.
 */
@Suppress("LateinitUsage") // Refer to PR #23
class JavaJarProject(private val jar: File) : JavaProject {
    override val classDir: File
        get() = jar
    override val dependencyDir: File = classDir
    override var classes: Set<File> = setOf()
    override var dependencies: Set<File> = setOf()
    override var classpath: String = classDir.absolutePath
    override val projectDir: File = classDir

    override lateinit var classNames: Set<String>

    init {
        require(jar.isFile) { "Given project JAR is not a file." }
        require(jar.canRead()) { "Cannot read project JAR." }
    }
}
