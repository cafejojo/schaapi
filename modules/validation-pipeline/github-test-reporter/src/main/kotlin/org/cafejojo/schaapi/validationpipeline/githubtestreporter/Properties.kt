package org.cafejojo.schaapi.validationpipeline.githubtestreporter

import java.io.File

/**
 * Environment properties of the GitHub test reporter.
 */
object Properties {
    val appId: String
        get() = getProperty("app_id")
    val appPrivateKeyLocation: String
        get() = getProperty("app_private_key_location")
    val testsStorageLocation: File
        get() = File(getProperty("app_private_key_location"))

    init {
        File("githubtestreporter.properties").apply {
            if (exists()) System.getProperties().load(inputStream())
        }
    }

    private fun getProperty(name: String) = System.getProperty(name) ?: throw PropertyNotSetException(name)
}

private class PropertyNotSetException(propertyName: String) : RuntimeException("Property `$propertyName` is not set.")
