package org.cafejojo.schaapi.miningpipeline.projectcompiler.javamaven

import org.zeroturnaround.zip.ZipUtil
import java.io.File

/**
 * Installs a bundled version of Maven.
 */
class MavenInstaller {
    /**
     * Installs Maven at [path].
     *
     * @param path the directory to install Maven in
     */
    fun installMaven(path: File) {
        val zipStream = javaClass.getResourceAsStream("/maven/apache-maven-3.5.3-bin.zip")
        ZipUtil.unpack(zipStream, path.absoluteFile)
        path.resolve("bin/mvn").setExecutable(true)
    }
}
