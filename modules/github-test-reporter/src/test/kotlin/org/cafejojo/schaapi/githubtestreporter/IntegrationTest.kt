package org.cafejojo.schaapi.githubtestreporter

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.cafejojo.schaapi.web.WebApplication
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.concurrent.TimeUnit

@ExtendWith(SpringExtension::class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [IntegrationTest.TestConfig::class]
)
class IntegrationTest {
    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @Configuration
    @Import(WebApplication::class)
    internal open class TestConfig {
        @Bean
        open fun appKeyGenerator(): AppKeyGenerator = mock {
            on { create() } doReturn "this-is-the-app-key"
        }
    }

    @Test
    fun `it can receive a check suite start web hook`() {
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
}
