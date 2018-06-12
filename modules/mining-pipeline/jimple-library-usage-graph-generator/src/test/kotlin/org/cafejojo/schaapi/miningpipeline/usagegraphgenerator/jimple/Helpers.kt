package org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple

import org.cafejojo.schaapi.models.project.JavaProject
import java.io.File

internal data class TestProject(
    override var classpath: String = "",
    override var classNames: Set<String> = emptySet()
) : JavaProject {
    override val classDir: File = File(".")
    override val dependencyDir: File = File(".")
    override var dependencies: Set<File> = emptySet()
    override val projectDir: File = File(".")
    override var classes: Set<File> = emptySet()
}

internal val libraryClasses = setOf(
    "org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.testclasses.library.Object1"
)
internal val libraryProject = TestProject(classNames = libraryClasses)
