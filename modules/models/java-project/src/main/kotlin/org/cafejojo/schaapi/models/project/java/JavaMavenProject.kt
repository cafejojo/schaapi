package org.cafejojo.schaapi.models.project.java

import org.cafejojo.schaapi.models.project.MavenProject
import java.io.File

/**
 * A Java project using Maven.
 */
@SuppressWarnings("LateinitUsage") // Refer to PR #23
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

    override lateinit var classes: List<File>
    override lateinit var classNames: List<String>
    override lateinit var dependencies: List<File>
    override lateinit var classpath: String

    init {
        require(!projectDir.isDirectory) { "Given project directory does not exist" }
        require(!pomFile.isFile) { "Given project directory is not a Maven project" }

        classDir.mkdirs()
        dependencyDir.mkdirs()
    }
}
