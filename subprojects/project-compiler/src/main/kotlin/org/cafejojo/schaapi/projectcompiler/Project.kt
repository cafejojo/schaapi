package org.cafejojo.schaapi.projectcompiler

import java.io.File


/**
 * A Maven project.
 */
data class Project(val projectDir: File) {
    /**
     * The Maven configuration file.
     */
    val pomFile = File(projectDir, "pom.xml")
    /**
     * The directory containing the project's compiled class files.
     */
    val classDir = File(projectDir, "target/classes")
    /**
     * The directory containing the project's dependencies as JARs.
     */
    val dependencyDir = File(projectDir, "target/dependency")
    /**
     * The project's compiled class files.
     */
    val classes: List<File>
        get() = classDir.walk().filter { it.isFile && it.extension == "class" }.toList()
    /**
     * The project's dependencies as JARs.
     */
    val dependencies: List<File>
        get() = dependencyDir.listFiles().orEmpty().toList()
    /**
     * The classpath needed to load the complete project.
     */
    val classpath: String
        get() {
            if (dependencies.isEmpty()) {
                return classDir.absolutePath
            }

            return classDir.absolutePath + File.pathSeparator +
                dependencies.joinToString(File.pathSeparator) { it.absolutePath }
        }

    init {
        if (!projectDir.isDirectory) {
            throw IllegalArgumentException("Given project directory does not exist")
        }
        if (!pomFile.isFile) {
            throw IllegalArgumentException("Given project directory is not a Maven project")
        }

        classDir.mkdirs()
        dependencyDir.mkdirs()
    }

    /**
     * Returns true if the given class is part of this project.
     *
     * @param className the name of a class, including the package
     */
    fun containsClass(className: String): Boolean =
        classes
            .map { it.relativeTo(classDir) }
            .map { it.toString() }
            .map { it.dropLast(".class".length) }
            .map { it.replace(File.separatorChar, '.') }
            .contains(className)
}
