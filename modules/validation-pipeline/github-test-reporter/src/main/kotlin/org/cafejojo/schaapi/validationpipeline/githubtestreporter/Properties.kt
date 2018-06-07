package org.cafejojo.schaapi.validationpipeline.githubtestreporter

import java.io.File
import java.io.FileInputStream

/**
 * Environment properties of the GitHub test reporter.
 */
object Properties {
    val appId: String
    val appPrivateKeyLocation: String

    init {
        if (File("githubtestreporter.properties").exists()) {
            System.getProperties().load(FileInputStream("githubtestreporter.properties"))
        }

        appId =
            getProperty("app_id")
        appPrivateKeyLocation =
            getProperty("app_private_key_location")
    }

    private fun getProperty(name: String) = System.getProperty(name) ?: throw PropertyNotSetException(
        name)
}

private class PropertyNotSetException(propertyName: String) : RuntimeException("Property `$propertyName` is not set.")
