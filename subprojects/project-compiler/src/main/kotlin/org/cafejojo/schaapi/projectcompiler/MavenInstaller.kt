package org.cafejojo.schaapi.projectcompiler

import java.io.File

/**
 * Installs a bundled version of Maven.
 */
class MavenInstaller {
    /**
     * Installs Maven at [path].
     * @param path the directory to install Maven in
     */
    fun installMaven(path: File) {
        val zipStream = javaClass.getResourceAsStream("/maven/apache-maven-3.5.3-bin.zip")
        ZipExtractor(zipStream).extractTo(path)
    }

    companion object {
        val DEFAULT_MAVEN_HOME = File(System.getProperty("user.home") + "/schaapi/maven")
    }
}