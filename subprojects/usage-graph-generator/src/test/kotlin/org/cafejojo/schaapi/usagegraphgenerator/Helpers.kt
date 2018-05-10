package org.cafejojo.schaapi.usagegraphgenerator

import org.cafejojo.schaapi.common.JavaProject
import java.io.File

internal data class TestProject(
    override var classpath: String = "",
    private val fullyQualifiedClassNames: List<String> = emptyList()
) : JavaProject {
    override val classDir: File = File(".")
    override val dependencyDir: File = File(".")
    override var dependencies: List<File> = emptyList()
    override val projectDir: File = File(".")
    override var classes: List<File> = emptyList()

    override fun compile() = throw IllegalStateException("Test class cannot be compiled")

    override fun containsClass(className: String) = fullyQualifiedClassNames.contains(className)
}

internal val libraryClasses = listOf(
    "org.cafejojo.schaapi.usagegraphgenerator.testclasses.library.Object1"
)

internal val libraryProject = TestProject(fullyQualifiedClassNames = libraryClasses)
