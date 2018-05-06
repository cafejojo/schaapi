package org.cafejojo.schaapi.projectcompiler

import net.lingala.zip4j.core.ZipFile
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.jupiter.api.assertThrows
import java.io.File

internal class JavaMavenProjectTest : Spek({
    describe("Java Maven project validation") {
        val target = File("test/")

        afterEachTest {
            target.deleteRecursively()
        }

        it("detects non-existing directories") {
            assertThrows<IllegalArgumentException> {
                JavaMavenProject(File("./invalid-path"))
            }
        }

        it("detects non-maven directories") {
            assertThrows<IllegalArgumentException> {
                JavaMavenProject(target)
            }
        }
    }

    describe("Java Maven project compilation") {
        val target = File("./test")

        beforeGroup {
            MavenInstaller().installMaven(MavenInstaller.DEFAULT_MAVEN_HOME)
        }

        afterEachTest {
            target.deleteRecursively()
        }

        it("compiles codeless projects") {
            val projectZip = javaClass.getResource("/ProjectCompiler/no-sources.zip")
            ZipFile(projectZip.path).extractAll(target.absolutePath)

            val project = JavaMavenProject(target)
            project.compile()

            assertThat(project.projectDir).isEqualTo(target)
            assertThat(project.classes).isEmpty()
            assertThat(project.dependencies).isEmpty()
            assertThat(project.classpath.split(File.pathSeparator)).containsExactlyInAnyOrder(
                target.resolve("target/classes").absolutePath
            )
        }

        it("compiles simple projects") {
            val projectZip = javaClass.getResource("/ProjectCompiler/simple.zip")
            ZipFile(projectZip.path).extractAll(target.absolutePath)

            val project = JavaMavenProject(target)
            project.compile()

            assertThat(project.projectDir).isEqualTo(target)
            assertThat(project.classes).containsExactlyInAnyOrder(
                target.resolve("target/classes/org/cafejojo/schaapi/test/MyClass.class")
            )
            assertThat(project.dependencies).isEmpty()
            assertThat(project.classpath.split(File.pathSeparator)).containsExactlyInAnyOrder(
                target.resolve("target/classes").absolutePath
            )
        }

        it("compiles projects with dependencies") {
            val projectZip = javaClass.getResource("/ProjectCompiler/dependencies.zip")
            ZipFile(projectZip.path).extractAll(target.absolutePath)

            val project = JavaMavenProject(target)
            project.compile()

            assertThat(project.projectDir).isEqualTo(target)
            assertThat(project.classes).containsExactlyInAnyOrder(
                target.resolve("target/classes/org/cafejojo/schaapi/test/MyClass.class")
            )
            assertThat(project.dependencies).containsExactlyInAnyOrder(
                target.resolve("target/dependency/zip4j-1.3.2.jar")
            )
            assertThat(project.classpath.split(File.pathSeparator)).containsExactlyInAnyOrder(
                target.resolve("target/classes").absolutePath,
                target.resolve("target/dependency").resolve("zip4j-1.3.2.jar").absolutePath
            )
        }
    }

    describe("Java Maven project classes") {
        val target = File("./test")

        beforeEachTest {
            val projectURI = javaClass.getResource("/Project/dependencies-classes")
            if (projectURI == null) {
                Assertions.fail("Project source directory could not be found.")
            }

            val projectFiles = File(projectURI.toURI())
            projectFiles.copyRecursively(target, true)
        }

        afterEachTest {
            target.deleteRecursively()
        }

        it("knows which classes it contains") {
            val project = JavaMavenProject(target)
            project.compile()

            assertThat(project.containsClass("org.cafejojo.schaapi.test.MyClass")).isTrue()
        }

        it("is specific about packages") {
            val project = JavaMavenProject(target)
            project.compile()

            assertThat(project.containsClass("MyClass")).isFalse()
        }

        it("does not think it contains classes from its dependencies") {
            val project = JavaMavenProject(target)
            project.compile()

            assertThat(project.containsClass("net.lingala.zip4j.core.ZipFile")).isFalse()
        }
    }
})
