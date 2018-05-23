package org.cafejojo.schaapi.pipeline.projectcompiler.javajar

import org.assertj.core.api.Assertions.assertThat
import org.cafejojo.schaapi.models.project.java.JavaJarProject
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.io.File
import java.io.FileNotFoundException

internal class ProjectCompilerTest : Spek({
    val projectURI = ProjectCompilerTest::class.java.getResource("/schaapi.simple-project-1.0.0.jar")
        ?: throw FileNotFoundException("Could not find test resources.")

    describe("Java JAR project compilation") {
        it("compiles simple projects") {
            val projectFile = File(projectURI.path)
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
    }
})
