package org.cafejojo.schaapi

/**
 * Thrown when a required argument on the command line is missing.
 */
internal class MissingArgumentException(argument: String) : RuntimeException("Missing the '$argument' argument.") {
    /**
     * Creates specific message for the desired pipeline flavor.
     */
    fun messageForFlavor(flavor: String): String = "${this.message} was required for pipeline flavor $flavor."
}
