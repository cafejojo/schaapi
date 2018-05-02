package org.cafejojo.schaapi.projectcompiler

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.jupiter.api.assertThrows
import java.io.File

internal class ProjectCompilerTest : Spek({
    describe("project compiler errors") {
        it("detects non-existing directories") {
            assertThrows<IllegalArgumentException> {
                ProjectCompiler(File("./invalid-path"))
            }
        }

        it("detects non-maven directories") {
            val target = File("test/")
            target.mkdirs()

            assertThrows<IllegalArgumentException> {
                ProjectCompiler(target)
            }

            target.delete()
        }
    }

    describe("project compiler compilation") {
        val target = File("./test")

        beforeGroup {
            MavenInstaller().installMaven(MAVEN_HOME)
        }

        afterEachTest {
            target.deleteRecursively()
        }

        it("compiles codeless projects") {
            val projectZip = javaClass.getResourceAsStream("/ProjectCompiler/no-sources.zip")
            ZipExtractor(projectZip).extractTo(target)

            val classFiles = ProjectCompiler(target).compileProject()

            assertThat(classFiles).isEmpty()
        }

        it("compiles simple projects") {
            val projectZip = javaClass.getResourceAsStream("/ProjectCompiler/simple.zip")
            ZipExtractor(projectZip).extractTo(target)

            val classFiles = ProjectCompiler(target).compileProject()

            assertThat(classFiles).containsExactly(
                target.resolve("target/classes/org/cafejojo/schaapi/test/MyClass.class")
            )
        }
    }
})
