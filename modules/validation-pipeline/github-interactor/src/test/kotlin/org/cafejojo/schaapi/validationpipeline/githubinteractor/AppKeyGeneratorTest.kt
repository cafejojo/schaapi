package org.cafejojo.schaapi.validationpipeline.githubinteractor

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.it
import java.net.URLDecoder

object AppKeyGeneratorTest : Spek({
    beforeEachTest {
        System.getProperties().load(
            AppKeyGeneratorTest::class.java.getResourceAsStream("/githubtestreporter.properties")
        )
    }

    it("generates a jwt token to authenticate as GitHub app") {
        System.setProperty("app_id", "12345")
        System.setProperty("app_private_key_location", getResourcePath("/pkcs8_key"))

        val key = AppKeyGenerator().create()

        assertThat(key).isNotEmpty()
    }
})

private fun getResourcePath(path: String) =
    URLDecoder.decode(AppKeyGeneratorTest::class.java.getResource(path).path, "UTF-8")
