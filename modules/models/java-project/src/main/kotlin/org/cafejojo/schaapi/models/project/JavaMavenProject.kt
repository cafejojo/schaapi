package org.cafejojo.schaapi.models.project

import java.io.File

/**
 * A Java project using Maven.
 */
@Suppress("LateinitUsage") // Refer to PR #23
class JavaMavenProject(
    override val projectDir: File,
    override val mavenDir: File = DEFAULT_MAVEN_HOME
) : JavaProject, MavenProject {
    companion object {
        val DEFAULT_MAVEN_HOME = File(System.getProperty("user.home") + "/.schaapi/maven")
    }

    override val pomFile = File(projectDir, "pom.xml")
    override val classDir = File(projectDir, "target/classes")
    override val dependencyDir = File(projectDir, "target/dependency")

    override lateinit var classes: Set<File>
    override lateinit var classNames: Set<String>
    override lateinit var dependencies: Set<File>
    override lateinit var classpath: String

    init {
        require(projectDir.isDirectory) {
            "Given project directory is not a directory: '$projectDir' is not a directory."
        }
        require(projectDir.canRead()) { "Cannot read project directory: '$projectDir'." }
        require(pomFile.isFile) { "Given project directory is not a Maven project, '$pomFile' does not exist." }
        require(pomFile.canRead()) { "Cannot read POM file: '$pomFile'." }

        classDir.mkdirs()
        dependencyDir.mkdirs()
    }
}
