package org.cafejojo.schaapi.miningpipeline.projectcompiler.javajar

import org.assertj.core.api.Assertions.assertThat
import org.cafejojo.schaapi.models.project.JavaJarProject
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.io.File
import java.io.FileNotFoundException
import java.net.URLDecoder

internal class ProjectCompilerTest : Spek({
    describe("Java JAR project compilation") {
        it("compiles simple projects") {
            val projectFile = File(URLDecoder.decode(
                getResourceURI("/schaapi.simple-project-1.0.0.jar").path, "UTF-8"))
            val project = JavaJarProject(projectFile)
            ProjectCompiler().compile(project)

            assertThat(project.projectDir).isEqualTo(projectFile)
            assertThat(project.classes).isEmpty()
            assertThat(project.classNames).containsExactlyInAnyOrder(
                "org.cafejojo.schaapi.test.MyFirstClass",
                "org.cafejojo.schaapi.test.MySecondClass"
            )
            assertThat(project.dependencies).isEmpty()
            assertThat(project.classpath).isEqualTo(projectFile.absolutePath)
        }

        it("compiles projects with no classes") {
            val projectFile = File(URLDecoder.decode(
                getResourceURI("/schaapi.no-classes-project-1.0.0.jar").path, "UTF-8"))
            val project = JavaJarProject(projectFile)
            ProjectCompiler().compile(project)

            assertThat(project.projectDir).isEqualTo(projectFile)
            assertThat(project.classes).isEmpty()
            assertThat(project.classNames).isEmpty()
            assertThat(project.dependencies).isEmpty()
            assertThat(project.classpath).isEqualTo(projectFile.absolutePath)
        }
    }
})

fun getResourceURI(path: String) = ProjectCompilerTest::class.java.getResource(path)
    ?: throw FileNotFoundException("Could not find test resources.")