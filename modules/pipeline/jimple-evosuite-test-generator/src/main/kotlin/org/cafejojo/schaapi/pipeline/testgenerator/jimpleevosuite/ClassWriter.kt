package org.cafejojo.schaapi.pipeline.testgenerator.jimpleevosuite

import soot.SootClass
import soot.jimple.JasminClass
import soot.util.JasminOutputStream
import java.io.FileOutputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.nio.file.Paths

/**
 * Functionality for serializing a [SootClass] to JVM bytecode.
 */
object ClassWriter {
    /**
     * Writes the given [sootClass] to a class file.
     *
     * Creates the necessary package structure (based on the fully qualified name of the class) on the file system.
     * Creates all non-existing directories on the generated path.
     *
     * @param sootClass the class to write to file
     * @param targetDirectory the path to the base directory in which to place the class file structure
     */
    fun writeToFile(sootClass: SootClass, targetDirectory: String) {
        val outputFile = Paths.get(targetDirectory,
            generateClassFilePath(sootClass.name)).toFile()
        outputFile.parentFile.mkdirs()
        FileOutputStream(outputFile).use {
            writeToOutputStream(sootClass, it)
        }
    }

    /**
     *  Writes the given [sootClass] to the given [outputStream].
     *
     * @param sootClass the class to write to a file
     * @param outputStream the [OutputStream] to write the bytecode to
     */
    fun writeToOutputStream(sootClass: SootClass, outputStream: OutputStream) {
        val jasminOutputStream = JasminOutputStream(outputStream)
        val outputWriter = PrintWriter(OutputStreamWriter(jasminOutputStream))

        JasminClass(sootClass).print(outputWriter)

        outputWriter.flush()
    }

    private fun generateClassFilePath(fullyQualifiedName: String) = "${fullyQualifiedName.replace(".", "/")}.class"
}
