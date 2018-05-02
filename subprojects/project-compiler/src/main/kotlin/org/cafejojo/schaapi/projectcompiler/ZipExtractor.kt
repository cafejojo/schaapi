package org.cafejojo.schaapi.projectcompiler

import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipInputStream

/**
 * Extracts the given ZIP archive.
 */
class ZipExtractor(private val zipStream: InputStream) {
    /**
     * Extracts the ZIP archive to [target].
     * @param target the location to extract the ZIP archive to
     */
    fun extractTo(target: File) {
        if (!target.exists() || !target.isDirectory) {
            target.mkdirs()
        }

        val buffer = Array<Byte>(BUFFER_SIZE, { 0 }).toByteArray()
        val zis = ZipInputStream(zipStream)

        var entry = zis.nextEntry
        while (entry != null) {
            val entryName = entry.name
            val entryFile = target.resolve(entryName)

            if (entry.isDirectory) {
                entryFile.mkdirs()
            } else {
                entryFile.parentFile.mkdirs()
                entryFile.createNewFile()
                val entryStream = BufferedOutputStream(FileOutputStream(entryFile))

                var len = zis.read(buffer)
                while (len > 0) {
                    entryStream.write(buffer, 0, len)
                    len = zis.read(buffer)
                }

                entryStream.close()
            }

            entry = zis.nextEntry
        }

        zis.closeEntry()
        zis.close()
    }

    companion object {
        internal const val BUFFER_SIZE = 1024
    }
}
