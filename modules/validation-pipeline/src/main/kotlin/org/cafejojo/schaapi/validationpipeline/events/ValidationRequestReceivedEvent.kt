package org.cafejojo.schaapi.validationpipeline.events

import org.cafejojo.schaapi.validationpipeline.CIJobProjectMetadata
import java.io.File

/**
 * Event that can be broadcast after receiving a request to start the validation process.
 */
data class ValidationRequestReceivedEvent(
    val directory: File,
    val downloadUrl: String,
    val metadata: CIJobProjectMetadata
)
