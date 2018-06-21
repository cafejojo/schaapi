package org.cafejojo.schaapi.validationpipeline

/**
 * Exception that gets thrown when an error occurs during the execution of a CI job.
 */
class CIJobException(message: String, cause: Throwable? = null) : Exception(message, cause)
