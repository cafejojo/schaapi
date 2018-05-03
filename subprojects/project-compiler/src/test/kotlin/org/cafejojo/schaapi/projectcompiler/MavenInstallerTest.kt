package org.cafejojo.schaapi.projectcompiler

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.io.File

internal class MavenInstallerTest : Spek({
    describe("Maven installer") {
        val target = File("./test/")

        beforeGroup {
            target.deleteRecursively()
        }

        afterEachTest {
            target.deleteRecursively()
        }

        it("installs Maven") {
            MavenInstaller().installMaven(target)

            assertThat(target.resolve("README.txt")).isFile()
            assertThat(target.resolve("bin/mvn")).isFile()
            assertThat(target.resolve("bin/mvn.cmd")).isFile()
        }

        it("repairs a broken Maven installation") {
            MavenInstaller().installMaven(target)
            val oldReadMeContent = target.resolve("README.txt").readText()

            target.resolve("README.txt").writeText("")
            target.resolve("bin/mvn").delete()

            MavenInstaller().installMaven(target)

            assertThat(target.resolve("README.txt")).hasContent(oldReadMeContent)
            assertThat(target.resolve("bin/mvn")).isFile()
            assertThat(target.resolve("bin/mvn.cmd")).isFile()
        }
    }
})
