package org.cafejojo.schaapi.testgenerator

import soot.SootClass
import soot.jimple.JasminClass
import soot.util.JasminOutputStream
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.nio.file.Paths

/**
 * Writes a [SootClass] to a JVM bytecode class file.
 *
 * @property targetDirectory the path to the base directory in which to place the class file structure
 */
class SootClassWriter(private val targetDirectory: String) {
    /**
     * Writes the given [sootClass] to a class file.
     *
     * Creates the necessary package structure (based on the fully qualified name of the class) on the file system.
     * Creates all non-existing directories on the generated path.
     *
     * @param sootClass the class to write to a file
     */
    fun writeFile(sootClass: SootClass) {
        val outputFile = Paths.get(targetDirectory, generateClassFilePath(sootClass.name)).toFile()
        outputFile.parentFile.mkdirs()
        val outputStream = JasminOutputStream(FileOutputStream(outputFile))
        val outputWriter = PrintWriter(OutputStreamWriter(outputStream))

        JasminClass(sootClass).print(outputWriter)

        outputWriter.flush()
        outputStream.close()
    }

    private fun generateClassFilePath(fullyQualifiedName: String) = "${fullyQualifiedName.replace(".", "/")}.class"
}
