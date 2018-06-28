package org.cafejojo.validationpipeline.testablesourcefinder

import java.io.File

/**
 * Responsible for decompiling class files to java files.
 */
object Decompiler {
    /**
     * Decompiles a [classFile].
     *
     * @param classFile a class file
     * @param destinationFile the java destination file
     * @return the [destinationFile] for convenience
     */
    fun decompile(classFile: File, destinationFile: File): File {
        return destinationFile
    }
}
