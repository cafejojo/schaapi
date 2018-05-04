package org.cafejojo.schaapi.projectcompiler

import net.lingala.zip4j.core.ZipFile
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.jupiter.api.assertThrows
import java.io.File

internal class ProjectCompilerTest : Spek({
    describe("project compiler errors") {
        val target = File("test/")

        afterEachTest {
            target.deleteRecursively()
        }

        it("detects non-existing directories") {
            assertThrows<IllegalArgumentException> {
                ProjectCompiler().compileProject(File("./invalid-path"))
            }
        }

        it("detects non-maven directories") {
            assertThrows<IllegalArgumentException> {
                ProjectCompiler().compileProject(target)
            }
        }
    }

    describe("project compiler compilation") {
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

            val project = ProjectCompiler().compileProject(target)

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

            val project = ProjectCompiler().compileProject(target)

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

            val project = ProjectCompiler().compileProject(target)

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
})
