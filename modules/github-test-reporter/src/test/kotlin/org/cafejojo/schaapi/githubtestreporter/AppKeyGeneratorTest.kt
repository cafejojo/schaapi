package org.cafejojo.schaapi.githubtestreporter

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.it
import java.net.URLDecoder
import java.nio.file.Paths

object AppKeyGeneratorTest : Spek({
    it("generates a jwt token to authenticate as GitHub app") {
        System.setProperty("app_id", "12345")
        System.setProperty("app_private_key_location", getResourcePath("/pkcs8_key"))

        val key = AppKeyGenerator.create()

        assertThat(key).isNotEmpty()
    }
})

private fun getResourcePath(path: String) =
    URLDecoder.decode(Paths.get(AppKeyGeneratorTest::class.java.getResource(path).toURI()).toString(), "UTF-8")
