package org.cafejojo.schaapi.validationpipeline.events

import java.io.File

data class ValidateRequestReceivedEvent(val directory: File, val downloadUrl: String)
