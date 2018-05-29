package org.cafejojo.schaapi.web

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.client.TestRestTemplate

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class HomeTest {
    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @Test
    fun `it can retrieve the contents of the homepage`() {
        val body = restTemplate.getForObject("/", String::class.java)

        assertThat(body).isEqualTo("Welcome to Schaapi")
    }
}
