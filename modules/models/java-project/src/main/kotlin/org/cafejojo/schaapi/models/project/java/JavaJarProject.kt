package org.cafejojo.schaapi.models.project.java

import java.io.File

/**
 * A Java project contained in a JAR file.
 */
class JavaJarProject(override val classDir: File) : JavaProject {
    override val dependencyDir: File = classDir
    override var classes: List<File> = listOf()
    override var dependencies: List<File> = listOf()
    override var classpath: String = classDir.absolutePath
    override val projectDir: File = classDir

    override lateinit var classNames: List<String>
}
