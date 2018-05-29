package org.cafejojo.schaapi.githubtestreporter

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.it

object AppKeyGeneratorTest : Spek({
    it("generates a jwt token to authenticate as GitHub app") {
        System.setProperty("app_id", "12345")
        System.setProperty("app_private_key_location", "pkcs8_key")

        val key = AppKeyGenerator.create()

        assertThat(key).isNotEmpty()
    }
})
