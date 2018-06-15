package org.cafejojo.schaapi.validationpipeline.cijob

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.it
import java.io.File
import java.nio.file.Files

object CIJobTest : Spek({
    lateinit var projectDirectory: File

    beforeEachTest {
        projectDirectory = Files.createTempDirectory("schaapi-ci").toFile()
    }

    it("executes a CI job") {
        val commitHash = "4f392a58006d64c4ac3259bee14b0bf4484a431f"
        val ciJob = CIJob(commitHash, projectDirectory, "https://github.com/cafejojo/schaapi/archive/$commitHash.zip")

        ciJob.run()

        assertThat(File(projectDirectory, "builds/$commitHash/LICENSE")).exists()
    }
})
