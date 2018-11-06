package org.cafejojo.schaapi.miningpipeline.miner.github

import org.assertj.core.api.Assertions.assertThat
import org.cafejojo.schaapi.models.project.JavaMavenProject
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.it
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Files

internal object MavenLibraryVersionVerifierTest : Spek({
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

    it("should allow the library dependency in a project using the correct version") {
        setUpTestFiles("/verifiable-project", target)

        assertThat(
            MavenLibraryVersionVerifier("net.lingala.zip4j", "zip4j", "1.3.2")
                .verify(JavaMavenProject(target, mavenHome))
        ).isTrue()
    }

    it("should disallow the library dependency in a project using the wrong version") {
        setUpTestFiles("/verifiable-project", target)

        assertThat(
            MavenLibraryVersionVerifier("net.lingala.zip4j", "zip4j", "1.3.1")
                .verify(JavaMavenProject(target, mavenHome))
        ).isFalse()
    }

    it("should disallow a project not using the library dependency") {
        setUpTestFiles("/verifiable-project", target)

        assertThat(
            MavenLibraryVersionVerifier("com.example", "app", "1.0.1")
                .verify(JavaMavenProject(target, mavenHome))
        ).isFalse()
    }
})

private fun setUpTestFiles(resourceString: String, target: File) {
    val projectURI = MavenLibraryVersionVerifierTest::class.java.getResource(resourceString)
        ?: throw FileNotFoundException("Could not find test resources at $resourceString.")

    val projectFiles = File(projectURI.toURI())
    projectFiles.copyRecursively(target, true)
}
