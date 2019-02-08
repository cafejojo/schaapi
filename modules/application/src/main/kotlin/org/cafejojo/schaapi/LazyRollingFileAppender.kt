package org.cafejojo.schaapi

import ch.qos.logback.core.FileAppender
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

/**
 * A [FileAppender] that uses a [LazyFileOutputStream] so that the output file is created only when the first output
 * is written to it.
 *
 * Based on <a href="https://stackoverflow.com/a/37157829/3307872]">a StackOverflow answer by Robert</a>.
 *
 * @param E the type of the [FileAppender]
 */
class LazyFileAppender<E> : FileAppender<E>() {
    override fun openFile(fileName: String) {
        lock.lock()

        try {
            val file = File(fileName).apply { parentFile?.mkdirs() }
            outputStream = LazyFileOutputStream(file, append)
        } finally {
            lock.unlock()
        }
    }
}

/**
 * An [OutputStream] that creates the underlying [FileOutputStream] only when it is first written to.
 *
 * Based on <a href="https://github.com/aleroot/log4j-additions">Alessio Pollero's log4j-additions library</a>.
 *
 * @property file the file to write to
 * @property append whether the file should be appended to
 */
@Suppress("LateinitUsage") // Lateinit is exactly the feature of this class
class LazyFileOutputStream(private var file: File, private var append: Boolean) : OutputStream() {
    private var streamLock = Any()
    private var streamOpen = false
    private lateinit var oStream: FileOutputStream

    private fun outputStream(): FileOutputStream {
        synchronized(streamLock) {
            if (!streamOpen) {
                oStream = FileOutputStream(file, append)
                streamOpen = true
            }
        }

        return oStream
    }

    override fun close() {
        super.close()

        if (streamOpen)
            outputStream().close()
    }

    override fun flush() {
        super.flush()

        if (streamOpen)
            outputStream().flush()
    }

    override fun write(byte: Int) = outputStream().write(byte)

    override fun write(bytes: ByteArray, offset: Int, length: Int) = outputStream().write(bytes, offset, length)

    override fun write(bytes: ByteArray) = outputStream().write(bytes)
}
