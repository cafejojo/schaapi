package org.cafejojo.schaapi.validationpipeline.githubinteractor

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.cafejojo.schaapi.validationpipeline.githubinteractor.githubapi.json
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit

@ExtendWith(SpringExtension::class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [IntegrationTest.TestConfig::class]
)
class IntegrationTest {
    lateinit var testsStorageLocation: Path

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @Configuration
    @SpringBootApplication
    internal open class TestConfig {
        @Bean
        open fun appKeyGenerator(): AppKeyGenerator = mock {
            on { create() } doReturn "this-is-the-app-key"
        }
    }

    @BeforeEach
    fun setUp() {
        System.getProperties().load(
            IntegrationTest::class.java.getResourceAsStream("/githubtestreporter.properties")
        )

        testsStorageLocation = Files.createTempDirectory("schaapi-github")

        System.setProperty("tests_storage_location", testsStorageLocation.toString())
    }

    @AfterEach
    fun tearDown() {
        testsStorageLocation.toFile().deleteRecursively()
    }

    @Test
    fun `it can receive a 'check suite started' web hook`() {
        val (futureRequest, _) = mockHttpClient(
            json("installation token request response") {
                "token" to "this-is-the-installation-token"
                "expires_at" to "end date"
            },
            json("report started response") {
                "id" to 87575775
            }
        )[1]

        val requestJson = IntegrationTest::class.java
            .getResourceAsStream("/fixtures/github/check_suite_webhook.resp").bufferedReader().readText()

        val entity = HttpEntity(
            requestJson,
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
                add("X-GitHub-Event", "check_suite")
            }
        )

        val body = restTemplate.postForObject("/process-webhook", entity, String::class.java)

        with(futureRequest.get(0, TimeUnit.SECONDS)) {
            assertThat(url.toString()).contains("casperboone/ci-test")
            assertThat(this.headers).contains(entry("Authorization", "Token this-is-the-installation-token"))

            assertThatJson(bodyContents()).node("head_branch").isEqualTo("casperboone-patch-4")
            assertThatJson(bodyContents()).node("head_sha").isEqualTo("9d730c5673cdffa01a9171de7c25eb2ba01c5d8a")
        }

        assertThat(body).isEqualTo("Webhook received.")
    }

    @Test
    fun `it can receive a 'check run rerequested' web hook`() {
        val (futureRequest, _) = mockHttpClient(
            json("installation token request response") {
                "token" to "this-is-the-installation-token"
                "expires_at" to "end date"
            },
            json("report started response") {
                "id" to 87575775
            }
        )[1]

        val requestJson = IntegrationTest::class.java
            .getResourceAsStream("/fixtures/github/check_run_rerequested_webhook.resp").bufferedReader().readText()

        val entity = HttpEntity(
            requestJson,
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
                add("X-GitHub-Event", "check_run")
            }
        )

        val body = restTemplate.postForObject("/process-webhook", entity, String::class.java)

        with(futureRequest.get(0, TimeUnit.SECONDS)) {
            assertThat(url.toString()).contains("cafejojo/dummy-simple-maven-library")
            assertThat(this.headers).contains(entry("Authorization", "Token this-is-the-installation-token"))

            assertThatJson(bodyContents()).node("head_branch").isEqualTo("breaking-change")
            assertThatJson(bodyContents()).node("head_sha").isEqualTo("7eef62fb10b42c3d2bf54bba6b3658e53e79bb10")
        }

        assertThat(body).isEqualTo("Webhook received.")
    }

    @Test
    fun `it can receive an 'installation created' web hook`() {
        val requestJson = IntegrationTest::class.java
            .getResourceAsStream("/fixtures/github/installation_created_webhook.resp").bufferedReader().readText()

        val entity = HttpEntity(
            requestJson,
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
                add("X-GitHub-Event", "installation")
            }
        )

        val body = restTemplate.postForObject("/process-webhook", entity, String::class.java)

        assertThat(body).isEqualTo("Webhook received.")

        assertThat(File(Properties.testsStorageLocation, "cabotest/hello-world")).exists()
    }

    @Test
    fun `it can receive an 'installation deleted' web hook`() {
        val requestJson = IntegrationTest::class.java
            .getResourceAsStream("/fixtures/github/installation_deleted_webhook.resp").bufferedReader().readText()

        val entity = HttpEntity(
            requestJson,
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
                add("X-GitHub-Event", "installation")
            }
        )

        val body = restTemplate.postForObject("/process-webhook", entity, String::class.java)

        assertThat(body).isEqualTo("Webhook received.")

        assertThat(File(Properties.testsStorageLocation, "cabotest/hello-world")).doesNotExist()
        assertThat(File(Properties.testsStorageLocation, "cabotest")).doesNotExist()
    }
}
