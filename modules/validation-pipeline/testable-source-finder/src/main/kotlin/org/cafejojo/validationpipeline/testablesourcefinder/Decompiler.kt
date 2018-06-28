package org.cafejojo.validationpipeline.testablesourcefinder

import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler
import java.io.File

/**
 * Responsible for decompiling class files to java files.
 */
object Decompiler {
    /**
     * Decompiles a [classFile].
     *
     * @param classFile a class file
     * @param destinationDirectory the java destination file
     * @return the decompiled java source file or null if the file could not be decompiled
     */
    fun decompile(classFile: File, destinationDirectory: File): File? {
        ConsoleDecompiler.main(arrayOf(
            classFile.absolutePath,
            destinationDirectory.absolutePath
        ))

        return File(destinationDirectory, "${classFile.nameWithoutExtension}.java").let {
            if (it.exists()) it else null
        }
    }
}
