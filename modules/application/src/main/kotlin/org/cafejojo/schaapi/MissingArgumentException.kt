package org.cafejojo.schaapi

internal class MissingArgumentException(argument: String) : RuntimeException("Missing the '$argument' argument.") {
    fun messageForType(type: String): String = "${this.message} Was required for pipeline type $type."
}
