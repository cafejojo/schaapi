package org.cafejojo.schaapi.pipeline.projectcompiler.javajar

import org.assertj.core.api.Assertions.assertThat
import org.cafejojo.schaapi.models.project.JavaJarProject
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.io.File
import java.io.FileNotFoundException

internal class ProjectCompilerTest : Spek({
    describe("Java JAR project compilation") {
        it("compiles simple projects") {
            val projectFile = File(getResourceURI("/schaapi.simple-project-1.0.0.jar").path)
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
            val projectFile = File(getResourceURI("/schaapi.no-classes-project-1.0.0.jar").path)
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
