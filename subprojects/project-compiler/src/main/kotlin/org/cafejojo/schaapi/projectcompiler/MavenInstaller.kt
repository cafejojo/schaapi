package org.cafejojo.schaapi.projectcompiler

import java.io.File

val MAVEN_HOME = File(System.getProperty("user.home") + "/schaapi/maven")

class MavenInstaller {
    fun installMaven(path: File) {
        val zipStream = javaClass.getResourceAsStream("/maven/apache-maven-3.5.3-bin.zip")
        ZipExtractor(zipStream).extractTo(path)
    }
}
