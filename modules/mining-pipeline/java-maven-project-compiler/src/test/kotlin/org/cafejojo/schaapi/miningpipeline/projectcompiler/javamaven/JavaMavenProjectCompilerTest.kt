package org.cafejojo.schaapi.miningpipeline.projectcompiler.javamaven

import org.assertj.core.api.Assertions.assertThat
import org.cafejojo.schaapi.maveninstaller.MavenInstaller
import org.cafejojo.schaapi.models.project.JavaMavenProject
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.jupiter.api.assertThrows
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Files

internal object JavaMavenProjectCompilerTest : Spek({
    lateinit var mavenHome: File
    lateinit var target: File

    beforeGroup {
        mavenHome = Files.createTempDirectory("schaapi-maven").toFile()
        MavenInstaller().installMaven(mavenHome)
    }

    afterGroup {
        mavenHome.deleteRecursively()
    }

    beforeEachTest {
        target = Files.createTempDirectory("schaapi-test").toFile()
    }

    afterEachTest {
        target.deleteRecursively()
    }

    describe("Java Maven project validation") {
        it("detects non-existing directories") {
            assertThrows<IllegalArgumentException> {
                JavaMavenProject(File("./invalid-path"), mavenHome)
            }
        }

        it("detects non-maven directories") {
            assertThrows<IllegalArgumentException> {
                JavaMavenProject(target, mavenHome)
            }
        }
    }

    describe("Java Maven project compilation") {
        it("compiles codeless projects") {
            setUpTestFiles("/ProjectCompiler/no-sources", target)

            val project = JavaMavenProject(target, mavenHome)
            JavaMavenProjectCompiler().compile(project)

            assertThat(project.projectDir).isEqualTo(target)
            assertThat(project.classes).isEmpty()
            assertThat(project.classNames).isEmpty()
            assertThat(project.dependencies).isEmpty()
            assertThat(project.classpath.split(File.pathSeparator)).containsExactlyInAnyOrder(
                target.resolve("target/classes").absolutePath
            )
        }

        it("compiles simple projects") {
            setUpTestFiles("/ProjectCompiler/simple", target)

            val project = JavaMavenProject(target, mavenHome)
            JavaMavenProjectCompiler().compile(project)

            assertThat(project.projectDir).isEqualTo(target)
            assertThat(project.classes).containsExactlyInAnyOrder(
                target.resolve("target/classes/org/cafejojo/schaapi/test/MyClass.class")
            )
            assertThat(project.classNames).containsExactlyInAnyOrder(
                "org.cafejojo.schaapi.test.MyClass"
            )
            assertThat(project.dependencies).isEmpty()
            assertThat(project.classpath.split(File.pathSeparator)).containsExactlyInAnyOrder(
                target.resolve("target/classes").absolutePath
            )
        }

        it("compiles projects with dependencies") {
            setUpTestFiles("/ProjectCompiler/dependencies", target)

            val project = JavaMavenProject(target, mavenHome)
            JavaMavenProjectCompiler().compile(project)

            assertThat(project.projectDir).isEqualTo(target)
            assertThat(project.classes).containsExactlyInAnyOrder(
                target.resolve("target/classes/org/cafejojo/schaapi/test/MyClass.class")
            )
            assertThat(project.classNames).containsExactlyInAnyOrder(
                "org.cafejojo.schaapi.test.MyClass"
            )
            assertThat(project.dependencies).isEmpty()
            assertThat(project.classpath.split(File.pathSeparator)).containsExactlyInAnyOrder(
                target.resolve("target/classes").absolutePath
            )
        }
    }
})

private fun setUpTestFiles(resourceString: String, target: File) {
    val projectURI = JavaMavenProjectCompilerTest::class.java.getResource(resourceString)
        ?: throw FileNotFoundException("Could not find test resources at $resourceString.")

    val projectFiles = File(projectURI.toURI())
    projectFiles.copyRecursively(target, true)
}
