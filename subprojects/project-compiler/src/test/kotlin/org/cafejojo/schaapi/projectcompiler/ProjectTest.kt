package org.cafejojo.schaapi.projectcompiler

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.io.File

internal class ProjectTest : Spek({
    describe("projects") {
        val target = File("./test")

        beforeEachTest {
            val projectURI = javaClass.getResource("/Project/dependencies-classes")
            if (projectURI == null) {
                fail("Project source directory could not be found.")
            }

            val projectFiles = File(projectURI.toURI())
            projectFiles.copyRecursively(target, true)
        }

        afterEachTest {
            target.deleteRecursively()
        }

        it("knows which classes it contains") {
            val project = Project(target)

            assertThat(project.containsClass("MyClass")).isFalse()
        }

        it("is specific about packages") {
            val project = Project(target)

            assertThat(project.containsClass("MyClass")).isFalse()
        }

        it("does not think it contains classes from its dependencies") {
            val project = Project(target)

            assertThat(project.containsClass("net.lingala.zip4j.core.ZipFile")).isFalse()
        }
    }
})
