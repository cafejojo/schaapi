package org.cafejojo.schaapi.projectcompiler

import net.lingala.zip4j.core.ZipFile
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.io.File

internal class ProjectTest : Spek({
    describe("projects") {
        val target = File("./test")

        afterEachTest {
            target.deleteRecursively()
        }

        it("knows which classes it contains") {
            val projectZip = javaClass.getResource("/Project/dependencies-classes.zip")
            ZipFile(projectZip.path).extractAll(target.absolutePath)

            val project = Project(target)

            assertThat(project.containsClass("org.cafejojo.schaapi.test.MyClass")).isTrue()
        }

        it("is specific about packages") {
            val projectZip = javaClass.getResource("/Project/dependencies-classes.zip")
            ZipFile(projectZip.path).extractAll(target.absolutePath)

            val project = Project(target)

            assertThat(project.containsClass("MyClass")).isFalse()
        }

        it("does not think it contains classes from its dependencies") {
            val projectZip = javaClass.getResource("/Project/dependencies-classes.zip")
            ZipFile(projectZip.path).extractAll(target.absolutePath)

            val project = Project(target)

            assertThat(project.containsClass("net.lingala.zip4j.core.ZipFile")).isFalse()
        }
    }
})
