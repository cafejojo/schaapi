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

        beforeEachTest {
            target.mkdirs()
        }

        afterEachTest {
            target.deleteRecursively()
        }

        it("detects non-existing directories") {
            assertThrows<IllegalArgumentException> {
                ProjectCompiler(File("./invalid-path"))
            }
        }

        it("detects non-maven directories") {
            assertThrows<IllegalArgumentException> {
                ProjectCompiler(target)
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

            val classFiles = ProjectCompiler(target).compileProject()

            assertThat(classFiles).isEmpty()
        }

        it("compiles simple projects") {
            val projectZip = javaClass.getResource("/ProjectCompiler/simple.zip")
            ZipFile(projectZip.path).extractAll(target.absolutePath)

            val classFiles = ProjectCompiler(target).compileProject()

            assertThat(classFiles).containsExactly(
                target.resolve("target/classes/org/cafejojo/schaapi/test/MyClass.class")
            )
        }

        it("compiles projects with dependencies") {
            val projectZip = javaClass.getResource("/ProjectCompiler/dependencies.zip")
            ZipFile(projectZip.path).extractAll(target.absolutePath)

            val classFiles = ProjectCompiler(target).compileProject()

            assertThat(classFiles).containsExactly(
                target.resolve("target/classes/org/cafejojo/schaapi/test/MyClass.class")
            )
        }
    }
})
