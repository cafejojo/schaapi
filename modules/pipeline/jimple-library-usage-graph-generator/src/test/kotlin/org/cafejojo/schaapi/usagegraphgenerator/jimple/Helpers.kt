package org.cafejojo.schaapi.usagegraphgenerator.jimple

import org.cafejojo.schaapi.project.javamaven.JavaProject
import java.io.File

internal data class TestProject(
    override var classpath: String = "",
    override var classNames: List<String> = emptyList()
) : JavaProject {
    override val classDir: File = File(".")
    override val dependencyDir: File = File(".")
    override var dependencies: List<File> = emptyList()
    override val projectDir: File = File(".")
    override var classes: List<File> = emptyList()
}

internal val libraryClasses = listOf(
    "org.cafejojo.schaapi.usagegraphgenerator.jimple.testclasses.library.Object1"
)

internal val libraryProject = TestProject(classNames = libraryClasses)
