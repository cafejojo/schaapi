package org.cafejojo.schaapi.validationpipeline.cijob

import org.assertj.core.api.Assertions.assertThat
import org.cafejojo.schaapi.validationpipeline.events.CIJobCompletedEvent
import org.cafejojo.schaapi.validationpipeline.events.ValidationRequestReceivedEvent
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.it
import org.springframework.context.ApplicationEventPublisher
import java.io.File
import java.nio.file.Files

object IntegrationTest : Spek({
    lateinit var storageDirectory: File
    lateinit var projectDirectory: File

    beforeEachTest {
        storageDirectory = Files.createTempDirectory("schaapi-ci").toFile()
        projectDirectory = File(storageDirectory, "projects/cafejojo/dummy-simple-maven-library").also { it.mkdirs() }

        val testsDirectory = File(projectDirectory, "tests").also { it.mkdir() }

        File(CIJobTest::class.java.getResource("/projects/cafejojo/dummy-simple-maven-library/tests").file)
            .listFiles()
            .forEach { it.copyTo(File(testsDirectory, it.name), overwrite = true) }
    }

    afterEachTest {
        CIJobInitiator.executorService.shutdown()
    }

    it("initiates a CI job") {
        val commitHash = "ced7a679ba6337977d99effccdb4ac66b3ba34e0"
        val downloadUrl = "https://github.com/cafejojo/dummy-simple-maven-library/archive/$commitHash.zip"

        var event: CIJobCompletedEvent? = null
        val initiator = CIJobInitiator(
            ApplicationEventPublisher { event = it as? CIJobCompletedEvent }
        )

        initiator.handleValidateRequestReceivedEvent(
            ValidationRequestReceivedEvent(commitHash, projectDirectory, downloadUrl)
        )

        assertThat(event).isNotNull()
        event?.apply {
            assertThat(testResults.totalCount).isEqualTo(1)
            assertThat(testResults.passCount).isEqualTo(1)
        }
    }
})
