package org.cafejojo.schaapi.projectcompiler

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
    fun installMaven(path: File = DEFAULT_MAVEN_HOME) {
        val zipStream = javaClass.getResourceAsStream("/maven/apache-maven-3.5.3-bin.zip")
        ZipUtil.unpack(zipStream, path.absoluteFile)
        path.resolve("bin/mvn").setExecutable(true)
    }

    companion object {
        val DEFAULT_MAVEN_HOME = File(System.getProperty("user.home") + "/.schaapi/maven")
    }
}