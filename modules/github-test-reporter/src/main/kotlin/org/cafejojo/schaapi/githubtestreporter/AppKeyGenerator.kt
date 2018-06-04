package org.cafejojo.schaapi.githubtestreporter

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.io.File
import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Calendar
import java.util.Date

/**
 * JWT token generator used for authentication as GitHub app.
 */
object AppKeyGenerator {
    private const val EXPIRE_AFTER_MINUTES = 10

    /**
     * Creates an app key.
     */
    fun create(): String {
        val issueDate = Date()
        val expiryDate = Calendar.getInstance().run {
            time = issueDate
            add(Calendar.MINUTE, EXPIRE_AFTER_MINUTES)
            time
        }

        val algorithm = Algorithm.RSA256(null, getPrivateKey(Properties.appPrivateKeyLocation))

        return JWT.create()
            .withIssuer(Properties.appId)
            .withIssuedAt(issueDate)
            .withExpiresAt(expiryDate)
            .sign(algorithm)
    }

    private fun getPrivateKey(filename: String): RSAPrivateKey =
        File(filename).readBytes()
            .let { keyBytes -> PKCS8EncodedKeySpec(keyBytes) }
            .let { spec -> KeyFactory.getInstance("RSA").generatePrivate(spec) }
            .let { it as? RSAPrivateKey ?: throw IllegalStateException("Key `$filename` is not a valid key.") }
}
